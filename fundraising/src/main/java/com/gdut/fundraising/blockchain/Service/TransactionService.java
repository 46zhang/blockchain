package com.gdut.fundraising.blockchain.Service;

import com.gdut.fundraising.blockchain.Peer;
import com.gdut.fundraising.blockchain.Pointer;
import com.gdut.fundraising.blockchain.Transaction;
import com.gdut.fundraising.blockchain.UTXO;

import java.util.HashMap;
import java.util.List;

/**
 * 交易服务
 */
public interface TransactionService {
    /**
     * 创建交易
     *
     * @param peer
     * @param toAddress
     * @param money
     * @return
     */
    Transaction createTransaction(Peer peer, String toAddress, long money);


    /**
     * 验证交易
     *
     * @param transaction
     * @return
     */
    public boolean verifyTransaction(Peer peer, Transaction transaction);

    /**
     * 创币服务
     *
     * @param peer
     * @param toAddress
     * @param money
     * @return
     */
    Transaction createCoinBaseTransaction(Peer peer, String toAddress, long money);

    /**
     * 从交易中找到对应的输入单元的指针
     *
     * @param txs
     * @return
     */
    List<Pointer> findVinPointerFromTxs(List<Transaction> txs);

    /**
     * 移除消费过的utxo
     *
     * @param utxoHashMap
     * @param txs
     * @return
     */
    HashMap<Pointer, UTXO> removeSpentUTXOFromTxs(HashMap<Pointer, UTXO> utxoHashMap, List<Transaction> txs);

    /**
     * 将交易中的vout封装为utxo，插入到到utxoMap
     *
     * @param utxoHashMap
     * @param txs
     */
    void addUTXOFromTxsToMap(HashMap<Pointer, UTXO> utxoHashMap, List<Transaction> txs);

    /**
     * 在交易列表中找到输出单元的的定位指针列表
     *
     * @param txs
     * @return
     */
    List<Pointer> findVoutPointerFromTxs(List<Transaction> txs);

    /**
     * 从交易池中移除交易
     * @param pool
     * @param txs
     * @return
     */
    HashMap<String, Transaction> removeTransactionFromTransactionPool(HashMap<String, Transaction> pool,
                                                                      List<Transaction> txs);
}
