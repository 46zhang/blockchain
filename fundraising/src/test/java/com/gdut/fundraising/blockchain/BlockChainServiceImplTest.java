package com.gdut.fundraising.blockchain;

import com.gdut.fundraising.blockchain.Block;
import com.gdut.fundraising.blockchain.BlockChainConstant;
import com.gdut.fundraising.blockchain.EccUtil;
import com.gdut.fundraising.blockchain.Peer;
import com.gdut.fundraising.blockchain.Pointer;
import com.gdut.fundraising.blockchain.Service.MerkleTreeService;
import com.gdut.fundraising.blockchain.Service.TransactionService;
import com.gdut.fundraising.blockchain.Service.impl.BlockChainServiceImpl;
import com.gdut.fundraising.blockchain.Service.impl.MerkleTreeServiceImpl;
import com.gdut.fundraising.blockchain.Service.impl.TransactionServiceImpl;
import com.gdut.fundraising.blockchain.Service.impl.UTXOServiceImpl;
import com.gdut.fundraising.blockchain.Sha256Util;
import com.gdut.fundraising.blockchain.Transaction;
import com.gdut.fundraising.blockchain.UTXO;
import com.gdut.fundraising.blockchain.Vin;
import com.gdut.fundraising.blockchain.Vout;
import com.gdut.fundraising.blockchain.Wallet;
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

        //获取节点并初始化钱包
        Peer peer = getPeer();
        Wallet wallet = peer.getWallet();
        HashMap<Pointer,
                UTXO> utxoMap = new HashMap<>();
        //创建第一笔交易
        Transaction tx = transactionService.createCoinBaseTransaction(peer, peer.getWallet().getAddress(), 1000);
        List<Transaction> txs = new ArrayList<>();
        txs.add(tx);
        //封装数据成区块
        Block block = MockDataUtil.buildBlock(0, null, txs);

        //打包第一个区块到区块链中
        boolean result = blockChainService.addBlockToChain(peer, block);
        org.junit.Assert.assertTrue(result);
        Peer peerA = getPeer();
        Transaction tx1 = transactionService.createTransaction(peer, peerA.getWallet().getAddress(), 100);
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

        Transaction tx2 = transactionService.createTransaction(peer, peerA.getWallet().getAddress(), 200);

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
        Transaction tx = transactionService.createCoinBaseTransaction(peer, peer.getWallet().getAddress(), 1000);
        List<Transaction> txs = new ArrayList<>();
        txs.add(tx);
        //封装数据成区块
        Block block = MockDataUtil.buildBlock(0, null, txs);

        //打包第一个区块到区块链中
        boolean result = blockChainService.addBlockToChain(peer, block);
        org.junit.Assert.assertTrue(result);
        Peer peerA = getPeer();
        Transaction tx1 = transactionService.createTransaction(peer, peerA.getWallet().getAddress(), 100);
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

        Transaction tx2 = transactionService.createTransaction(peer, peerA.getWallet().getAddress(), 200);
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