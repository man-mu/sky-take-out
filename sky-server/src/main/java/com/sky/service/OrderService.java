package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * B端订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult searchOrder(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各状态订单统计
     * @return
     */
    OrderStatisticsVO getStatistics();

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    OrderVO getOrderDetails(String id);

    /**
     * 接单
     * @param id
     */
    void confirmOrder(Long id);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    void rejectOrder(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    void cancelOrder(OrdersCancelDTO ordersCancelDTO);

    /**
     * 派送订单
     * @param id
     */
    void deliveryOrder(long id);

    /**
     * 完成订单
     * @param orderId
     */
    void completeOrder(Long orderId);

    /**
     * C端历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult searchHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    void repetition(Long id);

    void userCancelById(Long id);
}
