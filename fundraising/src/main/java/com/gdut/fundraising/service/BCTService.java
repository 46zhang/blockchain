package com.gdut.fundraising.service;

import com.gdut.fundraising.blockchain.Block;
import com.gdut.fundraising.blockchain.Transaction;
import com.gdut.fundraising.dto.NodeQueryResult;
import com.gdut.fundraising.entities.FundFlowEntity;
import com.gdut.fundraising.entities.SpendEntity;

import java.util.List;

public interface BCTService {
    /**
     * 用户(userId)给某个项目(projectId)募捐金钱(数额: money)
     * @param userId
     * @param projectId
     * @param money
     * @return
     */
    boolean contribution(String userId,String projectId,long money);

    /**
     * 花费金额
     * @param spendEntity
     */
    boolean useMoney(SpendEntity spendEntity);

    /**
     * 校验交易
     * @param txs
     * @return
     */
    boolean verifyTransactionsFromOtherNode(List<Transaction> txs);

    /**
     * 添加区块到区块链
     * @param block
     * @return
     */
    boolean addBlockToChain(Block block);

//    /**
//     * txId 来查询某笔捐款的所有资金流动记录
//     * @param txId
//     * @return
//     */
//    List<FundFlowEntity> getTransactionFundFlowByBlockChain(String txId);


    /**
     * userId 来查询某个用户的所有捐款记录
     * @param userId
     * @return
     */
    List<FundFlowEntity> getUserAllContributionFlow(String userId);

    /**
     * userId 来查询某个用户对某个项目的所有资金流动情况
     * @param userId
     * @return
     */
    List<FundFlowEntity> getUserProjectAllFundFlow(String userId,String projectId);


    /**
     * 查询某个项目资金流动的情况
     * @param projectId
     * @return
     */
    List<FundFlowEntity> getProjectFundFlow(String projectId);

    /**
     * 获取所有节点的信息
     * @return
     */
    List<NodeQueryResult> getNodeQueryList();
}
