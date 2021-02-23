package com.gdut.fundraising.blockchain;

/**
 * 交易定位指针
 */
public class Pointer {
    /**
     * 交易id
     */
    private String txId;
    /**
     * 交易的第n个输出单元
     */
    private int n;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime * (txId == null ? 1 : txId.hashCode());
        return result * prime + n;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pointer)) {
            return false;
        }
        Pointer p = (Pointer) obj;
        //必须交易id相等 并且第n个输出单元也相等
        return (p.txId.equals(txId) && p.n == n);
    }

    public Pointer() {

    }

    public Pointer(String txId, int n) {
        this.txId = txId;
        this.n = n;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public static void main(String[] args) {
        String a = "123wffsftfwrgerg";
        String b = "123wffsftfwrgerg";
        System.out.println(a.hashCode());
        System.out.println(b.hashCode());
    }
}
