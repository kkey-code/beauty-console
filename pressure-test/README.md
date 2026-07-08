# Pressure Tests

This directory keeps reusable JMeter test plans only. Downloaded JMeter binaries,
run logs, result files, pid files, and local tokens are intentionally ignored.

Run the stock deduction test with a valid local auth header:

```powershell
.\apache-jmeter-5.6.3\bin\jmeter.bat -n `
  -t .\stock-deduct-300.jmx `
  -l .\stock-deduct-300.jtl `
  -Jthreads=300 `
  -JinventoryId=900001 `
  -JauthHeader="Bearer <token>"
```
