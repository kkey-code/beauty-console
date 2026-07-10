package com.wkr.storeserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wkr.storecommon.common.PageResult;
import com.wkr.storecommon.common.Result;
import com.wkr.storecommon.exception.BusinessException;
import com.wkr.storepojo.dto.ServiceOrderDTO;
import com.wkr.storepojo.dto.ServiceOrderItemDTO;
import com.wkr.storepojo.dto.ServiceOrderPageQueryDTO;
import com.wkr.storepojo.entity.Appointment;
import com.wkr.storepojo.entity.AppointmentItem;
import com.wkr.storepojo.entity.CustomerProfile;
import com.wkr.storepojo.entity.ServiceOrder;
import com.wkr.storepojo.entity.ServiceOrderItem;
import com.wkr.storepojo.enums.DebtStatusEnum;
import com.wkr.storepojo.enums.OrderStatusEnum;
import com.wkr.storepojo.enums.OrderTypeEnum;
import com.wkr.storepojo.enums.PayStatusEnum;
import com.wkr.storepojo.vo.ServiceOrderVO;
import com.wkr.storeserver.service.AppointmentItemService;
import com.wkr.storeserver.service.AppointmentService;
import com.wkr.storeserver.service.CustomerProfileService;
import com.wkr.storeserver.service.ServiceOrderItemService;
import com.wkr.storeserver.service.ServiceOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务订单接口控制器，负责接收管理端请求、校验参数并调用服务层完成业务处理。
 */
@Slf4j
@RestController
@RequestMapping("/admin/service-orders")
@Api(tags = "订单相关接口")
public class ServiceOrderController {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final ServiceOrderService serviceOrderService;
    private final CustomerProfileService customerProfileService;
    private final ServiceOrderItemService serviceOrderItemService;
    private final AppointmentService appointmentService;
    private final AppointmentItemService appointmentItemService;

    public ServiceOrderController(
            ServiceOrderService serviceOrderService,
            CustomerProfileService customerProfileService,
            ServiceOrderItemService serviceOrderItemService,
            AppointmentService appointmentService,
            AppointmentItemService appointmentItemService) {
        this.serviceOrderService = serviceOrderService;
        this.customerProfileService = customerProfileService;
        this.serviceOrderItemService = serviceOrderItemService;
        this.appointmentService = appointmentService;
        this.appointmentItemService = appointmentItemService;
    }

