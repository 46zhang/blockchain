package com.gdut.fundraising.blockchain.Service;

import com.gdut.fundraising.blockchain.Block;
import com.gdut.fundraising.blockchain.Peer;
import com.gdut.fundraising.blockchain.Transaction;

import java.util.List;

public interface BlockChainService {
    /**
     * 创建候选区块
     * @param txs
     * @param preBlock
     * @return
     */
    Block createCandidateBlock(List<Transaction> txs,Block preBlock);

    /**
     * 验证区块
     * @param peer
     * @param block
     * @return
     */
    boolean verifyBlock(Peer peer,Block block);

    /**
     * 添加区块到链上
     * @param peer
     * @param block
     * @return
     */
    boolean addBlockToChain(Peer peer,Block block);
}
