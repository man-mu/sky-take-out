package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;              // 员工唯一标识符（主键ID）

    private String username;      // 员工登录用户名

    private String name;          // 员工姓名

    private String password;      // 员工登录密码（加密后）

    private String phone;         // 员工手机号码

    private String sex;           // 员工性别

    private String idNumber;      // 员工身份证号码

    private Integer status;       // 账号状态（启用/禁用等）

    private LocalDateTime createTime;  // 记录创建时间

    private LocalDateTime updateTime;  // 记录最后更新时间

    private Long createUser;      // 创建此员工记录的用户ID

    private Long updateUser;      // 最后修改此员工记录的用户ID


}
