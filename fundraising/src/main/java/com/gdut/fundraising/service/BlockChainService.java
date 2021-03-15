package com.gdut.fundraising.service;

import com.gdut.fundraising.blockchain.Block;
import com.gdut.fundraising.blockchain.Transaction;
import com.gdut.fundraising.entities.SpendEntity;

import java.util.List;

public interface BlockChainService {
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
}
