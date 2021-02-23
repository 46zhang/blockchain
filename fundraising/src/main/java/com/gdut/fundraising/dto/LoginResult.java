package com.gdut.fundraising.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResult {
    /**
     * 判断是否为管理员
     * 0-用户
     * 1-管理员
     * 其他-无效
     */
    private int userManage;
    /**
     * 用户的电话号码
     */
    private String userPhone;
    /**
     * 用户的名字
     */
    private String userName;
    /**
     * 用户的地址
     */
    private String userAddress;
    /**
     * 用户的银行卡号
     */
    private String userBank;
    /**
     * 登陆成功的token
     */
    private String token;
}
