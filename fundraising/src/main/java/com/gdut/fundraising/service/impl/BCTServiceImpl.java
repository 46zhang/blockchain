package com.gdut.fundraising.service.impl;

import com.gdut.fundraising.blockchain.*;
import com.gdut.fundraising.constant.raft.NodeStatus;
import com.gdut.fundraising.entities.FundFlowEntity;
import com.gdut.fundraising.entities.SpendEntity;
import com.gdut.fundraising.entities.raft.BlockChainNode;
import com.gdut.fundraising.exception.BaseException;
import com.gdut.fundraising.service.BCTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class BCTServiceImpl implements BCTService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BCTServiceImpl.class);

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
                spendEntity.getMoney(), spendEntity.getProjectId(), spendEntity.getFormUserId());

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

//    /**
//     * 获取某个捐款的全部资金使用记录
//     *
//     * @param txId
//     * @return
//     */
//    @Override
//    public List<FundFlowEntity> getTransactionFundFlowByBlockChain(String txId) {
//        List<FundFlowEntity> fundFlowEntities = new ArrayList<>();
//        List<Block> blockChain = blockChainNode.getPeer().getBlockChain();
//        String aimTxId = txId;
//        //需要获取溯源该txId中的交易数据,找出其本身的交易以及其后续的交易
//        //后续交易的意思就是 A交易产生了 utxoA ，B交易使用utxoA作为输入单元，那么B就为A的后续交易，同理可得后续可能会有交易C、D
//        //因此要找出所有的相关交易
//        for (Block block : blockChain) {
//            for (Transaction tx : block.getTxs()) {
//                //同个交易的话直接构建资金流信息
//                if (tx.getId().equals(aimTxId)) {
//                    long sum = 0;
//                    for (Vout vout : tx.getOutList()) {
//                        sum += vout.getMoney();
//                    }
//                    //构建资金流信息
//                    FundFlowEntity fundFlowEntity = buildFundFlowEntity(tx,block);
//
//                    fundFlowEntities.add(fundFlowEntity);
//                } else {
//                    //判断是否有人用txId的vout作为输入单元，有的话相当于就是使用了该txId的钱来做输入
//                    List<Vin> vins = tx.getInList();
//                    for(Vin vin:vins){
//                        if(vin.getToSpent().getTxId().equals(aimTxId)){
//                            //更新新的交易id
//                            aimTxId=tx.getId();
//
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }

    /**
     * 获取所有用户捐款
     *
     * @param userId
     * @return
     */
    @Override
    public List<FundFlowEntity> getUserAllContributionFlow(String userId) {
        List<FundFlowEntity> fundFlowEntities = new ArrayList<>();
        List<Block> blockChain = blockChainNode.getPeer().getBlockChain();

        for (int i = blockChain.size() - 1; i >= 0; --i) {
            Block block = blockChain.get(i);
            //实际上时间复杂度不会很高，因为每个block只有1-2个transaction
            for (Transaction tx : block.getTxs()) {
                //获取用户的所有捐款信息，捐款全部都是创币交易
                if (tx.getFromUserId().equals(userId) && tx.isCoinBase()) {
                    FundFlowEntity fundFlowEntity = buildFundFlowEntity(tx, block);
                    fundFlowEntities.add(fundFlowEntity);
                }
            }
        }
        return fundFlowEntities;
    }

    @Override
    public List<FundFlowEntity> getUserProjectAllFundFlow(String userId, String projectId) {
        List<FundFlowEntity> fundFlowEntities = new ArrayList<>();
        List<Block> blockChain = blockChainNode.getPeer().getBlockChain();

        for (int i = blockChain.size() - 1; i >= 0; --i) {
            Block block = blockChain.get(i);
            //实际上时间复杂度不会很高，因为每个block只有1-2个transaction
            for (Transaction tx : block.getTxs()) {
                if (tx.getFromUserId().equals(userId) && tx.getFormProjectId().equals(projectId)) {
                    FundFlowEntity fundFlowEntity = buildFundFlowEntity(tx, block);
                    fundFlowEntities.add(fundFlowEntity);
                }
            }
        }
        return fundFlowEntities;
    }

    @Override
    public List<FundFlowEntity> getProjectFundFlow(String projectId) {
        List<FundFlowEntity> fundFlowEntities = new ArrayList<>();
        List<Block> blockChain = blockChainNode.getPeer().getBlockChain();

        for (int i = blockChain.size() - 1; i >= 0; --i) {
            Block block = blockChain.get(i);
            //实际上时间复杂度不会很高，因为每个block只有1-2个transaction
            for (Transaction tx : block.getTxs()) {
                if (tx.getFormProjectId().equals(projectId)) {
                    FundFlowEntity fundFlowEntity = buildFundFlowEntity(tx, block);
                    fundFlowEntities.add(fundFlowEntity);
                }
            }
        }
        return fundFlowEntities;
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

    /**
     * 构建资金流实体
     *
     * @param tx
     * @param block
     * @return
     */
    private FundFlowEntity buildFundFlowEntity(Transaction tx, Block block) {
//        long sum = 0;
//        for (Vout vout : tx.getOutList()) {
//            sum += vout.getMoney();
//        }
        //获取输出金额，不包括找零
        long sum = tx.getOutputMoneyNoIncludeChange();

        //构建资金流信息
        FundFlowEntity fundFlowEntity = new FundFlowEntity();
        fundFlowEntity.setBlockHash(block.getHash());
        fundFlowEntity.setMoney(sum);
        fundFlowEntity.setProjectId(tx.getFormProjectId());
        fundFlowEntity.setUserId(tx.getFromUserId());
        fundFlowEntity.setTime(tx.getLockTime());
        fundFlowEntity.setTo(tx.getOutList().get(0).getToAddress());
        fundFlowEntity.setTxId(tx.getId());

        //如果是创币交易不需要设置输入地址
        if (!tx.isCoinBase()) {
            //同一个交易的所有vin地址都一样，所以选第一个就好
            String vinAddress = EccUtil.generateAddress(tx.getInList().get(0).getPublicKey().getEncoded());
            fundFlowEntity.setFrom(vinAddress);
        }

        return fundFlowEntity;
    }


}
