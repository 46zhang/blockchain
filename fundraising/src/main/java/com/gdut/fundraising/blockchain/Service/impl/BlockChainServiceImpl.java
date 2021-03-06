package com.gdut.fundraising.blockchain.Service.impl;

import com.gdut.fundraising.blockchain.*;
import com.gdut.fundraising.blockchain.Service.BlockChainService;
import com.gdut.fundraising.blockchain.Service.MerkleTreeService;
import com.gdut.fundraising.blockchain.Service.TransactionService;
import com.gdut.fundraising.blockchain.Service.UTXOService;
import com.gdut.fundraising.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BlockChainServiceImpl implements BlockChainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockChainServiceImpl.class);

    @Autowired
    MerkleTreeService merkleTreeService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    UTXOService utxoService;


    /**
     * 创建候选区块
     *
     * @param txs
     * @param preBlock
     * @return
     */
    @Override
    public Block createCandidateBlock(List<Transaction> txs, Block preBlock) {
        Block block = new Block();
        block.setPreBlockHash(preBlock == null ? null : preBlock.getHash());
        block.setHeight(preBlock == null ? 0 : preBlock.getHeight() + 1);
        block.setTime(new Date());
        block.setTxs(txs);
        block.setVersion(BlockChainConstant.VERSION);
        block.setMerkleRootHash(merkleTreeService.getMerkleRoot(txs));
        block.setHash(Sha256Util.doubleSHA256(block.getHeader()));
        return block;
    }

    /**
     * 校验区块
     *
     * @param peer
     * @param block
     * @return
     */
    @Override
    public boolean verifyBlock(Peer peer, Block block) {
        //验证该区块中是否存在多重支付
        if (doublePayCheck(block.getTxs())) {
            return false;
        }

        //验证每一笔交易
        for (Transaction transaction : block.getTxs()) {
            if (!transactionService.verifyTransaction(peer, transaction)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 添加区块到区块链
     *
     * @param peer
     * @param block
     * @return
     */
    @Override
    public synchronized boolean addBlockToChain(Peer peer, Block block) {
        List<Block> blockChain = peer.getBlockChain();

        //计算该区块应该位于哪个高度
        long height = calculateBlockHeight(blockChain, block);
        //说明应该添加到最后一块
        //加锁互斥，保证线程安全

        if (blockChain.size() == height) {
            blockChain.add(block);
            //更新UTXOSet和交易池
            receiveBlockAtEndOfChain(peer, block.getTxs());
        } else {
            LOGGER.error("区块位置计算出错 block: {} height:{}", block, height);
            //其他位置return false
            return false;
        }

        return true;
    }

    /**
     * 回滚最后一个区块
     *
     * @param peer
     * @param block
     */
    @Override
    public synchronized void rollBackBlock(Peer peer, Block block) {
        List<Block> blockChain = peer.getBlockChain();
        //计算该区块应该位于哪个高度
        long height = calculateBlockHeight(blockChain, block);
        //移除最后一块
        if (height == blockChain.size() - 1) {
            //删除掉最后的区块.加锁互斥，保证线程安全
            blockChain.remove(blockChain.size() - 1);
            rollBack(peer);

        } else {
            LOGGER.error("Block:{},height:{}", block, height);
            throw new BaseException(400, "不是最后一个区块，无法回滚!!!");
        }
    }

    /**
     * 更新区块链上最后一个区块，将原有的替换掉
     *
     * @param peer
     * @param txs
     */
    private void updateBlockAtEndOfChain(Peer peer, List<Transaction> txs) {
        rollBack(peer);
        receiveBlockAtEndOfChain(peer, txs);
    }

    /**
     * 回滚数据
     *
     * @param peer
     */
    private void rollBack(Peer peer) {
        ConcurrentHashMap<Pointer, UTXO> utxoHashMap = peer.getUTXOHashMap();
        //恢复交易池中的交易
        peer.getTransactionPool().putAll(peer.getTransactionPoolBackup());

        //删除掉原来最后区块的输出单元封装的UTXO
        utxoService.deleteUTXOByPointer(utxoHashMap, peer.getPointersFromVout());

        //重新添加原来最后区块删除的输入单元
        utxoHashMap.putAll(peer.getUTXOHashMapBackup());

        //清空备份缓存
        peer.setTransactionPoolBackup(new ConcurrentHashMap<>());
        peer.setUTXOHashMapBackup(new ConcurrentHashMap<>());
        peer.setPointersFromVout(new CopyOnWriteArrayList<>());
    }

    /**
     * 添加最后一块区块到链上
     *
     * @param peer
     * @param txs
     */
    private void receiveBlockAtEndOfChain(Peer peer, List<Transaction> txs) {
        ConcurrentHashMap<Pointer, UTXO> utxoHashMap = peer.getUTXOHashMap();
        ConcurrentHashMap<String, Transaction> pool = peer.getTransactionPool();

        //更新的utxo集合,删除已经消费了的UTXO块并将这些被删除的utxo集合进行备份
        peer.setUTXOHashMapBackup(transactionService.removeSpentUTXOFromTxs(peer.getUTXOHashMap(), txs));
        //找到输出单元的定位指针,用于备份
        CopyOnWriteArrayList<Pointer> pointers = transactionService.findVoutPointerFromTxs(txs);
        peer.setPointersFromVout(pointers);
        //将utxo添加到集合中去
        transactionService.addUTXOFromTxsToMap(utxoHashMap, txs);
        //删除交易池中的数据
        ConcurrentHashMap<String, Transaction> poolBackup = transactionService.
                removeTransactionFromTransactionPool(pool, txs);
        //备份数据，用于回滚
        peer.setTransactionPoolBackup(poolBackup);
    }

    /**
     * 计算当前区块应该位于哪个位置
     *
     * @param blockChain
     * @param block
     * @return
     */
    private long calculateBlockHeight(List<Block> blockChain, Block block) {
        if (blockChain.size() == 0) {
            return 0;
        } else if (blockChain.size() == 1 && blockChain.get(0).getHash().equals(block.getHash())) {
            //第一个区块
            return 0;
        }
        String preHash = block.getPreBlockHash();
        for (int i = blockChain.size() - 1; i >= 0; --i) {
            if (blockChain.get(i).getHash().equals(preHash)) {
                return blockChain.get(i).getHeight() + 1;
            }
        }
        //TODO 如果整条链都找不到该哈希值可以抛一个异常
        return -1;
    }

    /**
     * 验证是否存在多重支付
     *
     * @param txs
     * @return
     */
    private boolean doublePayCheck(List<Transaction> txs) {
        List<Vin> vinList = new ArrayList<>();
        Set<Vin> vinSet = new HashSet<>();

        for (Transaction tx : txs) {
            vinList.addAll(tx.getInList());
            vinSet.addAll(tx.getInList());
        }

        //如果不存在多重支付，那么用set去重的结果肯定跟list的长度一样
        return vinList.size() != vinSet.size();
    }

    public void setMerkleTreeService(MerkleTreeService merkleTreeService) {
        this.merkleTreeService = merkleTreeService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setUtxoService(UTXOService utxoService) {
        this.utxoService = utxoService;
    }
}
