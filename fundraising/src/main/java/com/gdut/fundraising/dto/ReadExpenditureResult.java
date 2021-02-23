package com.gdut.fundraising.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadExpenditureResult {

    /**
     *项目发起者的id
     */
    private String formUserId;
    /**
     * 收到管理员钱的用户的名字
     */
    private String toUserName;
    /**
     * 收到管理员钱的用户的id
     */
    private String toUserId;
    /**
     * 操作的管理员的id
     */
    private String orderOperator;
    /**
     * 订单的id UUID
     * 主键
     */
    private String orderId;
    /**
     * 订单的钱数
     */
    private double orderMoney;
    /**
     * 订单对应的项目id
     */
    private String projectId;
    /**
     * 订单的时间  Date类型
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String orderTime;
    /**
     * 订单的描述
     */
    private String orderExplain;


    public String getOrderTime(){
        return orderTime.substring(0, 19);
    }
}