    @GetMapping
    @ApiOperation("订单列表")
    public Result<PageResult<ServiceOrderVO>> list(ServiceOrderPageQueryDTO dto) {
        LambdaQueryWrapper<ServiceOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getOrderNo()), ServiceOrder::getOrderNo, dto.getOrderNo())
                .eq(StringUtils.hasText(dto.getOrderType()), ServiceOrder::getOrderType, dto.getOrderType())
                .eq(dto.getPayStatus() != null, ServiceOrder::getPayStatus, dto.getPayStatus())
                .eq(dto.getDebtStatus() != null, ServiceOrder::getDebtStatus, dto.getDebtStatus())
                .eq(dto.getOrderStatus() != null, ServiceOrder::getOrderStatus, dto.getOrderStatus())
                .ge(dto.getBeginTime() != null, ServiceOrder::getCreateTime, dto.getBeginTime())
                .le(dto.getEndTime() != null, ServiceOrder::getCreateTime, dto.getEndTime());

        if (StringUtils.hasText(dto.getCustomerName())) {
            List<Long> customerIds = customerProfileService.list(new LambdaQueryWrapper<CustomerProfile>()
                            .like(CustomerProfile::getName, dto.getCustomerName()))
                    .stream()
                    .map(CustomerProfile::getId)
                    .toList();
            if (customerIds.isEmpty()) {
                return Result.success(new PageResult<>(0, List.of()));
            }
            wrapper.in(ServiceOrder::getCustomerId, customerIds);
        }

        Page<ServiceOrder> page = new Page<>(dto.getPage(), dto.getPageSize());
        IPage<ServiceOrder> pageResult = serviceOrderService.page(page, wrapper);

        List<ServiceOrderVO> list = new ArrayList<>();
        for (ServiceOrder serviceOrder : pageResult.getRecords()) {
            list.add(toVO(serviceOrder, true));
        }
        return Result.success(new PageResult<>(pageResult.getTotal(), list));
    }

    @GetMapping("/{id}")
    @ApiOperation("根据订单 id 查询订单")
    public Result<ServiceOrderVO> getById(@PathVariable("id") Long id) {
        ServiceOrder serviceOrder = getExistingOrder(id);
        return Result.success(toVO(serviceOrder, true));
    }

    @PostMapping
    @ApiOperation("添加订单")
    @Transactional
    public Result<Long> create(@Valid @RequestBody ServiceOrderDTO serviceOrderDTO) {
        if (serviceOrderDTO.getItems() == null || serviceOrderDTO.getItems().isEmpty()) {
            throw new BusinessException("订单至少需要一条项目明细");
        }

        ServiceOrder serviceOrder = new ServiceOrder();
        BeanUtils.copyProperties(serviceOrderDTO, serviceOrder);
        fillOrderDefaults(serviceOrder);
        validateOrderAmount(serviceOrder, summarizeDTOItems(serviceOrderDTO.getItems()));

        boolean saved = serviceOrderService.save(serviceOrder);
        if (!saved) {
            throw new BusinessException("添加订单失败");
        }

        saveOrderItems(serviceOrder.getId(), serviceOrderDTO.getItems());
        return Result.success(serviceOrder.getId());
    }

    @PostMapping("/from-appointment/{appointmentId}")
    @ApiOperation("从预约生成订单")
    @Transactional
    public Result<Long> createFromAppointment(@PathVariable("appointmentId") Long appointmentId) {
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
        List<ServiceOrderItem> orderItems = new ArrayList<>();
        for (AppointmentItem appointmentItem : appointmentItems) {
            orderItems.add(buildOrderItemFromAppointmentItem(serviceOrder.getId(), appointment, appointmentItem));
        }
        validateOrderAmount(serviceOrder, summarizeOrderItems(orderItems));

        boolean saved = serviceOrderService.save(serviceOrder);
        if (!saved) {
            throw new BusinessException("从预约生成订单失败");
        }

        for (ServiceOrderItem orderItem : orderItems) {
            orderItem.setOrderId(serviceOrder.getId());
        }
        serviceOrderItemService.saveBatch(orderItems);

        return Result.success(serviceOrder.getId());
    }

    @PutMapping("/{id}")
    @ApiOperation("修改订单")
    @Transactional
    public Result<Boolean> update(@PathVariable("id") Long id, @Valid @RequestBody ServiceOrderDTO serviceOrderDTO) {
        getExistingOrder(id);

        ServiceOrder serviceOrder = new ServiceOrder();
        BeanUtils.copyProperties(serviceOrderDTO, serviceOrder);
        serviceOrder.setId(id);
        serviceOrder.setUpdateTime(LocalDateTime.now());

        if (serviceOrderDTO.getItems() != null) {
            validateOrderAmount(serviceOrder, summarizeDTOItems(serviceOrderDTO.getItems()));
        } else {
            validateOrderAmount(serviceOrder, null);
        }

        boolean orderUpdated = serviceOrderService.updateById(serviceOrder);
        boolean itemsUpdated = true;
        if (serviceOrderDTO.getItems() != null) {
            serviceOrderItemService.remove(new LambdaQueryWrapper<ServiceOrderItem>()
                    .eq(ServiceOrderItem::getOrderId, id));
            itemsUpdated = saveOrderItems(id, serviceOrderDTO.getItems());
        }

        return Result.success(orderUpdated && itemsUpdated);
    }

    @PatchMapping("/{id}/cancel")
    @ApiOperation("取消订单")
    public Result<Boolean> cancel(@PathVariable("id") Long id) {
        ServiceOrder serviceOrder = getExistingOrder(id);
        OrderStatusEnum.validate(serviceOrder.getOrderStatus());

        serviceOrder.setOrderStatus(OrderStatusEnum.CANCELED.getCode());
        serviceOrder.setUpdateTime(LocalDateTime.now());
        return Result.success(serviceOrderService.updateById(serviceOrder));
    }

    @PatchMapping("/{id}/finish")
    @ApiOperation("完成订单")
    public Result<Boolean> finish(@PathVariable("id") Long id) {
        return Result.success(serviceOrderService.finish(id));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除订单")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        ServiceOrder serviceOrder = getExistingOrder(id);
        OrderStatusEnum.validate(serviceOrder.getOrderStatus());

        return Result.success(serviceOrderService.removeById(id));
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
            Long orderId,
            Appointment appointment,
            AppointmentItem appointmentItem) {
        BigDecimal price = valueOrZero(appointmentItem.getPrice());

        ServiceOrderItem serviceOrderItem = new ServiceOrderItem();
        serviceOrderItem.setOrderId(orderId);
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

    /**
     * 订单金额强校验：
     * 1. 主表金额必须满足：应收 = 原价 - 优惠，欠款 = 应收 - 已收。
     * 2. 如果传了明细，主表原价/优惠/应收必须等于明细合计。
     * 3. 支付状态和欠款状态只跟金额联动，不改变订单服务状态。
     */
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
            serviceOrderVO.setItems(serviceOrderService.getOrderItemsByOrderId(serviceOrder.getId()));
            serviceOrderVO.setPayments(serviceOrderService.getPaymentRecordByOrderId(serviceOrder.getId()));
        }
        return serviceOrderVO;
    }

    private ServiceOrder getExistingOrder(Long id) {
        ServiceOrder serviceOrder = serviceOrderService.getById(id);
        if (serviceOrder == null) {
            throw new BusinessException("订单不存在");
        }
        return serviceOrder;
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(ORDER_NO_FORMATTER);
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
