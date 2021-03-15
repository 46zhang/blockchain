package com.gdut.fundraising.service.impl;

import com.gdut.fundraising.blockchain.Block;
import com.gdut.fundraising.blockchain.Peer;
import com.gdut.fundraising.blockchain.Transaction;
import com.gdut.fundraising.constant.raft.NodeStatus;
import com.gdut.fundraising.entities.SpendEntity;
import com.gdut.fundraising.entities.raft.BlockChainNode;
import com.gdut.fundraising.service.BlockChainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class BlockChainServiceImpl implements BlockChainService {
    @Autowired
    private BlockChainNode blockChainNode;

    /**
     * 募捐
     *
     * @param userId
     * @param projectId
     * @param money
     * @return
     */
    @Override
    public boolean contribution(String userId, String projectId, long money) {
        Peer peer = blockChainNode.getPeer();
        //创币交易
        Transaction transaction = peer.getTransactionService().createCoinBaseTransaction(peer, peer.getWallet().getAddress(),
                userId, projectId, money);
        if (transaction == null) {
            return false;
        }
        //如果当前节点是leader则直接创建新的区块，如果是其他节点则只需要广播交易过去，由leader节点去创建区块
        Collection<Transaction> collection = peer.getTransactionPool().values();
        List<Transaction> txs = new ArrayList<Transaction>(collection);
        if (blockChainNode.status == NodeStatus.LEADER) {
            //TODO 返回false 对区块进行回滚
            return blockChainNode.sendLogToOtherNodeForConsistency(createBlock(peer));
        } else {
            //广播交易给到leader节点，由它去创建候选区块,并进行共识
            //TODO 返回false 对交易进行回滚
            return blockChainNode.broadcastTransaction(txs);
        }
    }

    /**
     * 管理员花费金额
     *
     * @param spendEntity
     * @return
     */
    @Override
    public boolean useMoney(SpendEntity spendEntity) {
        Peer peer = blockChainNode.getPeer();
        //创币交易
        Transaction transaction = peer.getTransactionService().createTransaction(peer, spendEntity.getToAddress(), spendEntity.getMoney());
        if (transaction == null) {
            return false;
        }

        //如果当前节点是leader则直接创建新的区块，如果是其他节点则只需要广播交易过去，由leader节点去创建区块
        Collection<Transaction> collection = peer.getTransactionPool().values();
        List<Transaction> txs = new ArrayList<Transaction>(collection);
        if (blockChainNode.status == NodeStatus.LEADER) {
            //TODO 返回false 对区块进行回滚
            return blockChainNode.sendLogToOtherNodeForConsistency(createBlock(peer));
        } else {
            //广播交易给到leader节点，由它去创建候选区块,并进行共识
            //TODO 返回false 对交易进行回滚
            return blockChainNode.broadcastTransaction(txs);
        }
        return false;
    }

    /**
     * 校验其他节点发送过来的数据是否正确
     * @param txs
     * @return
     */
    @Override
    public boolean verifyTransactionsFromOtherNode(List<Transaction> txs) {
        Peer peer = blockChainNode.getPeer();
        for (Transaction t : txs) {
            boolean res = peer.getTransactionService().verifyTransaction(peer, t);
            if (!res) {
                return false;
            }
        }

        //添加到交易池
        for (Transaction t : txs) {
            peer.getTransactionService().addTransaction(peer,t);
        }

        return blockChainNode.sendLogToOtherNodeForConsistency(createBlock(peer));
    }

    /**
     * 创建区块
     * @param peer
     * @return
     */
    private Block createBlock(Peer peer){
        //获取上一个区块
        Block preBlock = peer.getBlockChain().size() == 0 ? null :
                peer.getBlockChain().get(peer.getBlockChain().size() - 1);
        //如果当前节点是leader则直接创建新的区块，如果是其他节点则只需要广播交易过去，由leader节点去创建区块
        Collection<Transaction> collection = peer.getTransactionPool().values();
        List<Transaction> txs = new ArrayList<Transaction>(collection);
        Block block = peer.getBlockChainService().createCandidateBlock(txs, preBlock);
        return block;
    }


}
