package com.gdut.fundraising.blockchain;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * 交易输入单元
 */
public class Vin {
    /**
     * 交易创建者的数字签名
     */
    byte[] signature;

    /**
     * 交易创建者者的公钥
     */
    private byte[] publicKey;

    /**
     * 输入单元的地址
     */
    private String address;

    /**
     * 指向交易创建者的UTXO
     */
    private Pointer toSpent;

    /**
     * 项目id
     */
    private String formProjectId;

    /**
     * 用户id
     */
    private String fromUserId;

//    /**
//     * 金额
//     */
//    private Long money;


    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public PublicKey getPublicKeyObject() {
        KeyFactory keyFactory = null;
        if (publicKey == null) {
            return null;
        }
        try {
            keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public Pointer getToSpent() {
        return toSpent;
    }

    public void setToSpent(Pointer toSpent) {
        this.toSpent = toSpent;
    }

    public String getFormProjectId() {
        return formProjectId;
    }

    public void setFormProjectId(String formProjectId) {
        this.formProjectId = formProjectId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

//    public Long getMoney() {
//        return money;
//    }
//
//    public void setMoney(Long money) {
//        this.money = money;
//    }
}
