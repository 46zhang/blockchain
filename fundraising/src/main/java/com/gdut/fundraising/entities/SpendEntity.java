package com.gdut.fundraising.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpendEntity {
    /**
     * 交易id
     */
    private String txId;

    /**
     *项目发起者的id
     */
    private String formUserId;
    /**
     * 其他机构的地址
     */
    private String toAddress;
    /**
     * 操作的管理员的id
     */
    private String orderOperator;
    /**
     * 订单的钱数
     */
    private long money;
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

}
