package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;


    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //处理DateList
        ArrayList<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        String dateListResult = StringUtils.join(dateList, ",");

        //处理turnoverList
        ArrayList<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnoverList.add(turnover);
        }

        String turnoverListResult = StringUtils.join(turnoverList, ",");


        // 构造返回结果
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(dateListResult);
        turnoverReportVO.setTurnoverList(turnoverListResult);

        return turnoverReportVO;
    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Transactional
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //处理DateList
        ArrayList<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        String dateListResult = StringUtils.join(dateList, ",");

        // 处理总用户数和新增用户数列表
        ArrayList<Integer> totalUserList = new ArrayList<>();
        ArrayList<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime endTime = LocalDateTime.of(date, java.time.LocalTime.MAX);
            LocalDateTime beginTime = LocalDateTime.of(date, java.time.LocalTime.MIN);

            // 获取截止到当天的总用户数
            Integer totalUsers = userMapper.countTotalUsers(endTime);
            totalUserList.add(totalUsers != null ? totalUsers : 0);

            // 获取当天新增用户数
            Integer newUsers = userMapper.countNewUsers(beginTime, endTime);
            newUserList.add(newUsers != null ? newUsers : 0);
        }

        String totalUserListResult = StringUtils.join(totalUserList, ",");
        String newUserListResult = StringUtils.join(newUserList, ",");

        // 构造返回结果
        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(dateListResult);
        userReportVO.setTotalUserList(totalUserListResult);
        userReportVO.setNewUserList(newUserListResult);

        return userReportVO;

    }

    /**
     * 订单数据统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Transactional
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //处理DateList
        ArrayList<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        String dateListResult = StringUtils.join(dateList, ",");

        // 处理每日订单数和有效订单数列表
        ArrayList<Integer> orderCountList = new ArrayList<>();
        ArrayList<Integer> validOrderCountList = new ArrayList<>();

        int totalOrderCount = 0;
        int validOrderCount = 0;

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, java.time.LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, java.time.LocalTime.MAX);

            // 获取当日订单总数
            Integer orderCount = orderMapper.countOrdersByTime(beginTime, endTime);
            orderCount = orderCount != null ? orderCount : 0;
            orderCountList.add(orderCount);
            totalOrderCount += orderCount;

            // 获取当日有效订单数（已完成的订单）
            Integer validCount = orderMapper.countValidOrdersByTime(beginTime, endTime);
            validCount = validCount != null ? validCount : 0;
            validOrderCountList.add(validCount);
            validOrderCount += validCount;
        }

        String orderCountListResult = StringUtils.join(orderCountList, ",");
        String validOrderCountListResult = StringUtils.join(validOrderCountList, ",");

        // 计算订单完成率
        Double orderCompletionRate = totalOrderCount > 0 ?
                (double) validOrderCount / totalOrderCount : 0.0;

        // 构造返回结果
        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(dateListResult)
                .orderCountList(orderCountListResult)
                .validOrderCountList(validOrderCountListResult)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();

        return orderReportVO;
    }

    /**
     * 销量前十通统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, java.time.LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, java.time.LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        //转换数据格式
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        String nameList = StringUtils.join(names, ",");
        String numberList = StringUtils.join(numbers, ",");
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }


}
