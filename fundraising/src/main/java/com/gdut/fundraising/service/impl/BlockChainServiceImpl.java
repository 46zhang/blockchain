package com.gdut.fundraising.service.impl;

import com.gdut.fundraising.blockchain.Block;
import com.gdut.fundraising.blockchain.Peer;
import com.gdut.fundraising.blockchain.Service.TransactionService;
import com.gdut.fundraising.blockchain.Transaction;
import com.gdut.fundraising.constant.raft.NodeStatus;
import com.gdut.fundraising.entities.SpendEntity;
import com.gdut.fundraising.entities.raft.BlockChainNode;
import com.gdut.fundraising.exception.BaseException;
import com.gdut.fundraising.manager.impl.RaftLogManagerImpl;
import com.gdut.fundraising.service.BlockChainService;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class BlockChainServiceImpl implements BlockChainService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockChainServiceImpl.class);

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
            LOGGER.error("transaction create fail!!  userId:{},projectId:{},money:{}", userId, projectId, money);
            return false;
        }

        //如果当前节点是leader则直接创建新的区块，如果是其他节点则只需要广播交易过去，由leader节点去创建区块
        Collection<Transaction> collection = peer.getTransactionPool().values();
        List<Transaction> txs = new ArrayList<Transaction>(collection);
        if (blockChainNode.status == NodeStatus.LEADER) {
            return doConsensus(peer);
        } else {
            //广播交易给到leader节点，由它去创建候选区块,并进行共识
            //TODO 返回false 对交易进行回滚
            return blockChainNode.broadcastTransaction(txs);
        }
    }

    /**
     * leader节点做一致性该做的事
     *
     * @param peer
     * @return
     */
    private boolean doConsensus(Peer peer) {
        if (blockChainNode.status != NodeStatus.LEADER) {
            throw new BaseException(400, "区块链共识出错，原因为非节点想代替主节点引导共识");
        }

        //创建区块
        Block block = createBlock(peer);

        //添加区块到区块链中,该方法是线程安全的
        boolean res = peer.getBlockChainService().addBlockToChain(peer, block);

        if (!res) {
            return false;
        }

        //进行共识
        res = blockChainNode.sendLogToOtherNodeForConsistency(block);

        //如果共识成功，则插入给该区块,否则回滚该区块,该区块必须是位于最后一个
        if (!res) {
            //该方法是线程安全的
            peer.getBlockChainService().rollBackBlock(peer, block);
        }

        return res;
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
        Transaction transaction = peer.getTransactionService().createTransaction(peer, spendEntity.getToAddress(),
                spendEntity.getMoney(), spendEntity.getFormUserId(), spendEntity.getProjectId());

        if (transaction == null) {
            LOGGER.error("transaction create fail!!  spendEntity:{}", spendEntity);
            return false;
        }

        //如果当前节点是leader则直接创建新的区块，如果是其他节点则只需要广播交易过去，由leader节点去创建区块
        Collection<Transaction> collection = peer.getTransactionPool().values();
        List<Transaction> txs = new ArrayList<Transaction>(collection);
        if (blockChainNode.status == NodeStatus.LEADER) {
            return doConsensus(peer);
        } else {
            //广播交易给到leader节点，由它去创建候选区块,并进行共识
            //TODO 返回false 对交易进行回滚
            return blockChainNode.broadcastTransaction(txs);
        }
    }

    /**
     * 校验其他节点发送过来的数据是否正确
     *
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
            peer.getTransactionService().addTransaction(peer, t);
        }

        return doConsensus(peer);
    }

    @Override
    public boolean addBlockToChain(Block block) {
        //添加区块到区块链中,该方法是线程安全的
        Peer peer = blockChainNode.getPeer();
        return peer.getBlockChainService().addBlockToChain(peer, block);
    }

    /**
     * 创建区块
     *
     * @param peer
     * @return
     */
    private Block createBlock(Peer peer) {
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
