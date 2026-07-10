package com.wkr.storeserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkr.storecommon.common.BaseContext;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.InventoryStockLogDTO;
import com.wkr.storepojo.dto.ServiceOrderDTO;
import com.wkr.storepojo.dto.ServiceOrderItemDTO;
import com.wkr.storepojo.dto.ServiceOrderPageQueryDTO;
import com.wkr.storepojo.entity.Appointment;
import com.wkr.storepojo.entity.AppointmentItem;
import com.wkr.storepojo.entity.CustomerProfile;
import com.wkr.storepojo.entity.InventoryStockLog;
import com.wkr.storepojo.entity.PaymentRecord;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storepojo.entity.ServiceOrderItem;
import com.wkr.storepojo.entity.ServiceProjectInventory;
import com.wkr.storepojo.entity.StaffMember;
import com.wkr.storepojo.enums.DebtStatusEnum;
import com.wkr.storepojo.enums.InventoryChangeTypeEnum;
import com.wkr.storepojo.enums.OrderStatusEnum;
import com.wkr.storepojo.enums.OrderTypeEnum;
import com.wkr.storepojo.enums.PayStatusEnum;
import com.wkr.storepojo.vo.PaymentRecordVO;
import com.wkr.storepojo.vo.ServiceOrderItemVO;
import com.wkr.storepojo.vo.ServiceOrderVO;
import com.wkr.storeserver.mapper.ServiceOrderMapper;
import com.wkr.storeserver.service.AppointmentItemService;
import com.wkr.storeserver.service.AppointmentService;
import com.wkr.storeserver.service.CustomerProfileService;
import com.wkr.storeserver.service.InventoryStockLogService;
import com.wkr.storeserver.service.PaymentRecordService;
import com.wkr.storeserver.service.ServiceOrderItemService;
import com.wkr.storeserver.service.ServiceOrderService;
import com.wkr.storeserver.service.ServiceProjectInventoryService;
import com.wkr.storeserver.service.StaffMemberService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author kkey
* @description 针对表【service_order(订单主表)】的数据库操作Service实现
* @createDate 2026-07-03 21:10:41
*/
@Service
public class ServiceOrderServiceImpl extends ServiceImpl<ServiceOrderMapper, ServiceOrder>
    implements ServiceOrderService{

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final ServiceOrderMapper serviceOrderMapper;
    private final ServiceOrderItemService serviceOrderItemService;
    private final PaymentRecordService paymentRecordService;
    private final StaffMemberService staffMemberService;
    private final ServiceProjectInventoryService serviceProjectInventoryService;
    private final InventoryStockLogService inventoryStockLogService;
    private final CustomerProfileService customerProfileService;
    private final AppointmentService appointmentService;
    private final AppointmentItemService appointmentItemService;

    public ServiceOrderServiceImpl(
            ServiceOrderMapper serviceOrderMapper,
            ServiceOrderItemService serviceOrderItemService,
            PaymentRecordService paymentRecordService,
            StaffMemberService staffMemberService,
            ServiceProjectInventoryService serviceProjectInventoryService,
            InventoryStockLogService inventoryStockLogService,
            CustomerProfileService customerProfileService,
            AppointmentService appointmentService,
            AppointmentItemService appointmentItemService) {
        this.serviceOrderMapper = serviceOrderMapper;
        this.serviceOrderItemService = serviceOrderItemService;
        this.paymentRecordService = paymentRecordService;
        this.staffMemberService = staffMemberService;
        this.serviceProjectInventoryService = serviceProjectInventoryService;
        this.inventoryStockLogService = inventoryStockLogService;
        this.customerProfileService = customerProfileService;
        this.appointmentService = appointmentService;
        this.appointmentItemService = appointmentItemService;
    }

    @Override
    public PageResult<ServiceOrderVO> pageOrders(ServiceOrderPageQueryDTO dto) {
        LambdaQueryWrapper<ServiceOrder> wrapper = buildPageWrapper(dto);

        Page<ServiceOrder> page = new Page<>(dto.getPage(), dto.getPageSize());
        IPage<ServiceOrder> pageResult = page(page, wrapper);

        List<ServiceOrderVO> list = new ArrayList<>();
        for (ServiceOrder serviceOrder : pageResult.getRecords()) {
            list.add(toVO(serviceOrder, true));
        }
        return new PageResult<>(pageResult.getTotal(), list);
    }

    @Override
    public ServiceOrderVO getDetail(Long id) {
        return toVO(getExistingOrder(id), true);
    }

    @Override
    @Transactional
    public Long createOrder(ServiceOrderDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("订单至少需要一条项目明细");
        }

        ServiceOrder serviceOrder = new ServiceOrder();
        BeanUtils.copyProperties(dto, serviceOrder);
        fillOrderDefaults(serviceOrder);
        validateOrderAmount(serviceOrder, summarizeDTOItems(dto.getItems()));

        if (!save(serviceOrder)) {
            throw new BusinessException("添加订单失败");
        }

        saveOrderItems(serviceOrder.getId(), dto.getItems());
        return serviceOrder.getId();
    }

    @Override
    @Transactional
    public Long createFromAppointment(Long appointmentId) {
        Appointment appointment = appointmentService.getById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }

        List<AppointmentItem> appointmentItems = appointmentItemService.list(
                new LambdaQueryWrapper<AppointmentItem>().eq(AppointmentItem::getAppointmentId, appointmentId));
        if (appointmentItems.isEmpty()) {
            throw new BusinessException("预约没有项目明细，不能生成订单");
        }

        ServiceOrder serviceOrder = buildOrderFromAppointment(appointment, appointmentItems);
        List<ServiceOrderItem> orderItems = appointmentItems.stream()
                .map(appointmentItem -> buildOrderItemFromAppointmentItem(appointment, appointmentItem))
                .collect(Collectors.toList());
        validateOrderAmount(serviceOrder, summarizeOrderItems(orderItems));

        if (!save(serviceOrder)) {
            throw new BusinessException("从预约生成订单失败");
        }

        for (ServiceOrderItem orderItem : orderItems) {
            orderItem.setOrderId(serviceOrder.getId());
        }
        serviceOrderItemService.saveBatch(orderItems);
        return serviceOrder.getId();
    }

    @Override
    @Transactional
    public boolean updateOrder(Long id, ServiceOrderDTO dto) {
        getExistingOrder(id);

        ServiceOrder serviceOrder = new ServiceOrder();
        BeanUtils.copyProperties(dto, serviceOrder);
        serviceOrder.setId(id);
        serviceOrder.setUpdateTime(LocalDateTime.now());

        if (dto.getItems() != null) {
            validateOrderAmount(serviceOrder, summarizeDTOItems(dto.getItems()));
        } else {
            validateOrderAmount(serviceOrder, null);
        }

        boolean orderUpdated = updateById(serviceOrder);
        boolean itemsUpdated = true;
        if (dto.getItems() != null) {
            serviceOrderItemService.remove(new LambdaQueryWrapper<ServiceOrderItem>()
                    .eq(ServiceOrderItem::getOrderId, id));
            itemsUpdated = saveOrderItems(id, dto.getItems());
        }

        return orderUpdated && itemsUpdated;
    }

    @Override
    @Transactional
    public boolean cancel(Long id) {
        ServiceOrder serviceOrder = lockOrder(id);

        OrderStatusEnum.validate(serviceOrder.getOrderStatus());
        if (OrderStatusEnum.CANCELED.matches(serviceOrder.getOrderStatus())) {
            return true;
        }
        if (OrderStatusEnum.COMPLETED.matches(serviceOrder.getOrderStatus())) {
            rollbackInventoryForCompletedOrder(serviceOrder);
        }

        ServiceOrder updateOrder = new ServiceOrder();
        updateOrder.setId(id);
        updateOrder.setOrderStatus(OrderStatusEnum.CANCELED.getCode());
        updateOrder.setUpdateTime(LocalDateTime.now());
        if (serviceOrderMapper.updateById(updateOrder) != 1) {
            throw new BusinessException("取消订单失败");
        }
        return true;
    }

    @Override
    public List<ServiceOrderItemVO> getOrderItemsByOrderId(Long orderId) {
        LambdaQueryWrapper<ServiceOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceOrderItem::getOrderId, orderId);
        List<ServiceOrderItem> itemList = serviceOrderItemService.list(wrapper);
        Set<Long> staffIds = itemList.stream()
                .map(ServiceOrderItem::getStaffId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, String> staffNameById = loadStaffNames(staffIds);

        return itemList.stream()
                .map(item -> {
                    ServiceOrderItemVO itemVO = new ServiceOrderItemVO();
                    BeanUtils.copyProperties(item, itemVO);
                    Long staffId = item.getStaffId();
                    if (staffId != null) {
                        itemVO.setStaffName(staffNameById.get(staffId));
                    }
                    return itemVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentRecordVO> getPaymentRecordByOrderId(Long orderId) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getOrderId, orderId);
        List<PaymentRecord> paymentRecordList = paymentRecordService.list(wrapper);

        return paymentRecordList.stream()
                .map(item -> {
                    PaymentRecordVO paymentRecordVO = new PaymentRecordVO();
                    BeanUtils.copyProperties(item, paymentRecordVO);
                    return paymentRecordVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean finish(Long id) {
        ServiceOrder serviceOrder = lockOrder(id);

        OrderStatusEnum.validate(serviceOrder.getOrderStatus());
        if (OrderStatusEnum.COMPLETED.matches(serviceOrder.getOrderStatus())) {
            return true;
        }
        if (OrderStatusEnum.CANCELED.matches(serviceOrder.getOrderStatus())) {
            throw new BusinessException("已取消订单不能完成");
        }

        List<ServiceOrderItem> orderItems = listOrderItems(id);
        deductInventoryForServiceOrder(serviceOrder, orderItems);

        ServiceOrder updateOrder = new ServiceOrder();
        updateOrder.setId(id);
        updateOrder.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
        updateOrder.setUpdateTime(LocalDateTime.now());
        if (serviceOrderMapper.updateById(updateOrder) != 1) {
            throw new BusinessException("完成订单失败");
        }
        return true;
    }

    @Override
    public boolean deleteOrder(Long id) {
        ServiceOrder serviceOrder = getExistingOrder(id);
        OrderStatusEnum.validate(serviceOrder.getOrderStatus());
        return removeById(id);
    }

    private LambdaQueryWrapper<ServiceOrder> buildPageWrapper(ServiceOrderPageQueryDTO dto) {
        LambdaQueryWrapper<ServiceOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getOrderNo()), ServiceOrder::getOrderNo, dto.getOrderNo())
                .eq(StringUtils.hasText(dto.getOrderType()), ServiceOrder::getOrderType, dto.getOrderType())
                .eq(dto.getPayStatus() != null, ServiceOrder::getPayStatus, dto.getPayStatus())
                .eq(dto.getDebtStatus() != null, ServiceOrder::getDebtStatus, dto.getDebtStatus())
                .eq(dto.getOrderStatus() != null, ServiceOrder::getOrderStatus, dto.getOrderStatus())
                .ge(dto.getBeginTime() != null, ServiceOrder::getCreateTime, dto.getBeginTime())
                .le(dto.getEndTime() != null, ServiceOrder::getCreateTime, dto.getEndTime())
                .orderByDesc(ServiceOrder::getCreateTime)
                .orderByDesc(ServiceOrder::getId);

        if (StringUtils.hasText(dto.getCustomerName())) {
            List<Long> customerIds = customerProfileService.list(new LambdaQueryWrapper<CustomerProfile>()
                            .like(CustomerProfile::getName, dto.getCustomerName()))
                    .stream()
                    .map(CustomerProfile::getId)
                    .toList();
            if (customerIds.isEmpty()) {
                wrapper.apply("1 = 0");
            } else {
                wrapper.in(ServiceOrder::getCustomerId, customerIds);
            }
        }
        return wrapper;
    }

    private ServiceOrder getExistingOrder(Long id) {
        ServiceOrder serviceOrder = getById(id);
        if (serviceOrder == null) {
            throw new BusinessException("订单不存在");
        }
        return serviceOrder;
    }

    private ServiceOrder lockOrder(Long id) {
        ServiceOrder serviceOrder = serviceOrderMapper.selectOne(
                new LambdaQueryWrapper<ServiceOrder>()
                        .eq(ServiceOrder::getId, id)
                        .last("FOR UPDATE"));
        if (serviceOrder == null) {
            throw new BusinessException("订单不存在");
        }
        return serviceOrder;
    }

    private List<ServiceOrderItem> listOrderItems(Long orderId) {
        return serviceOrderItemService.list(
                new LambdaQueryWrapper<ServiceOrderItem>().eq(ServiceOrderItem::getOrderId, orderId));
    }

    private boolean saveOrderItems(Long orderId, List<ServiceOrderItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return true;
        }

        List<ServiceOrderItem> itemList = items.stream()
                .map(item -> {
                    ServiceOrderItem serviceOrderItem = new ServiceOrderItem();
                    BeanUtils.copyProperties(item, serviceOrderItem);
                    serviceOrderItem.setId(null);
                    serviceOrderItem.setOrderId(orderId);
                    serviceOrderItem.setCreateTime(LocalDateTime.now());
                    return serviceOrderItem;
                })
                .toList();
        return serviceOrderItemService.saveBatch(itemList);
    }

    private ServiceOrder buildOrderFromAppointment(Appointment appointment, List<AppointmentItem> appointmentItems) {
        BigDecimal originalAmount = appointmentItems.stream()
                .map(AppointmentItem::getPrice)
                .map(this::valueOrZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal receivableAmount = originalAmount.subtract(discountAmount);
        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal debtAmount = receivableAmount;

        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setOrderNo(generateOrderNo());
        serviceOrder.setAppointmentId(appointment.getId());
        serviceOrder.setCustomerId(appointment.getCustomerId());
        serviceOrder.setOrderType(OrderTypeEnum.SERVICE.getCode());
        serviceOrder.setOriginalAmount(originalAmount);
        serviceOrder.setDiscountAmount(discountAmount);
        serviceOrder.setReceivableAmount(receivableAmount);
        serviceOrder.setPaidAmount(paidAmount);
        serviceOrder.setDebtAmount(debtAmount);
        serviceOrder.setDebtStatus(DebtStatusEnum.INSTALLMENT.getCode());
        serviceOrder.setPayStatus(PayStatusEnum.UNPAID.getCode());
        serviceOrder.setOrderStatus(OrderStatusEnum.PENDING.getCode());
        serviceOrder.setRemark(appointment.getRemark());
        serviceOrder.setCreateTime(LocalDateTime.now());
        serviceOrder.setUpdateTime(LocalDateTime.now());
        return serviceOrder;
    }

    private ServiceOrderItem buildOrderItemFromAppointmentItem(
            Appointment appointment,
            AppointmentItem appointmentItem) {
        BigDecimal price = valueOrZero(appointmentItem.getPrice());

        ServiceOrderItem serviceOrderItem = new ServiceOrderItem();
        serviceOrderItem.setServiceProjectId(appointmentItem.getServiceProjectId());
        serviceOrderItem.setServiceName(appointmentItem.getServiceName());
        serviceOrderItem.setUnitPrice(price);
        serviceOrderItem.setQuantity(BigDecimal.ONE);
        serviceOrderItem.setDiscountAmount(BigDecimal.ZERO);
        serviceOrderItem.setActualAmount(price);
        serviceOrderItem.setStaffId(appointmentItem.getStaffId() != null
                ? appointmentItem.getStaffId()
                : appointment.getStaffId());
        serviceOrderItem.setCreateTime(LocalDateTime.now());
        return serviceOrderItem;
    }

    private void fillOrderDefaults(ServiceOrder serviceOrder) {
        if (!StringUtils.hasText(serviceOrder.getOrderNo())) {
            serviceOrder.setOrderNo(generateOrderNo());
        }
        if (serviceOrder.getDiscountAmount() == null) {
            serviceOrder.setDiscountAmount(BigDecimal.ZERO);
        }
        if (serviceOrder.getPaidAmount() == null) {
            serviceOrder.setPaidAmount(BigDecimal.ZERO);
        }
        if (serviceOrder.getDebtAmount() == null && serviceOrder.getReceivableAmount() != null) {
            serviceOrder.setDebtAmount(serviceOrder.getReceivableAmount().subtract(serviceOrder.getPaidAmount()));
        }
        if (serviceOrder.getPayStatus() == null) {
            serviceOrder.setPayStatus(PayStatusEnum.UNPAID.getCode());
        }
        if (serviceOrder.getDebtStatus() == null) {
            serviceOrder.setDebtStatus(valueOrZero(serviceOrder.getDebtAmount()).compareTo(BigDecimal.ZERO) > 0
                    ? DebtStatusEnum.INSTALLMENT.getCode()
                    : DebtStatusEnum.NONE.getCode());
        }
        if (serviceOrder.getOrderStatus() == null) {
            serviceOrder.setOrderStatus(OrderStatusEnum.PENDING.getCode());
        }
        if (serviceOrder.getCreateTime() == null) {
            serviceOrder.setCreateTime(LocalDateTime.now());
        }
        serviceOrder.setUpdateTime(LocalDateTime.now());
    }

    private void validateOrderAmount(ServiceOrder serviceOrder, OrderAmountSummary itemSummary) {
        List<String> errors = new ArrayList<>();

        BigDecimal originalAmount = valueOrZero(serviceOrder.getOriginalAmount());
        BigDecimal discountAmount = valueOrZero(serviceOrder.getDiscountAmount());
        BigDecimal receivableAmount = valueOrZero(serviceOrder.getReceivableAmount());
        BigDecimal paidAmount = valueOrZero(serviceOrder.getPaidAmount());
        BigDecimal debtAmount = valueOrZero(serviceOrder.getDebtAmount());

        if (hasNegative(originalAmount, discountAmount, receivableAmount, paidAmount, debtAmount)) {
            errors.add("订单金额不能为负数");
        }
        if (discountAmount.compareTo(originalAmount) > 0) {
            errors.add("优惠金额不能大于原价金额");
        }

        BigDecimal calculatedReceivable = originalAmount.subtract(discountAmount);
        if (receivableAmount.compareTo(calculatedReceivable) != 0) {
            errors.add("应收金额必须等于原价金额 - 优惠金额");
        }

        if (paidAmount.compareTo(receivableAmount) > 0) {
            errors.add("已收金额不能大于应收金额");
        }

        BigDecimal calculatedDebt = receivableAmount.subtract(paidAmount);
        if (debtAmount.compareTo(calculatedDebt) != 0) {
            errors.add("欠款金额必须等于应收金额 - 已收金额");
        }

        if (itemSummary != null) {
            if (originalAmount.compareTo(itemSummary.originalAmount()) != 0) {
                errors.add("订单原价金额必须等于明细原价合计");
            }
            if (discountAmount.compareTo(itemSummary.discountAmount()) != 0) {
                errors.add("订单优惠金额必须等于明细优惠合计");
            }
            if (receivableAmount.compareTo(itemSummary.receivableAmount()) != 0) {
                errors.add("订单应收金额必须等于明细应收合计");
            }
        }

        Integer expectedPayStatus = payStatusOf(paidAmount, receivableAmount);
        if (serviceOrder.getPayStatus() != null && !serviceOrder.getPayStatus().equals(expectedPayStatus)) {
            errors.add("支付状态和已收/应收金额不一致");
        }
        serviceOrder.setPayStatus(expectedPayStatus);

        Integer expectedDebtStatus = debtAmount.compareTo(BigDecimal.ZERO) > 0
                ? DebtStatusEnum.INSTALLMENT.getCode()
                : DebtStatusEnum.NONE.getCode();
        if (serviceOrder.getDebtStatus() != null && !serviceOrder.getDebtStatus().equals(expectedDebtStatus)) {
            errors.add("欠款状态和欠款金额不一致");
        }
        serviceOrder.setDebtStatus(expectedDebtStatus);

        if (!errors.isEmpty()) {
            throw new BusinessException("订单金额校验失败：" + String.join("；", errors));
        }
    }

    private OrderAmountSummary summarizeDTOItems(List<ServiceOrderItemDTO> items) {
        BigDecimal originalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal receivableAmount = BigDecimal.ZERO;

        for (ServiceOrderItemDTO item : items) {
            BigDecimal quantity = valueOrZero(item.getQuantity());
            BigDecimal unitPrice = valueOrZero(item.getUnitPrice());
            BigDecimal itemDiscount = valueOrZero(item.getDiscountAmount());
            BigDecimal itemActualAmount = valueOrZero(item.getActualAmount());
            BigDecimal itemOriginalAmount = unitPrice.multiply(quantity);

            if (itemOriginalAmount.subtract(itemDiscount).compareTo(itemActualAmount) != 0) {
                throw new BusinessException("订单明细金额错误：明细应收必须等于单价 * 数量 - 明细优惠");
            }

            originalAmount = originalAmount.add(itemOriginalAmount);
            discountAmount = discountAmount.add(itemDiscount);
            receivableAmount = receivableAmount.add(itemActualAmount);
        }
        return new OrderAmountSummary(originalAmount, discountAmount, receivableAmount);
    }

    private OrderAmountSummary summarizeOrderItems(List<ServiceOrderItem> items) {
        BigDecimal originalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal receivableAmount = BigDecimal.ZERO;

        for (ServiceOrderItem item : items) {
            BigDecimal quantity = valueOrZero(item.getQuantity());
            BigDecimal unitPrice = valueOrZero(item.getUnitPrice());
            BigDecimal itemDiscount = valueOrZero(item.getDiscountAmount());
            BigDecimal itemActualAmount = valueOrZero(item.getActualAmount());

            originalAmount = originalAmount.add(unitPrice.multiply(quantity));
            discountAmount = discountAmount.add(itemDiscount);
            receivableAmount = receivableAmount.add(itemActualAmount);
        }
        return new OrderAmountSummary(originalAmount, discountAmount, receivableAmount);
    }

    private void deductInventoryForServiceOrder(ServiceOrder serviceOrder, List<ServiceOrderItem> orderItems) {
        if (!OrderTypeEnum.SERVICE.matches(serviceOrder.getOrderType())) {
            return;
        }
        Map<Long, BigDecimal> quantityByInventory = calculateInventoryQuantities(orderItems);
        for (Map.Entry<Long, BigDecimal> entry : quantityByInventory.entrySet()) {
            recordOrderStockChange(
                    serviceOrder,
                    entry.getKey(),
                    entry.getValue(),
                    InventoryChangeTypeEnum.STOCK_OUT,
                    "订单完成自动扣库存，订单号：");
        }
    }

    private void rollbackInventoryForCompletedOrder(ServiceOrder serviceOrder) {
        if (!OrderTypeEnum.SERVICE.matches(serviceOrder.getOrderType())) {
            return;
        }
        List<InventoryStockLog> stockOutLogs = inventoryStockLogService.list(
                new LambdaQueryWrapper<InventoryStockLog>()
                        .eq(InventoryStockLog::getRelatedOrderId, serviceOrder.getId())
                        .eq(InventoryStockLog::getChangeType, InventoryChangeTypeEnum.STOCK_OUT.getCode()));
        if (stockOutLogs.isEmpty()) {
            throw new BusinessException("订单没有可回滚的库存流水，不能取消已完成订单");
        }

        Map<Long, BigDecimal> quantityByInventory = stockOutLogs.stream()
                .filter(log -> log.getInventoryId() != null)
                .collect(Collectors.toMap(
                        InventoryStockLog::getInventoryId,
                        log -> positiveQuantity(log.getChangeQuantity()),
                        BigDecimal::add,
                        LinkedHashMap::new));
        if (quantityByInventory.isEmpty()) {
            throw new BusinessException("订单没有可回滚的库存流水，不能取消已完成订单");
        }

        for (Map.Entry<Long, BigDecimal> entry : quantityByInventory.entrySet()) {
            recordOrderStockChange(
                    serviceOrder,
                    entry.getKey(),
                    entry.getValue(),
                    InventoryChangeTypeEnum.RETURN,
                    "订单取消回滚库存，订单号：");
        }
    }

    private Map<Long, BigDecimal> calculateInventoryQuantities(List<ServiceOrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new BusinessException("订单没有项目明细，不能完成服务订单");
        }

        Set<Long> serviceProjectIds = orderItems.stream()
                .map(ServiceOrderItem::getServiceProjectId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (serviceProjectIds.isEmpty()) {
            throw new BusinessException("订单项目缺少服务项目，不能完成服务订单");
        }

        List<ServiceProjectInventory> relations = serviceProjectInventoryService.listActiveByProjectIds(serviceProjectIds);
        if (relations.isEmpty()) {
            throw new BusinessException("服务项目未配置耗材关系，不能完成订单");
        }

        Map<Long, List<ServiceProjectInventory>> relationsByProject = relations.stream()
                .collect(Collectors.groupingBy(ServiceProjectInventory::getServiceProjectId));
        Set<Long> missingProjectIds = new LinkedHashSet<>(serviceProjectIds);
        missingProjectIds.removeAll(relationsByProject.keySet());
        if (!missingProjectIds.isEmpty()) {
            throw new BusinessException("服务项目未配置耗材关系，不能完成订单");
        }

        Map<Long, BigDecimal> quantityByInventory = new LinkedHashMap<>();
        for (ServiceOrderItem orderItem : orderItems) {
            List<ServiceProjectInventory> projectRelations = relationsByProject.get(orderItem.getServiceProjectId());
            if (projectRelations == null || projectRelations.isEmpty()) {
                continue;
            }

            BigDecimal itemQuantity = positiveQuantity(orderItem.getQuantity());
            for (ServiceProjectInventory relation : projectRelations) {
                BigDecimal consumeQuantity = positiveQuantity(relation.getConsumeQuantity());
                BigDecimal stockOutQuantity = consumeQuantity.multiply(itemQuantity);
                quantityByInventory.merge(relation.getInventoryId(), stockOutQuantity, BigDecimal::add);
            }
        }
        if (quantityByInventory.isEmpty()) {
            throw new BusinessException("服务项目未配置有效耗材关系，不能完成订单");
        }
        return quantityByInventory;
    }

    private void recordOrderStockChange(
            ServiceOrder serviceOrder,
            Long inventoryId,
            BigDecimal quantity,
            InventoryChangeTypeEnum changeType,
            String remarkPrefix) {
        InventoryStockLogDTO dto = new InventoryStockLogDTO();
        dto.setInventoryId(inventoryId);
        dto.setChangeType(changeType.getCode());
        dto.setChangeQuantity(quantity);
        dto.setRelatedOrderId(serviceOrder.getId());
        dto.setOperatorId(BaseContext.getCurrentId());
        dto.setRemark(remarkPrefix + serviceOrder.getOrderNo());
        inventoryStockLogService.recordStockChange(dto, changeType.getCode());
    }

    private ServiceOrderVO toVO(ServiceOrder serviceOrder, boolean includeChildren) {
        ServiceOrderVO serviceOrderVO = new ServiceOrderVO();
        BeanUtils.copyProperties(serviceOrder, serviceOrderVO);

        CustomerProfile customerProfile = customerProfileService.getById(serviceOrder.getCustomerId());
        if (customerProfile != null) {
            serviceOrderVO.setCustomerName(customerProfile.getName());
            serviceOrderVO.setCustomerPhone(customerProfile.getPhone());
        }

        serviceOrderVO.setOrderTypeName(OrderTypeEnum.labelOf(serviceOrder.getOrderType()));
        serviceOrderVO.setOrderStatusName(OrderStatusEnum.labelOf(serviceOrder.getOrderStatus()));
        serviceOrderVO.setDebtStatusName(DebtStatusEnum.labelOf(serviceOrder.getDebtStatus()));
        serviceOrderVO.setPayStatusName(PayStatusEnum.labelOf(serviceOrder.getPayStatus()));

        if (includeChildren) {
            serviceOrderVO.setItems(getOrderItemsByOrderId(serviceOrder.getId()));
            serviceOrderVO.setPayments(getPaymentRecordByOrderId(serviceOrder.getId()));
        }
        return serviceOrderVO;
    }

    private Map<Long, String> loadStaffNames(Set<Long> staffIds) {
        if (staffIds.isEmpty()) {
            return Map.of();
        }
        Collection<StaffMember> staffMembers = staffMemberService.listByIds(staffIds);
        if (staffMembers == null || staffMembers.isEmpty()) {
            return Map.of();
        }
        return staffMembers.stream()
                .filter(staffMember -> staffMember.getId() != null)
                .collect(Collectors.toMap(
                        StaffMember::getId,
                        StaffMember::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(ORDER_NO_FORMATTER);
    }

    private Integer payStatusOf(BigDecimal paidAmount, BigDecimal receivableAmount) {
        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            return PayStatusEnum.UNPAID.getCode();
        }
        if (paidAmount.compareTo(receivableAmount) < 0) {
            return PayStatusEnum.PARTIAL_PAID.getCode();
        }
        return PayStatusEnum.PAID.getCode();
    }

    private boolean hasNegative(BigDecimal... values) {
        for (BigDecimal value : values) {
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal positiveQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("扣库存数量必须大于0");
        }
        return quantity;
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record OrderAmountSummary(
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal receivableAmount) {
    }
}
