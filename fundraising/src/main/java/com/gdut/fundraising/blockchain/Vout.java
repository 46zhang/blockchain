package com.gdut.fundraising.blockchain;

/**
 * 交易输出单元
 */
public class Vout {
    /**
     * 交易输出地址
     */
    private String toAddress;

    /**
     * 交易金额
     */
    private long money;

    /**
     * 项目id
     */
    private String formProjectId;

    /**
     * 用户id
     */
    private String fromUserId;

    @Override
    public String toString() {
        return toAddress+money;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public String getFormProjectId() {
        return formProjectId;
    }

    public void setFormProjectId(String formProjectId) {
        this.formProjectId = formProjectId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }
}
