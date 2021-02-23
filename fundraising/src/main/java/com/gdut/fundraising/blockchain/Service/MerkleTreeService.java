package com.gdut.fundraising.blockchain.Service;

import com.gdut.fundraising.blockchain.Transaction;

import java.util.List;

/**
 * 梅克尔树加密服务
 */
public interface MerkleTreeService {
    String getMerkleRoot(List<Transaction> txs);
}
