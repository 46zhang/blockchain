package com.gdut.fundraising.service.impl;

import com.gdut.fundraising.blockchain.*;
import com.gdut.fundraising.blockchain.Service.impl.MerkleTreeServiceImpl;
import com.gdut.fundraising.blockchain.Service.impl.TransactionServiceImpl;
import com.gdut.fundraising.blockchain.Service.impl.UTXOServiceImpl;
import com.gdut.fundraising.constant.raft.NodeStatus;
import com.gdut.fundraising.entities.FundFlowEntity;
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
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class BCTServiceImplTest {

    @InjectMocks
    BCTServiceImpl blockChainService;

    @Mock
    BlockChainNode blockChainNode;

    private Wallet wallet;


    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        wallet = new Wallet();
        wallet.generateKeyAndAddress();
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

    @Test
    public void testgetProjectFundFlow() {
        String projectId = "asfafacxxxxx";
        String userId = "2412423edasdas";
        List<Block> blockList = new ArrayList<>();
        Peer peer = new Peer();
        peer.getWallet().generateKeyAndAddress();

        when(blockChainNode.getPeer()).thenReturn(peer);

        blockList.add(buildBlock(0, true, null, projectId, userId));
        blockList.add(buildBlock(0, false, null, projectId, userId));
        blockList.add(buildBlock(0, true, null, projectId + "fsaf", userId));


        peer.setBlockChain(blockList);
        List<FundFlowEntity> fundFlowEntities = blockChainService.getProjectFundFlow(projectId);

        Assert.assertEquals(fundFlowEntities.size(), 2);
        Assert.assertEquals(fundFlowEntities.get(0).getTxId(), blockList.get(1).getTxs().get(0).getId());
        Assert.assertEquals(fundFlowEntities.get(0).getTo(), wallet.getAddress());

        Assert.assertEquals(fundFlowEntities.get(1).getTxId(), blockList.get(0).getTxs().get(0).getId());
        Assert.assertEquals(fundFlowEntities.get(1).getTo(), wallet.getAddress());
    }

    @Test
    public void testGetUserProjectAllFundFlow() {
        String projectId = "asfafacxxxxx";
        String userId = "2412423edasdas";
        List<Block> blockList = new ArrayList<>();
        Peer peer = new Peer();
        peer.getWallet().generateKeyAndAddress();

        when(blockChainNode.getPeer()).thenReturn(peer);

        blockList.add(buildBlock(0, true, null, projectId, userId));
        blockList.add(buildBlock(0, false, null, projectId, userId));
        blockList.add(buildBlock(0, true, null, projectId + "fsaf", userId));


        peer.setBlockChain(blockList);
        List<FundFlowEntity> fundFlowEntities = blockChainService.getUserProjectAllFundFlow(userId,projectId);

        Assert.assertEquals(fundFlowEntities.size(), 2);
        Assert.assertEquals(fundFlowEntities.get(0).getTxId(), blockList.get(1).getTxs().get(0).getId());
        Assert.assertEquals(fundFlowEntities.get(0).getTo(), wallet.getAddress());

        Assert.assertEquals(fundFlowEntities.get(1).getTxId(), blockList.get(0).getTxs().get(0).getId());
        Assert.assertEquals(fundFlowEntities.get(1).getTo(), wallet.getAddress());
    }


    @Test
    public void testGetUserAllFundFlow() {
        String projectId = "asfafacxxxxx";
        String userId = "2412423edasdas";
        List<Block> blockList = new ArrayList<>();
        Peer peer = new Peer();
        peer.getWallet().generateKeyAndAddress();

        when(blockChainNode.getPeer()).thenReturn(peer);

        blockList.add(buildBlock(0, true, null, projectId, userId));
        blockList.add(buildBlock(0, false, null, projectId, userId));
        blockList.add(buildBlock(0, false, null, projectId + "fsaf", userId));


        peer.setBlockChain(blockList);
        List<FundFlowEntity> fundFlowEntities = blockChainService.getUserAllContributionFlow(userId);

        Assert.assertEquals(fundFlowEntities.size(), 1);
        Assert.assertEquals(fundFlowEntities.get(0).getTxId(), blockList.get(0).getTxs().get(0).getId());
        Assert.assertEquals(fundFlowEntities.get(0).getTo(), wallet.getAddress());
        Assert.assertEquals(fundFlowEntities.get(0).getBlockHash(),blockList.get(0).getHash());
        Assert.assertNull(fundFlowEntities.get(0).getFrom());
    }



    private Block buildBlock(int height, boolean coinBase, String preHash, String projectId, String userId) {
        Block block = new Block();
        List<Transaction> txs = new ArrayList<>();
        txs.add(buildTransaction(coinBase, 0, new Date(), projectId, userId));
        block.setTxs(txs);
        MerkleTreeServiceImpl merkleTreeServiceImpl = new MerkleTreeServiceImpl();
        block.setMerkleRootHash(merkleTreeServiceImpl.getMerkleRoot(txs));
        block.setHeight(height);
        block.setPreBlockHash(preHash);
        block.setVersion(BlockChainConstant.VERSION);
        block.setTime(new Date());
        block.setHash(Sha256Util.doubleSHA256(block.getHeader()));
        return block;
    }


    private Transaction buildTransaction(boolean coinBase, long fee, Date lockTime, String projectId, String userId) {
        Transaction transaction = new Transaction();
        List<Vout> vouts = new ArrayList<>();
        Vout vout = new Vout();
        vout.setMoney(100L);
        vouts.add(vout);
        vout.setToAddress(wallet.getAddress());

        List<Vin> vins = new ArrayList<>();
        Vin vin = new Vin();
        vin.setPublicKey(wallet.getKeyPair().getPublic());
        vins.add(vin);

        transaction.setLockTime(lockTime);
        transaction.setFee(fee);
        transaction.setCoinBase(coinBase);
        transaction.setId(Sha256Util.doubleSHA256(transaction.toString()));
        transaction.setFormProjectId(projectId);
        transaction.setFromUserId(userId);
        transaction.setOutList(vouts);
        transaction.setInList(vins);

        return transaction;
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