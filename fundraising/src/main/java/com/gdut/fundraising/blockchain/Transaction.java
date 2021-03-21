package com.gdut.fundraising.blockchain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 交易
 */
public class Transaction {
    /**
     * 输入列表
     */
    private List<Vin> inList;

    /**
     * 输入列表
     */
    private List<Vout> outList;

    /**
     * 手续费,分为单元
     */
    private long fee;

    /**
     * 交易编号
     */
    private String id;

    /**
     * 是否是创币交易
     */
    private boolean isCoinBase;

    /**
     * 项目id
     */
    private String formProjectId;

    /**
     * 用户id
     */
    private String fromUserId;


    private Date lockTime;

    public Transaction() {
        inList = new ArrayList<>();
        outList = new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.valueOf(fee) + isCoinBase + inList.toString() + outList.toString();
    }

    public List<Vin> getInList() {
        return inList;
    }

    public void setInList(List<Vin> inList) {
        this.inList = inList;
    }

    /**
     * 获取输出金额，不包括找零
     * @return
     */
    public Long getOutputMoneyNoIncludeChange(){
        return outList.get(0).getMoney();
    }

    public List<Vout> getOutList() {
        return outList;
    }

    public void setOutList(List<Vout> outList) {
        this.outList = outList;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isCoinBase() {
        return isCoinBase;
    }

    public void setCoinBase(boolean coinBase) {
        isCoinBase = coinBase;
    }

    public Date getLockTime() {
        return lockTime;
    }

    public void setLockTime(Date lockTime) {
        this.lockTime = lockTime;
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
