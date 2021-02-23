package com.gdut.fundraising.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiftTblEntity {
    /**
     * 募捐的用户的id
     */
    private String userId;
    /**
     * 订单的id UUID
     * 主键
     */
    private String giftId;
    /**
     * 募捐的钱数
     */
    private double giftMoney;
    /**
     * 募捐对应项目的id
     */
    private String projectId;
    /**
     * 募捐时间   Date类型
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String giftTime;

    public String getGiftTime(){
        return giftTime.substring(0, 19);
    }

}
