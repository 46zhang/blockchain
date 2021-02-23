package com.gdut.fundraising.blockchain;

import java.util.Date;
import java.util.List;

/**
 * 区块
 */
public class Block {
    /**
     * 版本号
     */
    String version;

    /**
     * 前一区块的哈希值
     */
    String preBlockHash;

    /**
     * 梅克尔树哈希值
     */
    String merkleRootHash;

    /**
     * 该区块的哈希值
     */
    String hash;

    /**
     * 交易集合
     */
    List<Transaction> txs;

    /**
     * 区块诞生的时间
     */
    Date time;

    /**
     * 当前区高
     */
    long height;

    /**
     * 获取区块头
     * @return
     */
    public String getHeader() {
        return version + preBlockHash + merkleRootHash + time.toString();
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPreBlockHash() {
        return preBlockHash;
    }

    public void setPreBlockHash(String preBlockHash) {
        this.preBlockHash = preBlockHash;
    }

    public String getMerkleRootHash() {
        return merkleRootHash;
    }

    public void setMerkleRootHash(String merkleRootHash) {
        this.merkleRootHash = merkleRootHash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(List<Transaction> txs) {
        this.txs = txs;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
