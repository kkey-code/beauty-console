package com.wkr.storeserver;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storepojo.dto.ServiceProjectInventoryPageQueryDTO;
import com.wkr.storepojo.entity.InventorySku;
import com.wkr.storepojo.entity.ServiceProject;
import com.wkr.storepojo.entity.ServiceProjectInventory;
import com.wkr.storepojo.vo.ServiceProjectInventoryVO;
import com.wkr.storeserver.controller.ServiceProjectInventoryController;
import com.wkr.storeserver.service.InventorySkuService;
import com.wkr.storeserver.service.ServiceProjectInventoryService;
import com.wkr.storeserver.service.ServiceProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServiceProjectInventoryBatchQueryTest {

    @Mock
    private ServiceProjectInventoryService relationService;
    @Mock
    private ServiceProjectService serviceProjectService;
    @Mock
    private InventorySkuService inventorySkuService;

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listLoadsProjectAndInventoryRelationsInBatches() {
        ServiceProjectInventory first = relation(1L, 101L, 201L);
        ServiceProjectInventory second = relation(2L, 102L, 202L);
        Page<ServiceProjectInventory> page = new Page<>(1, 10);
        page.setRecords(List.of(first, second));
        page.setTotal(2);

        doReturn(page).when(relationService).page(any(Page.class), any(Wrapper.class));
        doReturn(List.of(project(101L, "项目A"), project(102L, "项目B")))
                .when(serviceProjectService).listByIds(anyCollection());
        doReturn(List.of(inventory(201L, "耗材A"), inventory(202L, "耗材B")))
                .when(inventorySkuService).listByIds(anyCollection());

        ServiceProjectInventoryController controller = new ServiceProjectInventoryController(
                relationService,
                serviceProjectService,
                inventorySkuService);
        Result<PageResult<ServiceProjectInventoryVO>> result = controller.list(
                new ServiceProjectInventoryPageQueryDTO());

        assertThat(result.getData().getRecords()).hasSize(2);
        List<ServiceProjectInventoryVO> records = (List<ServiceProjectInventoryVO>) result.getData().getRecords();
        assertThat(records.get(0).getServiceProjectName()).isEqualTo("项目A");
        assertThat(records.get(0).getInventoryName()).isEqualTo("耗材A");
        verify(serviceProjectService).listByIds(anyCollection());
        verify(inventorySkuService).listByIds(anyCollection());
        verify(serviceProjectService, never()).getById(any());
        verify(inventorySkuService, never()).getById(any());
    }

    private ServiceProjectInventory relation(Long id, Long projectId, Long inventoryId) {
        ServiceProjectInventory relation = new ServiceProjectInventory();
        relation.setId(id);
        relation.setServiceProjectId(projectId);
        relation.setInventoryId(inventoryId);
        relation.setStatus(1);
        return relation;
    }

    private ServiceProject project(Long id, String name) {
        ServiceProject project = new ServiceProject();
        project.setId(id);
        project.setName(name);
        return project;
    }

    private InventorySku inventory(Long id, String name) {
        InventorySku inventory = new InventorySku();
        inventory.setId(id);
        inventory.setName(name);
        inventory.setUnit("份");
        return inventory;
    }
}
