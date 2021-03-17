package com.gdut.fundraising.service.impl;

import com.gdut.fundraising.blockchain.*;
import com.gdut.fundraising.blockchain.Service.BlockChainService;
import com.gdut.fundraising.blockchain.Service.impl.MerkleTreeServiceImpl;
import com.gdut.fundraising.blockchain.Service.impl.TransactionServiceImpl;
import com.gdut.fundraising.blockchain.Service.impl.UTXOServiceImpl;
import com.gdut.fundraising.constant.raft.NodeStatus;
import com.gdut.fundraising.entities.SpendEntity;
import com.gdut.fundraising.entities.raft.BlockChainNode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class BlockChainServiceImplTest {

    @InjectMocks
    BlockChainServiceImpl blockChainService;

    @Mock
    BlockChainNode blockChainNode;


    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testContribution() {
        String userId = "qdfwfafsd";
        String projectId = "sfdfsdg";
        long money = 100L;
        Transaction transaction = new Transaction();
        transaction.setFormProjectId(projectId);
        transaction.setFromUserId(userId);
        transaction.setCoinBase(true);

        //peer节点没办法mock，于是创建一个新的peer节点，并手动设置相关的bean
        Peer peer = initPeer();

        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(transaction);

        when(blockChainNode.getPeer()).thenReturn(peer);
        when(blockChainNode.sendLogToOtherNodeForConsistency(any())).thenReturn(true);

        blockChainNode.status = NodeStatus.LEADER;

        boolean res = blockChainService.contribution(userId, projectId, money);

        Assert.assertTrue(res);
    }

    @Test
    public void testContributionFollower() {
        String userId = "qdfwfafsd";
        String projectId = "sfdfsdg";
        long money = 100L;
        Transaction transaction = new Transaction();
        transaction.setFormProjectId(projectId);
        transaction.setFromUserId(userId);
        transaction.setCoinBase(true);

        //peer节点没办法mock，于是创建一个新的peer节点，并手动设置相关的bean
        Peer peer = initPeer();

        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(transaction);

        when(blockChainNode.getPeer()).thenReturn(peer);
        when(blockChainNode.broadcastTransaction(any())).thenReturn(true);

        blockChainNode.status = NodeStatus.FOLLOWER;

        boolean res = blockChainService.contribution(userId, projectId, money);

        Assert.assertTrue(res);
    }


    @Test
    public void testContributionRaftError() {
        String userId = "qdfwfafsd";
        String projectId = "sfdfsdg";
        long money = 100L;
        Transaction transaction = new Transaction();
        transaction.setFormProjectId(projectId);
        transaction.setFromUserId(userId);
        transaction.setCoinBase(true);

        //peer节点没办法mock，于是创建一个新的peer节点，并手动设置相关的bean
        Peer peer = initPeer();

        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(transaction);

        when(blockChainNode.getPeer()).thenReturn(peer);
        when(blockChainNode.sendLogToOtherNodeForConsistency(any())).thenReturn(false);

        blockChainNode.status = NodeStatus.LEADER;

        boolean res = blockChainService.contribution(userId, projectId, money);

        Assert.assertFalse(res);
    }

    @Test
    public void testUseMoney() {
        String userId = "qdfwfafsd";
        String projectId = "sfdfsdg";
        long money = 100L;

        SpendEntity spendEntity = new SpendEntity();
        spendEntity.setToAddress("xxx");
        spendEntity.setProjectId(projectId);
        spendEntity.setMoney(money);

        Peer peer = initPeer();
        UTXO utxo = getUTXO(peer.getWallet().getKeyPair().getPublic(), peer.getWallet().getAddress(), userId, projectId);
        peer.getUTXOHashMap().put(utxo.getPointer(), utxo);

        when(blockChainNode.getPeer()).thenReturn(peer);
        when(blockChainNode.sendLogToOtherNodeForConsistency(any())).thenReturn(true);

        blockChainNode.status = NodeStatus.LEADER;

        boolean res = blockChainService.useMoney(spendEntity);
        Assert.assertTrue(res);
    }

    @Test
    public void testVerifyTransactionsFromOtherNode() {
    }

    @Test
    public void testAddBlockToChain() {
    }

    private Peer initPeer() {
        //peer节点没办法mock，于是创建一个新的peer节点，并手动设置相关的bean
        Peer peer = new Peer();
        Wallet wallet = new Wallet();
        wallet.generateKeyAndAddress();

        peer.setWallet(wallet);

        TransactionServiceImpl transactionService = new TransactionServiceImpl();
        com.gdut.fundraising.blockchain.Service.impl.BlockChainServiceImpl blockChainService1 =
                new com.gdut.fundraising.blockchain.Service.impl.BlockChainServiceImpl();
        UTXOServiceImpl utxoService = new UTXOServiceImpl();

        transactionService.setUtxoService(utxoService);

        blockChainService1.setMerkleTreeService(new MerkleTreeServiceImpl());
        blockChainService1.setTransactionService(transactionService);
        blockChainService1.setUtxoService(utxoService);

        peer.setTransactionService(transactionService);
        peer.setBlockChainService(blockChainService1);
        return peer;
    }

    private UTXO getUTXO(PublicKey pk, String address, String userId, String projectId) {
        Pointer pointer = new Pointer();
        UTXO utxo = new UTXO();
        Vout vout = new Vout();

        pointer.setN(1);
        pointer.setTxId("afafaaxascasdafdafsdf");

        vout.setMoney(10000L);
        vout.setToAddress(address);

        utxo.setFromUserId(userId);
        utxo.setFormProjectId(projectId);
        utxo.setPointer(pointer);
        utxo.setConfirmed(true);
        utxo.setSpent(false);
        utxo.setVout(vout);

        return utxo;
    }
}