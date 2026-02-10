package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * B端订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各状态订单数量统计
     * @return
     */
    OrderStatisticsVO getStatistics();

    /**
     * 查询订单详情
     * @param orderId
     * @return
     */
    @Select("select * from orders where id = #{orderId}")
    OrderVO getById(long orderId);

    /**
     * 接单
     * @param id
     */
    @Update("update orders set status = 3 where id = #{id}")
    void confirmOrder(Long id);

    /**
     * 拒单
     * @param id
     */
    @Update("update orders set status = 4 where id = #{id}")
    void deliveryOrder(Long id);

    /**
     * 完成订单
     * @param orderId
     */
    @Update("update orders set status = 5 where id = #{orderId}")
    void completeOrder(Long orderId);

    /**
     * C端历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<OrderVO> historyOrdersQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询付款超时订单
     * @param status
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getTimeOutOrders(Integer status, LocalDateTime time);
}
