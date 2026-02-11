package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
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

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;


    /**
     * 营业额统计
     *
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
}
