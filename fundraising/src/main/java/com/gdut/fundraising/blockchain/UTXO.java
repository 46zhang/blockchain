package com.gdut.fundraising.blockchain;

import java.util.Map;

/**
 * 未消费交易输出模块
 */
public class UTXO {
    /**
     * 未消费输入在区块链中的地址
     */
    private Pointer pointer;

    /**
     * 输出单元
     */
    Vout vout;

    /**
     * 是否已经被消费
     */
    private boolean isSpent;

    /**
     * 是否被确认
     */
    private boolean isConfirmed;

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


    public String encodeString() {
        return pointer.getTxId() + pointer.getN();
    }

    @Override
    public String toString() {
        return "UTXO{" +
                "pointer='" + pointer + '\'' +
                ",vout=" + vout + '\'' +
                ",isSpent='" + isSpent + '\'' +
                ",isConfirmed='" + isConfirmed + '\'' +
                ",formProjectId='" + formProjectId + '\'' +
                ",fromUserId='" + fromUserId + '\'' +
                ",isCoinBase='" + isCoinBase +
                '}';
    }

    public Pointer getPointer() {
        return pointer;
    }

    public void setPointer(Pointer pointer) {
        this.pointer = pointer;
    }

    public Vout getVout() {
        return vout;
    }

    public void setVout(Vout vout) {
        this.vout = vout;
    }


    public boolean isSpent() {
        return isSpent;
    }

    public void setSpent(boolean spent) {
        isSpent = spent;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

    public boolean isCoinBase() {
        return isCoinBase;
    }

    public void setCoinBase(boolean coinBase) {
        isCoinBase = coinBase;
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
