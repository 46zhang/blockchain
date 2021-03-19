package com.gdut.fundraising.blockchain;

import com.gdut.fundraising.util.FileUtils;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.*;

public class PeerTest {

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testWriteAndReadNoBlockChain() {
        Peer peer=new Peer();
        buildUTXO(peer);
        buildTransaction(peer);
        buildWallet(peer);
        //test write data
        peer.writeData("10000");

        //用另一个peer1去读取peer写入的数据，判断是否能够读写成功，且读写正确
        Peer peer1=new Peer();

        peer1.loadAll("10000");

        Assert.assertEquals(peer.getUTXOHashMap().size(),peer1.getUTXOHashMap().size());
        Assert.assertEquals(peer.getTransactionPool().size(),peer1.getTransactionPool().size());
        Assert.assertEquals(peer.getWallet().getAddress(),peer1.getWallet().getAddress());
        Assert.assertEquals(peer.getBlockChain().size(),peer1.getBlockChain().size());


    }


    @Test
    public void testWriteAndRead() {
        Peer peer=new Peer();
        buildUTXO(peer);
        buildTransaction(peer);
        buildWallet(peer);
        //test write data
        peer.writeData("10000");

        //用另一个peer1去读取peer写入的数据，判断是否能够读写成功，且读写正确
        Peer peer1=new Peer();

        peer1.loadAll("10000");

        Assert.assertEquals(peer.getUTXOHashMap().size(),peer1.getUTXOHashMap().size());
        Assert.assertEquals(peer.getTransactionPool().size(),peer1.getTransactionPool().size());
        Assert.assertEquals(peer.getWallet().getAddress(),peer1.getWallet().getAddress());
        Assert.assertEquals(peer.getBlockChain().size(),peer1.getBlockChain().size());


    }


    void buildUTXO(Peer peer){
        for(int i=0;i<10;++i){
            UTXO utxo=new UTXO();
            utxo.setFormProjectId(String.valueOf(i));
            utxo.setFromUserId(String.valueOf(i));
            utxo.setCoinBase(false);
            utxo.setPointer(new Pointer(String.valueOf(i),i));
            peer.getUTXOHashMap().put(utxo.getPointer(),utxo);
        }
    }


    void buildTransaction(Peer peer){
        for(int i=0;i<10;++i){
            Transaction transaction=new Transaction();
            transaction.setFormProjectId(String.valueOf(i));
            transaction.setFromUserId(String.valueOf(i));
            transaction.setCoinBase(false);
            transaction.setId(String.valueOf(i));
            peer.getTransactionPool().put(transaction.getId(),transaction);
        }
    }

    void buildWallet(Peer peer){
        Wallet wallet=peer.getWallet();
        wallet.generateKeyAndAddress();
    }
}