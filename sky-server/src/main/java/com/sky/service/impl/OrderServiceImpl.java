package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShopingCartMapper shopingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetilMapper orderDetilMapper;
    @Autowired
    private UserMapper userMapper;


    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @ApiOperation("用户下单")
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //校验数据
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shopingCartMapper.list(shoppingCart);

        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //在订单表中插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setUserId(userId);
        orders.setUserName(addressBook.getConsignee());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());

        orderMapper.insert(orders);

        //在订单明细表中插入n条数据
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetilMapper.insertBatch(orderDetails);

        //清空用户购物车数据
        shopingCartMapper.deleteByUserId(userId);

        //封装结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Transactional
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        /*JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );
*/
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    @Override
    @Transactional
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * B端订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult searchOrder(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO orderStatisticsVO = orderMapper.getStatistics();
        return orderStatisticsVO;
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Transactional
    @Override
    public OrderVO getOrderDetails(String id) {
        //根据订单id查询order表,封装父类order的属性
        long orderId = Long.parseLong(id);
        OrderVO orderVO = orderMapper.getById(orderId);

        //根据订单id查询order_detail表,封装子类order_detail的属性
        List<OrderDetail> orderDetailList = orderDetilMapper.listByOrderId(orderId);
        orderVO.setOrderDetailList(orderDetailList);

        //处理orderDishes
        StringBuilder orderDishes = new StringBuilder();
        for (int i = 0; i < orderDetailList.size(); i++) {
            OrderDetail detail = orderDetailList.get(i);
            orderDishes.append(detail.getName());
            if (i < orderDetailList.size() - 1) {
                orderDishes.append(",");
            }
        }
        orderVO.setOrderDishes(orderDishes.toString());

        // 查询并设置地址信息
        if (orderVO.getAddressBookId() != null) {
            AddressBook addressBook = addressBookMapper.getById(orderVO.getAddressBookId());
            if (addressBook != null) {
                // 构建完整地址字符串
                StringBuilder fullAddress = new StringBuilder();
                if (addressBook.getProvinceName() != null) {
                    fullAddress.append(addressBook.getProvinceName());
                }
                if (addressBook.getCityName() != null) {
                    fullAddress.append(addressBook.getCityName());
                }
                if (addressBook.getDistrictName() != null) {
                    fullAddress.append(addressBook.getDistrictName());
                }
                if (addressBook.getDetail() != null) {
                    fullAddress.append(addressBook.getDetail());
                }
                orderVO.setAddress(fullAddress.toString());

                // 设置收货人信息
                orderVO.setConsignee(addressBook.getConsignee());
                orderVO.setPhone(addressBook.getPhone());
            }
        }

        return orderVO;
    }

    @Override
    public void confirmOrder(Long id) {
        orderMapper.confirmOrder(id);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Transactional
    @Override
    public void rejectOrder(OrdersRejectionDTO ordersRejectionDTO) {
        //先查询原订单信息
        Orders originalOrder = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (originalOrder == null || !originalOrder.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //在原有基础上修改需要的字段
        originalOrder.setStatus(Orders.CANCELLED);  // 修改订单状态
        originalOrder.setRejectionReason(ordersRejectionDTO.getRejectionReason());  // 补全拒单原因

        //使用一条SQL修改订单状态和拒单原因
        orderMapper.update(originalOrder);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Transactional
    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        //先查询原订单信息
        Orders originalOrder = orderMapper.getById(ordersCancelDTO.getId());

        //在原有基础上修改需要的字段
        originalOrder.setStatus(Orders.CANCELLED);  // 修改订单状态
        originalOrder.setCancelReason(ordersCancelDTO.getCancelReason());  // 补全取消原因

        //使用一条SQL修改订单状态和拒单原因
        orderMapper.update(originalOrder);

        //逝去的自动退款功能(悲)
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void deliveryOrder(long id) {
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.deliveryOrder(id);
    }

    /**
     * 完成订单
     * @param orderId
     */
    @Override
    public void completeOrder(Long orderId) {
        Orders ordersDB = orderMapper.getById(orderId);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.completeOrder(orderId);
    }

    /**
     * C端历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
//    @Override
//    public PageResult searchHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
//        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
//        Page<OrderVO> page = orderMapper.historyOrdersQuery(ordersPageQueryDTO);
//
//        List<OrderVO> orders = page.getResult();
//        for (OrderVO order : orders) {
//            //获取每个订单的订单详情
//            order.setOrderDetailList(orderDetilMapper.listByOrderId(order.getId()));
//        }
//        return new PageResult(page.getTotal(), orders);
//    }

    @Transactional
    @Override
    public PageResult searchHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.historyOrdersQuery(ordersPageQueryDTO);

        List<OrderVO> orders = page.getResult();

        // 优化
        if (!orders.isEmpty()) {
            // 批量获取所有订单ID
            List<Long> orderIds = orders.stream()
                    .map(OrderVO::getId)
                    .collect(Collectors.toList());

            // 一次性查询所有订单详情
            Map<Long, List<OrderDetail>> detailMap = orderDetilMapper.listByOrderIds(orderIds)
                    .stream()
                    .collect(Collectors.groupingBy(OrderDetail::getOrderId));

            // 批量设置订单详情
            for (OrderVO order : orders) {
                order.setOrderDetailList(detailMap.getOrDefault(order.getId(), new ArrayList<>()));
            }
        }

        return new PageResult(page.getTotal(), orders);
    }


    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    public void repetition(Long id) {
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetilMapper.listByOrderId(id);

        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车对象批量添加到数据库
        shopingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    public void userCancelById(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


}
