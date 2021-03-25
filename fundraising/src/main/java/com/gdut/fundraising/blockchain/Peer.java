package com.gdut.fundraising.blockchain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gdut.fundraising.blockchain.Service.BlockChainService;
import com.gdut.fundraising.blockchain.Service.TransactionService;
import com.gdut.fundraising.constant.LogConstance;
import com.gdut.fundraising.entities.raft.LogEntry;
import com.gdut.fundraising.exception.BaseException;
import com.gdut.fundraising.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;


/**
 * 区块链节点
 */
@Component
public class Peer {

    private final static Logger LOGGER = LoggerFactory.getLogger(Peer.class);
    /**
     * 区块链的数据，用volatile关键字声明，确保某个线程修改后，对其他线程可见
     */
    volatile private List<Block> blockChain;

    /**
     * utxo 哈希集
     */
    volatile private HashMap<Pointer, UTXO> UTXOHashMap;

    /**
     * 钱包
     */
    private Wallet wallet;

    /**
     * 孤立交易池
     */
    private HashMap<String, Transaction> orphanPool;

    /**
     * 交易池
     */
    volatile private HashMap<String, Transaction> transactionPool;

    /**
     * utxo 哈希集备份
     */
    private HashMap<Pointer, UTXO> UTXOHashMapBackup;

    /**
     * 用户自身的utxo备份
     */
    private HashMap<Pointer, UTXO> ownUTXOHashMapBackup;

    /**
     * 交易池备份
     */
    private HashMap<String, Transaction> transactionPoolBackup;

    /**
     * 输出单元的定位指针，用于数据回滚
     */
    private List<Pointer> pointersFromVout;

    /**
     * 输出单元封装成的utxo，用于数据回滚
     */
    private List<UTXO> utxosFromVout;

    /**
     * 区块链服务
     */
    @Autowired
    private BlockChainService blockChainService;

    /**
     * 交易服务
     */
    @Autowired
    private TransactionService transactionService;


    public Peer() {
        UTXOHashMap = new HashMap<>();
        orphanPool = new HashMap<>();
        transactionPool = new HashMap<>();
        blockChain = new ArrayList<>();
        transactionPoolBackup = new HashMap<>();
        wallet = new Wallet();
    }

    @PostConstruct
    public void loadPeerInitData() {
        Properties props = System.getProperties(); //系统属性
        String port = (String) props.get("port");
        //加载peer的属性
        //TODO 在测试的时候要屏蔽该行
        loadAll(port);
    }

    @PreDestroy
    /**
     * 销毁前把数据存入文件，需要存入以下3种数据
     * <1>waller</1>
     * <2>utxoHashMap values</2>
     * <3>transactionPol values</3>
     */
    public void writeData() {
        LOGGER.info("================开始写入数据到文件=====================");
        Properties props = System.getProperties(); //系统属性
        String port = (String) props.get("port");
        writeData(port);
        LOGGER.info("================写入数据到文件完成=====================");
    }


    /**
     * 销毁前把数据存入文件，需要存入以下3种数据
     * <1>waller</1>
     * <2>utxoHashMap values</2>
     * <3>transactionPol values</3>
     */
    public void writeData(String port) {
        String pathName = FileUtils.buildPath(FileUtils.getRootFilePath(), port);
        //创建目录，会保证幂等性
        FileUtils.createDir(pathName);
        pathName = FileUtils.buildPath(pathName, LogConstance.PEER_PATH);
        //创建目录，会保证幂等性
        FileUtils.createDir(pathName);

        //write keypair
        writeKeyPair(port);
        //write transaction
        writeTransactionValues(port);
        //write utxo
        writeUTXOValues(port);
    }


    public boolean saveUTXOSet() {
        Properties props = System.getProperties(); //系统属性
        String port = (String) props.get("port");
        boolean res = writeUTXOValues(port);
        if (!res) {
            LOGGER.error("写入UTXO到文件失败 {}", port);
        }
        return res;
    }

    public boolean saveTransactionSet() {
        Properties props = System.getProperties(); //系统属性
        String port = (String) props.get("port");
        boolean res = writeTransactionValues(port);
        if (!res) {
            LOGGER.error("写入Transaction到文件失败 {}", port);
        }
        return res;
    }

