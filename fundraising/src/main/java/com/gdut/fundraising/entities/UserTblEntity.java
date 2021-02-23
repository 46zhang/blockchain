package com.gdut.fundraising.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTblEntity {
    /**
     * 用户id  UUID生成
     * 主键
     */
    private String userId;
    /**
     * 用户电话号码
     */
    private String userPhone;
    /**
     * 用户密码
     */
    private String userPassword;
    /**
     * 用户名字
     */
    private String userName;
    /**
     * 用户地址
     */
    private String userAddress;
    /**
     * 用户银行卡号码
     */
    private String userBank;
    /**
     * 登录后的token
     */
    private String userToken;

    public String notNullRegister(){
        if(userPhone == null){
            return "miss phone";
        }
        if(userPassword == null){
            return "miss password";
        }
        if(userName==null){
            return "miss user name";
        }
        if(userAddress==null){
            return "miss user address";
        }
        if(userBank==null){
            return "miss user bank";
        }

        return null;
    }



}
