package com.gdut.fundraising.blockchain;


import com.gdut.fundraising.blockchain.Service.MerkleTreeService;
import com.gdut.fundraising.blockchain.Service.TransactionService;
import com.gdut.fundraising.blockchain.Service.impl.BlockChainServiceImpl;
import com.gdut.fundraising.blockchain.Service.impl.MerkleTreeServiceImpl;
import com.gdut.fundraising.blockchain.Service.impl.TransactionServiceImpl;
import com.gdut.fundraising.blockchain.Service.impl.UTXOServiceImpl;

import com.gdut.fundraising.exception.BaseException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BlockChainServiceImplTest {

    @InjectMocks
    BlockChainServiceImpl blockChainService;

    @Mock
    TransactionService transactionService;

    @Mock
    MerkleTreeService merkleTreeService;


    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateCandidateBlock() {
        List<Transaction> txs = new ArrayList<>();
        txs.add(buildTransaction(false, 0, new Date()));
        txs.add(buildTransaction(true, 1, new Date()));
        Block block = new Block();
        block.setHash("dsfsdfsd21e12r12rqwrqrqewr");
        block.setHeight(1);

        when(merkleTreeService.getMerkleRoot(any())).thenReturn("23r4dafdsfsdf32423432432refdfsdf");
        Block result = blockChainService.createCandidateBlock(txs, block);
        Assert.assertEquals(result.getHeight(), block.getHeight() + 1);
        Assert.assertEquals(result.getMerkleRootHash(), "23r4dafdsfsdf32423432432refdfsdf");
        Assert.assertEquals(result.getPreBlockHash(), "dsfsdfsd21e12r12rqwrqrqewr");
        Assert.assertTrue(result.getHash().equals(Sha256Util.doubleSHA256(result.getHeader())));
    }

    @Test
    public void testVerifyBlock() {
        Block block = new Block();
        List<Transaction> txs = new ArrayList<>();
        txs.add(buildTransaction(false, 0, new Date()));
        txs.add(buildTransaction(true, 1, new Date()));
        KeyPair keyPair = EccUtil.generateKeys();
        Pointer pointer = new Pointer();
        Vin vin = new Vin();
        Vin vin1 = new Vin();
        txs.get(0).getInList().add(vin);
        txs.get(1).getInList().add(vin1);
        block.setTxs(txs);
        when(transactionService.verifyTransaction(any(), any())).thenReturn(true);
        boolean result = blockChainService.verifyBlock(new Peer(), block);
        Assert.assertTrue(result);

        txs.get(1).getInList().add(vin);
        result = blockChainService.verifyBlock(new Peer(), block);
        Assert.assertFalse(result);

    }

    @Test
    public void testAddBlockToChain() {

        TransactionServiceImpl transactionService = new TransactionServiceImpl();
        BlockChainServiceImpl blockChainService = new BlockChainServiceImpl();
        transactionService.setUtxoService(new UTXOServiceImpl());
        blockChainService.setTransactionService(transactionService);
        blockChainService.setUtxoService(new UTXOServiceImpl());

        String userId = "34324234";
        String projectId = "afsafsdxxfa34234";

        //获取节点并初始化钱包
        Peer peer = getPeer();
        Wallet wallet = peer.getWallet();
        HashMap<Pointer,
                UTXO> utxoMap = new HashMap<>();
        //创建第一笔交易
        Transaction tx = transactionService.createCoinBaseTransaction(peer, peer.getWallet().getAddress(),
                userId, projectId, 1000);

        Assert.assertEquals(tx.getFormProjectId(), projectId);
        Assert.assertEquals(tx.getFromUserId(), userId);

        List<Transaction> txs = new ArrayList<>();
        txs.add(tx);
        //封装数据成区块
        Block block = MockDataUtil.buildBlock(0, null, txs);

        //打包第一个区块到区块链中
        boolean result = blockChainService.addBlockToChain(peer, block);
        Assert.assertTrue(result);
        Peer peerA = getPeer();


        String userId1 = "34324234234234";


        Transaction tx1 = transactionService.createTransaction(peer, peerA.getWallet().getAddress(),
                100, projectId, userId1);

        Assert.assertEquals(tx1.getFormProjectId(), projectId);
        Assert.assertEquals(tx1.getFromUserId(), userId1);

        txs.clear();
        txs.add(tx1);
        Block block1 = MockDataUtil.buildBlock(1, block.getHash(), txs);
        //打包第二个区块到区块链中
        result = blockChainService.addBlockToChain(peer, block1);
        Assert.assertTrue(result);
        Assert.assertTrue(peer.getBlockChain().size() == 2);
        Assert.assertTrue(peer.getBlockChain().get(0).getHash().equals(block.getHash()));
        Assert.assertTrue(peer.getBlockChain().get(1).getHash().equals(block1.getHash()));
        Assert.assertTrue(peer.getTransactionPoolBackup().containsKey(tx1.getId()));
        Assert.assertTrue(peer.getTransactionPool().size() == 0);
        Assert.assertTrue(peer.getUTXOHashMapBackup().size() == 1);
        Assert.assertTrue(peer.getUTXOHashMapBackup().containsKey(new Pointer(tx.getId(), 0)));

        UTXO utxo = peer.getUTXOHashMap().get(new Pointer(tx1.getId(), 0));

        //验证对应交易创建的block中，生成的utxo附带的项目信息(userId projectId)是正确的
        Assert.assertEquals(utxo.getFromUserId(), userId1);
        Assert.assertEquals(utxo.getFormProjectId(), projectId);
        Assert.assertEquals(utxo.getVout().getFromUserId(),userId1);
        Assert.assertEquals(utxo.getVout().getFormProjectId(),projectId);

        String userId2 = "3sda4324234234234";

        Transaction tx2 = transactionService.createTransaction(peer, peerA.getWallet().getAddress(),
                200, projectId, userId2);

        txs.add(tx2);
        Block block2 = MockDataUtil.buildBlock(1, block.getHash(), txs);
        //打包第三个区块(包含 tx1 tx2)到区块链中
        result = blockChainService.addBlockToChain(peer, block2);
        Assert.assertTrue(result);
        Assert.assertTrue(peer.getBlockChain().size() == 2);
        Assert.assertTrue(peer.getBlockChain().get(0).getHash().equals(block.getHash()));
        Assert.assertTrue(peer.getBlockChain().get(1).getHash().equals(block2.getHash()));
        Assert.assertTrue(peer.getBlockChain().get(1).getTxs().size() == 2);
        Assert.assertTrue(peer.getUTXOHashMapBackup().containsKey(new Pointer(tx.getId(), 0)));
        Assert.assertTrue(peer.getTransactionPool().isEmpty());
        Assert.assertTrue(peer.getTransactionPoolBackup().containsKey(tx1.getId()));
        Assert.assertTrue(peer.getTransactionPoolBackup().containsKey(tx2.getId()));
    }

    @Test
    public void testAddBlockToChainUpLastBlock() {
        String projectId="asfdscsdcxxxx";

        TransactionServiceImpl transactionService = new TransactionServiceImpl();
        BlockChainServiceImpl blockChainService = new BlockChainServiceImpl();
        transactionService.setUtxoService(new UTXOServiceImpl());
        blockChainService.setTransactionService(transactionService);
        blockChainService.setUtxoService(new UTXOServiceImpl());

        //获取节点并初始化钱包
        Peer peer = getPeer();
        Wallet wallet = peer.getWallet();
        HashMap<Pointer, UTXO> utxoMap = new HashMap<>();
        //创建第一笔交易
        Transaction tx = transactionService.createCoinBaseTransaction(peer, peer.getWallet().getAddress(),
                null,projectId,1000);
        List<Transaction> txs = new ArrayList<>();
        txs.add(tx);
        //封装数据成区块
        Block block = MockDataUtil.buildBlock(0, null, txs);

        //打包第一个区块到区块链中
        boolean result = blockChainService.addBlockToChain(peer, block);
        Assert.assertTrue(result);
        Peer peerA = getPeer();
        Transaction tx1 = transactionService.createTransaction(peer, peerA.getWallet().getAddress(),
                100,projectId,null);
        txs.clear();
        txs.add(tx1);
        Block block1 = MockDataUtil.buildBlock(1, block.getHash(), txs);
        //打包第二个区块到区块链中
        result = blockChainService.addBlockToChain(peer, block1);
        Assert.assertTrue(result);
        Assert.assertTrue(peer.getBlockChain().size() == 2);
        Assert.assertTrue(peer.getBlockChain().get(0).getHash().equals(block.getHash()));
        Assert.assertTrue(peer.getBlockChain().get(1).getHash().equals(block1.getHash()));
        Assert.assertTrue(peer.getTransactionPoolBackup().containsKey(tx1.getId()));
        Assert.assertTrue(peer.getTransactionPool().size() == 0);
        Assert.assertTrue(peer.getUTXOHashMapBackup().size() == 1);
        Assert.assertTrue(peer.getUTXOHashMapBackup().containsKey(new Pointer(tx.getId(), 0)));
        Assert.assertTrue(peer.getUTXOHashMap().containsKey(new Pointer(tx1.getId(), 0)));
        Assert.assertTrue(peer.getUTXOHashMap().containsKey(new Pointer(tx1.getId(), 1)));

        Transaction tx2 = transactionService.createTransaction(peer, peerA.getWallet().getAddress(), 200,projectId,null);
        txs.clear();
        txs.add(tx2);
        Block block2 = MockDataUtil.buildBlock(1, block.getHash(), txs);
        //打包第三个区块(只含 tx1 tx2)到区块链中
        //会爆错，整个区块链会混乱，因为上一个交易tx1丢失了
        result = blockChainService.addBlockToChain(peer, block2);
        Assert.assertTrue(result);
        Assert.assertTrue(peer.getBlockChain().size() == 2);
        Assert.assertTrue(peer.getBlockChain().get(0).getHash().equals(block.getHash()));
        Assert.assertTrue(peer.getBlockChain().get(1).getHash().equals(block2.getHash()));
        Assert.assertTrue(peer.getBlockChain().get(1).getTxs().size() == 1);
        Assert.assertTrue(!peer.getTransactionPool().isEmpty());
        Assert.assertTrue(peer.getTransactionPool().containsKey(tx1.getId()));
        Assert.assertTrue(peer.getTransactionPoolBackup().containsKey(tx2.getId()));

    }


    @Test
    public void testRollBack() {
        TransactionServiceImpl transactionService = new TransactionServiceImpl();
        BlockChainServiceImpl blockChainService = new BlockChainServiceImpl();
        transactionService.setUtxoService(new UTXOServiceImpl());
        blockChainService.setTransactionService(transactionService);
        blockChainService.setUtxoService(new UTXOServiceImpl());

        String userId = "34324234";
        String projectId = "afsafsdxxfa34234";

        //获取节点并初始化钱包
        Peer peer = getPeer();
        Wallet wallet = peer.getWallet();
        HashMap<Pointer,
                UTXO> utxoMap = new HashMap<>();
        //创建第一笔交易
        Transaction tx = transactionService.createCoinBaseTransaction(peer, peer.getWallet().getAddress(),
                userId, projectId, 1000);

        Assert.assertEquals(tx.getFormProjectId(), projectId);
        Assert.assertEquals(tx.getFromUserId(), userId);

        List<Transaction> txs = new ArrayList<>();
        txs.add(tx);
        //封装数据成区块
        Block block = MockDataUtil.buildBlock(0, null, txs);

        //打包第一个区块到区块链中
        boolean result = blockChainService.addBlockToChain(peer, block);
        Assert.assertTrue(result);
        Peer peerA = getPeer();


        String userId1 = "34324234234234";


        Transaction tx1 = transactionService.createTransaction(peer, peerA.getWallet().getAddress(),
                100, projectId, userId1);

        Assert.assertEquals(tx1.getFormProjectId(), projectId);
        Assert.assertEquals(tx1.getFromUserId(), userId1);

        txs.clear();
        txs.add(tx1);
        Block block1 = MockDataUtil.buildBlock(1, block.getHash(), txs);
        //打包第二个区块到区块链中
        result = blockChainService.addBlockToChain(peer, block1);
        Assert.assertTrue(result);

        //tx1交易所在区块添加后，会生成utxo
        Assert.assertEquals(peer.getUTXOHashMap().get(new Pointer(tx1.getId(),0)).getVout().getMoney(),100);
        Assert.assertEquals(peer.getUTXOHashMapBackup().get(new Pointer(tx.getId(),0)).getVout().getMoney(),1000);
        Assert.assertNull(peer.getUTXOHashMap().get(new Pointer(tx.getId(),0)));
        //回滚到block1
        blockChainService.rollBackBlock(peer, block1);
        //tx1交易所在区块回滚后，utxo会删除
        Assert.assertNull(peer.getUTXOHashMap().get(new Pointer(tx1.getId(),0)));
        Assert.assertEquals(peer.getUTXOHashMap().get(new Pointer(tx.getId(),0)).getVout().getMoney(),1000);

    }


    @Test
    public void testRollBackNotLastBlock() {
        TransactionServiceImpl transactionService = new TransactionServiceImpl();
        BlockChainServiceImpl blockChainService = new BlockChainServiceImpl();
        transactionService.setUtxoService(new UTXOServiceImpl());
        blockChainService.setTransactionService(transactionService);
        blockChainService.setUtxoService(new UTXOServiceImpl());

        String userId = "34324234";
        String projectId = "afsafsdxxfa34234";

        //获取节点并初始化钱包
        Peer peer = getPeer();
        Wallet wallet = peer.getWallet();
        HashMap<Pointer,
                UTXO> utxoMap = new HashMap<>();
        //创建第一笔交易
        Transaction tx = transactionService.createCoinBaseTransaction(peer, peer.getWallet().getAddress(),
                userId, projectId, 1000);

        Assert.assertEquals(tx.getFormProjectId(), projectId);
        Assert.assertEquals(tx.getFromUserId(), userId);

        List<Transaction> txs = new ArrayList<>();
        txs.add(tx);
        //封装数据成区块
        Block block = MockDataUtil.buildBlock(0, null, txs);

        //打包第一个区块到区块链中
        boolean result = blockChainService.addBlockToChain(peer, block);
        Assert.assertTrue(result);
        Peer peerA = getPeer();


        String userId1 = "34324234234234";


        Transaction tx1 = transactionService.createTransaction(peer, peerA.getWallet().getAddress(),
                100, projectId, userId1);

        Assert.assertEquals(tx1.getFormProjectId(), projectId);
        Assert.assertEquals(tx1.getFromUserId(), userId1);

        txs.clear();
        txs.add(tx1);
        Block block1 = MockDataUtil.buildBlock(1, block.getHash(), txs);
        //打包第二个区块到区块链中
        result = blockChainService.addBlockToChain(peer, block1);
        Assert.assertTrue(result);

        try {
            //回滚到block1
            blockChainService.rollBackBlock(peer, block);
            Assert.fail("No Exception");
        }catch (BaseException e){
            Assert.assertEquals(e.getMessage(),"不是最后一个区块，无法回滚!!!");
        }

    }

    private Block buildBlock(int height, String preHash) {
        Block block = new Block();
        List<Transaction> txs = new ArrayList<>();
        txs.add(buildTransaction(false, 0, new Date()));
        txs.add(buildTransaction(true, 1, new Date()));
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


    private Transaction buildTransaction(boolean coinBase, long fee, Date lockTime) {
        Transaction transaction = new Transaction();
        transaction.setLockTime(lockTime);
        transaction.setFee(fee);
        transaction.setCoinBase(coinBase);
        transaction.setId(Sha256Util.doubleSHA256(transaction.toString()));
        return transaction;
    }


    private Peer getPeer() {
        Peer peer = new Peer();
        Wallet wallet = new Wallet();
        wallet.generateKeyAndAddress();
        peer.setWallet(wallet);
        return peer;
    }

    private UTXO getUTXO(PublicKey pk, String address) {
        Pointer pointer = new Pointer();
        UTXO utxo = new UTXO();
        Vout vout = new Vout();

        pointer.setN(1);
        pointer.setTxId(generateRandomStr(32));

        vout.setMoney(10000L);
        vout.setToAddress(address);

        utxo.setPointer(pointer);
        utxo.setConfirmed(true);
        utxo.setSpent(false);
        utxo.setVout(vout);

        return utxo;
    }

    /**
     * 随机产生一个length长度的a-Z和0-9混合字符串
     *
     * @param length 长度
     * @return 随机字符串
     */
    private String generateRandomStr(int length) {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }


}