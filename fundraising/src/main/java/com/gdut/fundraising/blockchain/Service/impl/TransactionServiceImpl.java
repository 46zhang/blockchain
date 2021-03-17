package com.gdut.fundraising.blockchain.Service.impl;

import com.gdut.fundraising.blockchain.*;
import com.gdut.fundraising.blockchain.Service.TransactionService;
import com.gdut.fundraising.blockchain.Service.UTXOService;
import com.gdut.fundraising.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Service
public class TransactionServiceImpl implements TransactionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private static String ALGORITHM_NAME = "SHA256withECDSA";

    private UTXOService utxoService;

    /**
     * 创建交易
     *
     * @param peer
     * @param toAddress
     * @param money
     * @param projectId
     * @param userId
     * @return
     */
    @Override
    public Transaction createTransaction(Peer peer, String toAddress, long money,
                                         String projectId, String userId) {
        long balance = peer.getBalance(peer.getWallet().getAddress(),projectId);
        //余额不足
        if (balance < money) {
            LOGGER.error("utxo: {},money:{},toAddress:{},projectId:{},userId:{}",
                    peer.getUTXOHashMap().values(), money, toAddress, projectId, userId);
            throw new BaseException(400, "用户余额不够!!!");

        }
        List<UTXO> ownUTXOList = new ArrayList<>();
        long amount = 0;
        //把相关的输入单元加入到交易中
        for (UTXO utxo : peer.getUTXOHashMap().values()) {
            //如果已经消费了则跳过 或者地址不相等的
            if (utxo.isSpent() || !utxo.getVout().getToAddress().equals(peer.getWallet().getAddress())) {
                continue;
            }

            //不是同一个项目的utxo不能使用，不累加金钱
            if (!utxo.getFormProjectId().equals(projectId)) {
                continue;
            }

            amount += utxo.getVout().getMoney();
            ownUTXOList.add(utxo);
            if (amount >= money) {
                break;
            }
        }

        Transaction transaction = new Transaction();
        Wallet ownWallet = peer.getWallet();
        //要减去手续费
        Vout other = buildVout(toAddress, money - BlockChainConstant.FEE, userId, projectId);

        transaction.getOutList().add(other);

        //如果剩余金额大于所需支付金额，那么会将多一笔vout，把多的钱给自己，找零
        if (amount > money) {
            Vout ownOut = buildVout(ownWallet.getAddress(), amount - money, userId, projectId);
            transaction.getOutList().add(ownOut);
        }

        //之前在ownUTXOList里的utxo将变成输入单元
        for (UTXO utxo : ownUTXOList) {
            //找出对应的钱包地址、公钥、私钥
            String address = utxo.getVout().getToAddress();

            PublicKey publicKey = ownWallet.getKeyPair().getPublic();
            PrivateKey privateKey = ownWallet.getKeyPair().getPrivate();

            //设置待加密文本
            String s = utxo.getPointer().getTxId() + utxo.getPointer().getN() + transaction.getOutList();
            //得到加密内容
            byte[] data = EccUtil.buildMessage(s);
            try {
                //产生数字签名
                byte[] signature = EccUtil.signData(ALGORITHM_NAME, data, privateKey);
                Vin vin = buildVin(signature, publicKey, utxo.getPointer(), userId, projectId);
                //加入到交易的输入单元
                transaction.getInList().add(vin);
            } catch (Exception e) {
                LOGGER.error("sign data error!!! toAddress:{},money:{},userId:{},projectId:{}",
                        toAddress, money, userId, projectId);
                throw new BaseException(400, "区块链服务出现异常!!");
            }

        }

        //区块都要标记为已被消费
        for (UTXO utxo : ownUTXOList) {
            utxo.setSpent(true);
        }
        //创建交易id,由交易内容的toString获取得到
        transaction.setId(Sha256Util.doubleSHA256(transaction.toString()));

        //设置项目信息
        transaction.setFromUserId(userId);
        transaction.setFormProjectId(projectId);

        //插入到交易池
        addTransaction(peer, transaction);
        //peer.getTransactionPool().put(transaction.getId(), transaction);
        return transaction;
    }

    /**
     * 验证交易
     *
     * @param peer
     * @param transaction
     * @return
     */
    @Override
    public boolean verifyTransaction(Peer peer, Transaction transaction) {
        //校验基本信息
        if (!verifyBaseMsg(transaction)) {
            return false;
        }
        //校验是否存在双重支付
        if (verifyDoublePayment(peer, transaction)) {
            return false;
        }

        //如果是创币交易,则不需要校验输入单元，因为其输入单元为空
        if (transaction.isCoinBase()) {
            return true;
        }

        for (Vin vin : transaction.getInList()) {
            //查找该输入单元对应的utxo，如果不存在该输入单元的utxo，则加入到孤立交易池中
            UTXO utxo = peer.getUTXOHashMap().get(vin.getToSpent());
            if (utxo == null) {
                peer.getOrphanPool().put(vin.getToSpent().getTxId(), transaction);
                return false;
            }
            //校验地址跟数字签名
            if (!verifyAddressAndSignature(vin, utxo, transaction.getOutList())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 验证地址跟签名
     *
     * @param vin
     * @param utxo
     * @param voutList
     * @return
     */
    private boolean verifyAddressAndSignature(Vin vin, UTXO utxo, List<Vout> voutList) {
        //根据输入单元的公钥生成地址
        String address = EccUtil.generateAddress(vin.getPublicKey().getEncoded());
        //如果地址不匹配，则校验失败
        if (!address.equals(utxo.getVout().getToAddress())) {
            return false;
        }
        boolean result = false;
        try {
            String s = utxo.getPointer().getTxId() + utxo.getPointer().getN() + voutList;
            //校验数字签名
            result = EccUtil.verifySign(ALGORITHM_NAME, EccUtil.buildMessage(s),
                    vin.getPublicKey(), vin.getSignature());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 校验基本信息
     *
     * @param transaction
     * @return
     */
    private boolean verifyBaseMsg(Transaction transaction) {
        //输入或者输出单元任一为空都是不符合标准要求
        if (ObjectUtils.isEmpty(transaction.getInList()) ||
                ObjectUtils.isEmpty(transaction.getOutList())) {
            return false;
        }
        return true;
    }

    /**
     * 校验是否存在双重支付
     *
     * @param peer
     * @param transaction
     * @return
     */
    private boolean verifyDoublePayment(Peer peer, Transaction transaction) {
        //判断交易池中是否已经存在该笔交易
        if (peer.getTransactionPool().containsKey(transaction.getId())) {
            return true;
        }

        //判断该交易单元的输入单元是否存在交易池中
        for (Vin vin : transaction.getInList()) {
            for (Transaction tx : peer.getTransactionPool().values()) {
                if (tx.getInList().contains(vin)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 创币交易
     *
     * @param peer
     * @param toAddress
     * @param userId
     * @param projectId
     * @param money
     * @return
     */
    @Override
    public Transaction createCoinBaseTransaction(Peer peer, String toAddress,
                                                 String userId, String projectId, long money) {
        Transaction transaction = new Transaction();
        List<Vin> vinList = new ArrayList<>();
        List<Vout> voutList = new ArrayList<>();

        //设置地址
        Vout vout = buildVout(peer.getWallet().getAddress(), money, userId, projectId);

        Vin vin = buildVin(generateRandomStr(32).getBytes(), null, null, userId, projectId);

        voutList.add(vout);
        vinList.add(vin);

        transaction.setInList(vinList);
        transaction.setOutList(voutList);
        //创币交易
        transaction.setCoinBase(true);
        transaction.setId(Sha256Util.doubleSHA256(transaction.toString()));

        transaction.setFromUserId(userId);
        transaction.setFormProjectId(projectId);

        //加入交易池
        addTransaction(peer, transaction);
        return transaction;
    }

    /**
     * 找到输入单元对应的定位指针
     *
     * @param txs
     * @return
     */
    @Override
    public List<Pointer> findVinPointerFromTxs(List<Transaction> txs) {
        if (txs.size() == 0) {
            return new ArrayList<>();
        }
        List<Pointer> pointers = new ArrayList<>();
        for (Transaction t : txs) {
            for (Vin vin : t.getInList()) {
                pointers.add(vin.getToSpent());
            }
        }
        return pointers;
    }

    /**
     * 返回被删除的utxo集合
     *
     * @param utxoHashMap
     * @param txs
     * @return
     */
    @Override
    public HashMap<Pointer, UTXO> removeSpentUTXOFromTxs(HashMap<Pointer, UTXO> utxoHashMap, List<Transaction> txs) {
        List<Pointer> pointers = findVinPointerFromTxs(txs);
        return utxoService.deleteUTXOByPointer(utxoHashMap, pointers);
    }

    /**
     * 添加utxo到utxo map
     *
     * @param utxoHashMap
     * @param txs
     */
    @Override
    public void addUTXOFromTxsToMap(HashMap<Pointer, UTXO> utxoHashMap, List<Transaction> txs) {
        List<UTXO> utxoList = findUTXOFromTxsInBlock(txs);
        utxoService.addUTXOToMap(utxoHashMap, utxoList);
    }

    /**
     * 找到输出单元的定位指针
     *
     * @param txs
     * @return
     */
    @Override
    public List<Pointer> findVoutPointerFromTxs(List<Transaction> txs) {
        List<Pointer> pointerList = new ArrayList<>();
        for (Transaction t : txs) {
            for (int i = 0; i < t.getOutList().size(); ++i) {
                pointerList.add(new Pointer(t.getId(), i));
            }
        }
        return pointerList;
    }

    /**
     * 删除交易池中的交易，并返回被删除的交易用于备份，后续可回滚
     *
     * @param pool
     * @param txs
     * @return
     */
    @Override
    public HashMap<String, Transaction> removeTransactionFromTransactionPool(HashMap<String, Transaction> pool,
                                                                             List<Transaction> txs) {
        HashMap<String, Transaction> deletedTransaction = new HashMap<>();
        for (Transaction t : txs) {
            if (pool.containsKey(t.getId())) {
                deletedTransaction.put(t.getId(), t);
                pool.remove(t.getId());
            }
        }
        return deletedTransaction;
    }

    /**
     * 插入交易到交易池
     *
     * @param peer
     * @param t
     */
    @Override
    public void addTransaction(Peer peer, Transaction t) {
        peer.getTransactionPool().put(t.getId(), t);
    }

    /**
     * 将区块中的交易中所有的vout封装成utxo
     *
     * @param txs
     * @return
     */
    private List<UTXO> findUTXOFromTxsInBlock(List<Transaction> txs) {
        List<UTXO> utxoList = new ArrayList<>();
        for (Transaction t : txs) {
            for (int i = 0; i < t.getOutList().size(); ++i) {
                //生成utxo
                UTXO utxo = new UTXO();
                utxo.setSpent(false);
                utxo.setVout(t.getOutList().get(i));
                utxo.setConfirmed(true);
                utxo.setCoinBase(t.isCoinBase());
                //设置指针
                utxo.setPointer(new Pointer(t.getId(), i));

                //设置项目信息
                utxo.setFormProjectId(t.getOutList().get(i).getFormProjectId());
                utxo.setFromUserId(t.getOutList().get(i).getFromUserId());

                utxoList.add(utxo);
            }
        }
        return utxoList;
    }


    private List<UTXO> findUTXOFromTxs(List<Transaction> txs) {
        List<UTXO> utxoList = new ArrayList<>();
        for (Transaction t : txs) {
            for (int i = 0; i < t.getOutList().size(); ++i) {
                UTXO utxo = new UTXO();
                utxo.setSpent(false);
                utxo.setVout(t.getOutList().get(i));
                utxo.setConfirmed(false);
                utxo.setCoinBase(t.isCoinBase());
                //设置指针
                utxo.setPointer(new Pointer(t.getId(), i));
                utxoList.add(utxo);
            }
        }
        return utxoList;
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

    /**
     * 构建vout
     *
     * @param toAddress
     * @param money
     * @param userId
     * @param projectId
     * @return
     */
    private Vout buildVout(String toAddress, long money, String userId, String projectId) {
        Vout vout = new Vout();
        vout.setFormProjectId(projectId);
        vout.setFromUserId(userId);
        vout.setMoney(money);
        vout.setToAddress(toAddress);
        return vout;
    }


    /**
     * 构建vin
     *
     * @param signature
     * @param publicKey
     * @param pointer
     * @param userId
     * @param projectId
     * @return
     */
    private Vin buildVin(byte[] signature, PublicKey publicKey, Pointer pointer, String userId, String projectId) {
        Vin vin = new Vin();
        vin.setSignature(signature);
        vin.setFromUserId(userId);
        vin.setFormProjectId(projectId);
        vin.setPublicKey(publicKey);
        vin.setToSpent(pointer);
        return vin;
    }

    public void setUtxoService(UTXOService utxoService) {
        this.utxoService = utxoService;
    }
}
