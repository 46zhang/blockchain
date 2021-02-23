package com.gdut.fundraising.blockchain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 区块链节点
 */
public class Peer {

    /**
     * 区块链
     */
    private List<Block> blockChain;

    /**
     * utxo 哈希集
     */
    private HashMap<Pointer, UTXO> UTXOHashMap;

//    /**
//     * 用户自身的utxo，方便计算余额
//     */
//    private HashMap<Pointer, UTXO> ownUTXOHashMap;


    /**
     * 钱包
     */
    private Wallet wallet;

    /**
     * 孤立交易池
     */
    private HashMap<String, Transaction> orphanPool;

    /**
     * 交易池
     */
    private HashMap<String, Transaction> transactionPool;


    /**
     * utxo 哈希集备份
     */
    private HashMap<Pointer, UTXO> UTXOHashMapBackup;

    /**
     * 用户自身的utxo备份
     */
    private HashMap<Pointer, UTXO> ownUTXOHashMapBackup;

    /**
     * 交易池备份
     */
    private HashMap<String, Transaction> transactionPoolBackup;

    /**
     * 输出单元的定位指针，用于数据回滚
     */
    private List<Pointer> pointersFromVout;

    /**
     * 输出单元封装成的utxo，用于数据回滚
     */
    private List<UTXO> utxosFromVout;

    public Peer() {
        UTXOHashMap = new HashMap<>();
        orphanPool = new HashMap<>();
        transactionPool = new HashMap<>();
        blockChain = new ArrayList<>();
    }

    //TODO 注意要看看是否存在未确认的utxo
    public long getBalance(String address) {
        long money = 0;
        for (UTXO utxo : UTXOHashMap.values()) {
            //必须找到地址一致且金额足够的utxo
            if (utxo.isSpent() || !utxo.getVout().getToAddress().equals(address)) {
                continue;
            }
            money += utxo.getVout().getMoney();
        }
        return money;
    }


    public List<Block> getBlockChain() {
        return blockChain;
    }

    public void setBlockChain(List<Block> blockChain) {
        this.blockChain = blockChain;
    }

    public HashMap<String, Transaction> getTransactionPool() {
        return transactionPool;
    }

    public void setTransactionPool(HashMap<String, Transaction> transactionPool) {
        this.transactionPool = transactionPool;
    }

    public HashMap<String, Transaction> getOrphanPool() {
        return orphanPool;
    }

    public void setOrphanPool(HashMap<String, Transaction> orphanPool) {
        this.orphanPool = orphanPool;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public HashMap<Pointer, UTXO> getUTXOHashMap() {
        return UTXOHashMap;
    }

    public void setUTXOHashMap(HashMap<Pointer, UTXO> UTXOHashMap) {
        this.UTXOHashMap = UTXOHashMap;
    }


    public HashMap<Pointer, UTXO> getUTXOHashMapBackup() {
        return UTXOHashMapBackup;
    }

    public void setUTXOHashMapBackup(HashMap<Pointer, UTXO> UTXOHashMapBackup) {
        this.UTXOHashMapBackup = UTXOHashMapBackup;
    }

    public HashMap<Pointer, UTXO> getOwnUTXOHashMapBackup() {
        return ownUTXOHashMapBackup;
    }

    public void setOwnUTXOHashMapBackup(HashMap<Pointer, UTXO> ownUTXOHashMapBackup) {
        this.ownUTXOHashMapBackup = ownUTXOHashMapBackup;
    }

    public HashMap<String, Transaction> getTransactionPoolBackup() {
        return transactionPoolBackup;
    }

    public void setTransactionPoolBackup(HashMap<String, Transaction> transactionPoolBackup) {
        this.transactionPoolBackup = transactionPoolBackup;
    }

    public List<Pointer> getPointersFromVout() {
        return pointersFromVout;
    }

    public void setPointersFromVout(List<Pointer> pointersFromVout) {
        this.pointersFromVout = pointersFromVout;
    }
}
