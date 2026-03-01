package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderService orderService;

    /**
     * 处理支付超时订单
     */
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void ProcessTimeOutOrder() {

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        //处理超时订单
        List<Orders> timeOutOrders = orderMapper.getTimeOutOrders(Orders.PENDING_PAYMENT, time);
        if (timeOutOrders != null && !timeOutOrders.isEmpty()){
            for (Orders timeOutOrder : timeOutOrders) {
                timeOutOrder.setStatus(Orders.CANCELLED);
                timeOutOrder.setCancelReason("订单超时，自动取消");
                timeOutOrder.setCancelTime(LocalDateTime.now());
                orderMapper.update(timeOutOrder);
            }
        }
    }

    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processCompletedOrder() {
        //查询距离现在1小时的订单
        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        List<Orders> ordersList = orderMapper.getTimeOutOrders(Orders.DELIVERY_IN_PROGRESS, time);

        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orderService.completeOrder(orders.getId());
            }
        }
    }


}
