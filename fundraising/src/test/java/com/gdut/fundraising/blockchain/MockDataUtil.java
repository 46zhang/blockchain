package com.gdut.fundraising.blockchain;

import com.gdut.fundraising.blockchain.Block;
import com.gdut.fundraising.blockchain.BlockChainConstant;
import com.gdut.fundraising.blockchain.Peer;
import com.gdut.fundraising.blockchain.Pointer;
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
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.security.PublicKey;
import java.util.*;

public class MockDataUtil {

    TransactionServiceImpl transactionService=new TransactionServiceImpl();
    BlockChainServiceImpl blockChainService=new BlockChainServiceImpl();

    @Test
    void buildBlockChain() {
        //获取节点并初始化钱包
        Peer peer = getPeer();
        Wallet wallet = peer.getWallet();
        HashMap<Pointer, UTXO> utxoMap = new HashMap<>();
        //创建第一笔交易
        Transaction tx= transactionService.createCoinBaseTransaction(peer,peer.getWallet().getAddress(),1000);
        List<Transaction> txs=new ArrayList<>();
        txs.add(tx);
        //封装数据成区块
        Block block=buildBlock(0,null, txs);

        transactionService.setUtxoService(new UTXOServiceImpl());
        blockChainService.setTransactionService(transactionService);
        blockChainService.setUtxoService(new UTXOServiceImpl());


        //打包第一个区块到区块链中
        boolean result= blockChainService.addBlockToChain(peer,block);
        Assert.assertTrue(result);
        Peer peerA=getPeer();
        Transaction tx1=transactionService.createTransaction(peer,peerA.getWallet().getAddress(),100);
        txs.clear();
        txs.add(tx1);
        Block block1=buildBlock(1,block.getHash(),txs);
        //打包第二个区块到区块链中
        result=blockChainService.addBlockToChain(peer,block1);
        Assert.assertTrue(result);
        Assert.assertTrue(peer.getBlockChain().size()==2);
        Assert.assertTrue(peer.getBlockChain().get(0).getHash().equals(block.getHash()));
        Assert.assertTrue(peer.getBlockChain().get(1).getHash().equals(block1.getHash()));
        Assert.assertTrue(peer.getTransactionPoolBackup().containsKey(tx1.getId()));
        Assert.assertTrue(peer.getTransactionPool().size()==0);
        Assert.assertTrue(peer.getUTXOHashMapBackup().size()==1);
        Assert.assertTrue(peer.getUTXOHashMapBackup().containsKey(new Pointer(tx.getId(),0)));

        Transaction tx2=transactionService.createTransaction(peer,peerA.getWallet().getAddress(),200);

        txs.add(tx2);
        Block block2=buildBlock(1,block.getHash(),txs);
        //打包第三个区块(包含 tx1 tx2)到区块链中
        result=blockChainService.addBlockToChain(peer,block2);
        Assert.assertTrue(result);
        Assert.assertTrue(peer.getBlockChain().size()==2);
        Assert.assertTrue(peer.getBlockChain().get(0).getHash().equals(block.getHash()));
        Assert.assertTrue(peer.getBlockChain().get(1).getHash().equals(block2.getHash()));
        Assert.assertTrue(peer.getBlockChain().get(1).getTxs().size()==2);
        Assert.assertTrue(peer.getUTXOHashMapBackup().containsKey(new Pointer(tx.getId(),0)));
    }

    public static Block buildBlock(int height, String preHash, List<Transaction> txs) {
        Block block = new Block();
        MerkleTreeServiceImpl merkleTreeService=new MerkleTreeServiceImpl();
        block.setTxs(txs);

        block.setMerkleRootHash(merkleTreeService.getMerkleRoot(txs));
        block.setHeight(height);
        block.setPreBlockHash(preHash);
        block.setVersion(BlockChainConstant.VERSION);
        block.setTime(new Date());
        block.setHash(Sha256Util.doubleSHA256(block.getHeader()));
        return block;
    }


    public static Transaction buildTransaction(boolean coinBase, long fee, Date lockTime,
                                                                   List<Vin> vins, List<Vout> vouts) {
        Transaction transaction = new Transaction();


        transaction.setLockTime(lockTime);
        transaction.setFee(fee);
        transaction.setCoinBase(coinBase);
        transaction.setId(Sha256Util.doubleSHA256(transaction.toString()));

        transaction.setInList(vins);
        transaction.setOutList(vouts);
        return transaction;
    }


    public static Peer getPeer() {
        Peer peer = new Peer();
        Wallet wallet = new Wallet();
        wallet.generateKeyAndAddress();
        peer.setWallet(wallet);
        return peer;
    }

    public static UTXO getUTXO(PublicKey pk, String address) {
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
    public static String generateRandomStr(int length) {
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
