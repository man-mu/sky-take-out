package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface OrderDetilMapper {

    /**
     * 批量插入订单明细数据
     * @param orderDetails
     */
    void insertBatch(List<OrderDetail> orderDetails);

    /**
     * 根据订单id查询订单明细
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> listByOrderId(long orderId);

    /**
     * 批量查询订单详情
     * @param orderIds 订单ID列表
     * @return 订单详情列表
     */
    List<OrderDetail> listByOrderIds(List<Long> orderIds);
}
