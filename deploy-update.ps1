[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$Server,
    [string]$User = "ubuntu",
    [string]$RemoteDir = "/home/ubuntu/beauty-console-deploy",
    [switch]$SkipBuild,
    [switch]$PackageOnly
)

$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($Server)) {
    throw "Please provide the server domain or IP with -Server."
}
$ProjectRoot = $PSScriptRoot
$Timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$GitHash = (& git -C $ProjectRoot rev-parse --short HEAD).Trim()
if (-not $GitHash) {
    $GitHash = "nogit"
}
$ReleaseName = "beauty-console-update-$GitHash-$Timestamp"
$ReleaseDir = Join-Path $ProjectRoot "tmp\$ReleaseName"
$MigrationDir = Join-Path $ReleaseDir "migrations"
$ArtifactDir = Join-Path $ProjectRoot "deploy-artifacts"
$PackagePath = Join-Path $ArtifactDir "$ReleaseName.tar.gz"

New-Item -ItemType Directory -Path $ReleaseDir, $MigrationDir, $ArtifactDir -Force | Out-Null

if (-not $SkipBuild) {
    Write-Host "[1/5] Building backend and frontend images..." -ForegroundColor Cyan
    & docker compose -f (Join-Path $ProjectRoot "docker-compose.yml") build app web
    if ($LASTEXITCODE -ne 0) { throw "Docker image build failed." }
}

Write-Host "[2/5] Exporting Docker images..." -ForegroundColor Cyan
& docker image inspect beauty-console-app:latest beauty-console-web:latest | Out-Null
if ($LASTEXITCODE -ne 0) { throw "Required application images do not exist." }
& docker save --output (Join-Path $ReleaseDir "images.tar") beauty-console-app:latest beauty-console-web:latest
if ($LASTEXITCODE -ne 0) { throw "Docker image export failed." }

Copy-Item -LiteralPath (Join-Path $ProjectRoot "docker-compose.server.yml") -Destination $ReleaseDir
Copy-Item -LiteralPath (Join-Path $ProjectRoot "update-server.sh") -Destination $ReleaseDir
$MigrationSource = Join-Path $ProjectRoot "beauty-console\store-server\src\main\resources\sql\migration"
$MigrationFiles = @(Get-ChildItem -LiteralPath $MigrationSource -Filter "V*.sql" -File | Sort-Object Name)
foreach ($MigrationFile in $MigrationFiles) {
    Copy-Item -LiteralPath $MigrationFile.FullName -Destination $MigrationDir
}

$ManifestFiles = @(
    "images.tar",
    "docker-compose.server.yml",
    "update-server.sh"
)
$ManifestFiles += $MigrationFiles | ForEach-Object { "migrations/$($_.Name)" }
$ManifestLines = foreach ($RelativePath in $ManifestFiles) {
    $LocalPath = Join-Path $ReleaseDir ($RelativePath -replace '/', '\')
    $Hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $LocalPath).Hash.ToLowerInvariant()
    "$Hash  $RelativePath"
}
$ManifestPath = Join-Path $ReleaseDir "manifest.sha256"
[System.IO.File]::WriteAllText(
    $ManifestPath,
    (($ManifestLines -join "`n") + "`n"),
    [System.Text.Encoding]::ASCII)

Write-Host "[3/5] Creating update package..." -ForegroundColor Cyan
& tar -czf $PackagePath -C $ReleaseDir .
if ($LASTEXITCODE -ne 0) { throw "Update package creation failed." }
$PackageHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $PackagePath).Hash
Write-Host "Package: $PackagePath"
Write-Host "SHA256: $PackageHash"

if ($PackageOnly) {
    Write-Host "Package created. Upload and update steps were skipped." -ForegroundColor Green
    exit 0
}

$Target = "$User@$Server"
$RemotePackage = "$RemoteDir/$ReleaseName.tar.gz"
$RemoteRelease = "$RemoteDir/releases/$ReleaseName"

Write-Host "[4/5] Uploading package to $Target..." -ForegroundColor Cyan
& scp $PackagePath "${Target}:$RemotePackage"
if ($LASTEXITCODE -ne 0) { throw "Package upload failed." }

Write-Host "[5/5] Running safe server update..." -ForegroundColor Cyan
$RemoteCommand = "mkdir -p '$RemoteRelease' && tar -xzf '$RemotePackage' -C '$RemoteRelease' && chmod +x '$RemoteRelease/update-server.sh' && sudo bash '$RemoteRelease/update-server.sh' '$RemoteRelease' '$RemoteDir'"
& ssh -t $Target $RemoteCommand
if ($LASTEXITCODE -ne 0) { throw "Server update failed. Check the remote output and backup path." }

Write-Host "Update succeeded: http://$Server/" -ForegroundColor Green