    /**
     * 写入utxo
     *
     * @param port
     */
    private boolean writeUTXOValues(String port) {
        String pathName = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_UTXO_PATH);
        //创建文件
        FileUtils.createFile(pathName);
        Collection<UTXO> collection = UTXOHashMap.values();
        List<UTXO> utxos = new ArrayList<UTXO>(collection);
        String data = JSON.toJSONString(utxos);
        return FileUtils.write(pathName, data);
    }

    /**
     * 写入交易
     *
     * @param port
     */
    private boolean writeTransactionValues(String port) {
        String pathName = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_TRANSACTION_PATH);
        //创建文件
        FileUtils.createFile(pathName);
        Collection<Transaction> collection = transactionPool.values();
        List<Transaction> utxos = new ArrayList<Transaction>(collection);
        String data = JSON.toJSONString(utxos);
        return FileUtils.write(pathName, data);
    }

    /**
     * 写入公私密钥
     *
     * @param port
     */
    private void writeKeyPair(String port) {
        String pathName1 = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_PK_PATH);
        String pathName2 = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_SK_PATH);
        String pathName3 = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_ADDR_PATH);

        //创建文件,会保证幂等
        FileUtils.createFile(pathName1);
        FileUtils.createFile(pathName2);
        FileUtils.createFile(pathName3);

        byte[] pk = wallet.getKeyPair().getPublic().getEncoded();
        byte[] sk = wallet.getKeyPair().getPrivate().getEncoded();
        String address = wallet.getAddress();
        try {
            FileUtils.write(pathName1, pk);
            FileUtils.write(pathName2, sk);
            FileUtils.write(pathName3, address);
        } catch (Exception e) {
            LOGGER.error("write key pair error!!!");
        }
    }

    /**
     * 写入钱包数据
     *
     * @param port
     */
    private void writeWallet(String port) {
        String pathName = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_WALLET_PATH);
        String data = JSON.toJSONString(wallet);
        FileUtils.write(pathName, data);
    }

    /**
     * 加载peer的配置项
     *
     * @param port
     */
    public void loadAll(String port) {
        String pathName = FileUtils.buildPath(FileUtils.getRootFilePath(), port);
        //创建目录，会保证幂等性
        FileUtils.createDir(pathName);
        pathName = FileUtils.buildPath(pathName, LogConstance.PEER_PATH);
        //创建目录，会保证幂等性
        FileUtils.createDir(pathName);

        //加载该节点的公私钥
        loadKeyPair(port);
        //加载该节点的交易池记录
        loadTransaction(port);
        //加载该节点的UTXO
        loadUTXO(port);
        //加载该节点的区块
        loadBLockChain(port);

    }

    /**
     * 加载钱包数据
     *
     * @param port
     */
    private void loadWallet(String port) {
        String pathName = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_WALLET_PATH);
        String data = FileUtils.read(pathName);
        if (data == null) {
            LOGGER.error("peer wallet钱包数据读取失败");
            throw new BaseException(400, "区块链节点本地数据初始化失败!!!");
        } else {
            wallet = JSON.parseObject(data, Wallet.class);
        }

    }

    public String getAddressFromFile(String port) {
        String pathName3 = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_ADDR_PATH);
        return FileUtils.read(pathName3);
    }

    private void loadKeyPair(String port) {
        String pathName1 = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_PK_PATH);
        String pathName2 = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_SK_PATH);
        String pathName3 = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_ADDR_PATH);
        byte[] bpk = FileUtils.readBin(pathName1);
        byte[] bsk = FileUtils.readBin(pathName2);
        String addr = FileUtils.read(pathName3);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey pk = keyFactory.generatePublic(new X509EncodedKeySpec(bpk));
            PrivateKey sk = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bsk));
            KeyPair keyPair = new KeyPair(pk, sk);
            wallet.setKeyPair(keyPair);
            wallet.setAddress(addr);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.error("peer wallet钱包数据读取失败");
            throw new BaseException(400, "区块链节点本地数据初始化失败!!!");
        }

    }

    private void loadBLockChain(String port) {
        String pathName = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.BLOCK_CHAIN_PATH);
        String[] fileNames = FileUtils.getAllFileNameInDir(pathName);

        if (fileNames == null || fileNames.length <= 0) {
            return;
        }

        for (int i = 0; i < fileNames.length; ++i) {
            String data = FileUtils.read(FileUtils.buildPath(pathName, String.valueOf(i+1) + ".json"));
            if (data != null) {
                LogEntry entry = JSON.parseObject(data, LogEntry.class);
                //添加区块数据
                blockChain.add(entry.getData());
            }
        }
        LOGGER.info("================区块链数组初始化完成=====================");
    }


    private void loadUTXO(String port) {
        String pathName = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_UTXO_PATH);
        String data = FileUtils.read(pathName);
        if (data == null) {
            LOGGER.warn("peer utxo数据读取数据为空");
            // throw new BaseException(400, "区块链节点本地数据初始化失败!!!");
        } else {
//            JSONArray jsonArray= JSONArray.parseArray(data);
//            for(Iterator iterator=jsonArray.iterator();iterator.hasNext();){
//                JSONObject jsonObject=(JSONObject) iterator.next();
//            }
            List<UTXO> list = JSON.parseArray(data, UTXO.class);
            //初始化hashMap
            for (int i = 0; i < list.size(); ++i) {
                UTXOHashMap.put(list.get(i).getPointer(), list.get(i));
            }
        }
        LOGGER.info("================UTXO Map初始化完成=====================");
    }

    private void loadTransaction(String port) {
        String pathName = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.PEER_TRANSACTION_PATH);
        String data = FileUtils.read(pathName);
        if (data == null) {
            LOGGER.error("peer transaction数据读取数据为空");
            //throw new BaseException(400, "区块链节点本地数据初始化失败!!!");
        } else {
            List<Transaction> list = JSON.parseArray(data, Transaction.class);
            //初始化 transactionPool
            for (int i = 0; i < list.size(); ++i) {
                transactionPool.put(list.get(i).getId(), list.get(i));
            }
        }
        LOGGER.info("================transactionPool 数据初始化完成=====================");

    }

    //TODO 注意要看看是否存在未确认的utxo
    public long getBalance(String address, String projectId, String userId) {
        long money = 0;
        for (UTXO utxo : UTXOHashMap.values()) {
            //必须找到地址一致且金额足够的utxo
            if (utxo.isSpent() || !utxo.getVout().getToAddress().equals(address)) {
                continue;
            }

            //不是同一个项目的utxo不能使用，不累加金钱
            if (!utxo.getFormProjectId().equals(projectId)) {
                continue;
            }

            //不是同一个项目的utxo不能使用，不累加金钱
            if (!utxo.getFromUserId().equals(userId)) {
                continue;
            }

            money += utxo.getVout().getMoney();
        }
        return money;
    }


    public List<Block> getBlockChain() {
        return blockChain;
    }

    public void setBlockChain(List<Block> blockChain) {
        this.blockChain = blockChain;
    }

    public HashMap<String, Transaction> getTransactionPool() {
        return transactionPool;
    }

    public void setTransactionPool(HashMap<String, Transaction> transactionPool) {
        this.transactionPool = transactionPool;
    }

    public HashMap<String, Transaction> getOrphanPool() {
        return orphanPool;
    }

    public void setOrphanPool(HashMap<String, Transaction> orphanPool) {
        this.orphanPool = orphanPool;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public HashMap<Pointer, UTXO> getUTXOHashMap() {
        return UTXOHashMap;
    }

    public void setUTXOHashMap(HashMap<Pointer, UTXO> UTXOHashMap) {
        this.UTXOHashMap = UTXOHashMap;
    }


    public HashMap<Pointer, UTXO> getUTXOHashMapBackup() {
        return UTXOHashMapBackup;
    }

    public void setUTXOHashMapBackup(HashMap<Pointer, UTXO> UTXOHashMapBackup) {
        this.UTXOHashMapBackup = UTXOHashMapBackup;
    }

    public HashMap<Pointer, UTXO> getOwnUTXOHashMapBackup() {
        return ownUTXOHashMapBackup;
    }

    public void setOwnUTXOHashMapBackup(HashMap<Pointer, UTXO> ownUTXOHashMapBackup) {
        this.ownUTXOHashMapBackup = ownUTXOHashMapBackup;
    }

    public HashMap<String, Transaction> getTransactionPoolBackup() {
        return transactionPoolBackup;
    }

    public void setTransactionPoolBackup(HashMap<String, Transaction> transactionPoolBackup) {
        this.transactionPoolBackup = transactionPoolBackup;
    }

    public List<Pointer> getPointersFromVout() {
        return pointersFromVout;
    }

    public void setPointersFromVout(List<Pointer> pointersFromVout) {
        this.pointersFromVout = pointersFromVout;
    }

    public List<UTXO> getUtxosFromVout() {
        return utxosFromVout;
    }

    public void setUtxosFromVout(List<UTXO> utxosFromVout) {
        this.utxosFromVout = utxosFromVout;
    }

    public BlockChainService getBlockChainService() {
        return blockChainService;
    }

    public void setBlockChainService(BlockChainService blockChainService) {
        this.blockChainService = blockChainService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
}
