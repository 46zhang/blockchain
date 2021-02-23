package com.gdut.fundraising.blockchain;

import com.gdut.fundraising.blockchain.BlockChainConstant;
import com.gdut.fundraising.blockchain.Peer;
import com.gdut.fundraising.blockchain.Pointer;
import com.gdut.fundraising.blockchain.Service.impl.TransactionServiceImpl;
import com.gdut.fundraising.blockchain.Sha256Util;
import com.gdut.fundraising.blockchain.Transaction;
import com.gdut.fundraising.blockchain.UTXO;
import com.gdut.fundraising.blockchain.Vout;
import com.gdut.fundraising.blockchain.Wallet;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

class TransactionServiceImplTest {

    TransactionServiceImpl transactionService = new TransactionServiceImpl();

    @Test
    void createTransaction() {
        Peer peer = getPeer();
        Wallet wallet = peer.getWallet();
        HashMap<Pointer, UTXO> utxoMap = new HashMap<>();
        UTXO utxo = getUTXO(wallet.getKeyPair().getPublic(),
                wallet.getAddress());
        utxoMap.put(utxo.getPointer(), utxo);

        peer.setUTXOHashMap(utxoMap);

        Peer peerA = getPeer();

        Transaction transaction1 = transactionService.createTransaction(peerA, peerA.getWallet().getAddress(), 1000);
        Assert.assertNull(transaction1);

        Transaction transaction = transactionService.createTransaction(peer, peerA.getWallet().getAddress(), 1000);
        Assert.assertNotNull(transaction);

        Assert.assertEquals(transaction.getInList().get(0).getToSpent().getTxId(), utxo.getPointer().getTxId());
        Assert.assertEquals(transaction.getOutList().get(0).getToAddress(), peerA.getWallet().getAddress());
        Assert.assertEquals(transaction.getOutList().get(0).getMoney(), 1000 - BlockChainConstant.FEE);

        Assert.assertEquals(transaction.getOutList().get(1).getToAddress(), peer.getWallet().getAddress());
        Assert.assertTrue(peer.getTransactionPool().containsKey(transaction.getId()));
        UTXO utxo1 = getUTXO(peer.getWallet().getKeyPair().getPublic(), peer.getWallet().getAddress());
        peer.getUTXOHashMap().put(utxo1.getPointer(), utxo1);

        transaction = transactionService.createTransaction(peer, peerA.getWallet().getAddress(), 10001);
        Assert.assertNull(transaction);

        UTXO utxo2 = getUTXO(peer.getWallet().getKeyPair().getPublic(), peer.getWallet().getAddress());
        peer.getUTXOHashMap().put(utxo2.getPointer(), utxo2);
        transaction = transactionService.createTransaction(peer, peerA.getWallet().getAddress(), 10001);
        Assert.assertEquals(transaction.getOutList().get(0).getMoney(), 10001 - BlockChainConstant.FEE);

        Assert.assertEquals(transaction.getOutList().get(1).getMoney(), 20000 - 10001);

    }

    @Test
    void verifyTransaction() {
        Peer peer = getPeer();
        Wallet wallet = peer.getWallet();
        HashMap<Pointer, UTXO> utxoMap = new HashMap<>();
        UTXO utxo = getUTXO(wallet.getKeyPair().getPublic(),
                wallet.getAddress());
        utxoMap.put(utxo.getPointer(), utxo);

        peer.setUTXOHashMap(utxoMap);

        Peer peerA = getPeer();
        Transaction transaction = transactionService.createTransaction(peer, peerA.getWallet().getAddress(), 1000);
        Assert.assertNotNull(transaction);
        boolean result=transactionService.verifyTransaction(peer,transaction);
        Assert.assertFalse(result);

        peerA.setUTXOHashMap(utxoMap);

        Assert.assertTrue( transactionService.verifyTransaction(peerA,transaction));
    }

    @Test
    void testCode() {
        UTXO utxo = new UTXO();
        Pointer pointer = new Pointer();
        List<Vout> vouts = new ArrayList<>();
        Vout vout1 = new Vout();
        Vout vout2 = new Vout();

        vout1.setMoney(100L);
        vout1.setToAddress("sdfhsdhfsdgsd132413242");

        vout2.setToAddress("sdfhshf324324");
        vout2.setMoney(20L);

        vouts.add(vout1);
        vouts.add(vout2);

        pointer.setTxId("1434234234");
        pointer.setN(10);

        utxo.setPointer(pointer);

        String s = utxo.getPointer().getTxId() + utxo.getPointer().getN() + vouts;

        System.out.println(s);

        System.out.println(Sha256Util.getSHA256(s));
    }

    @Test
    void createCoinBaseTransaction() {
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