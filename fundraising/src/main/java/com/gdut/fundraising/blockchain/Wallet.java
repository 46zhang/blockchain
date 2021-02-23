package com.gdut.fundraising.blockchain;

import java.security.KeyPair;
import java.security.PublicKey;

/**
 * 区块链钱包
 */
public class Wallet {
    /**
     * 密钥对列表
     */
    private KeyPair keyPair;

    /**
     * 钱包地址列表
     */
    private String address;


    /**
     * 产生钱包新的地址跟密钥对
     */
    public void generateKeyAndAddress() {
        KeyPair keyPair = generateKey();
        generateAddress(keyPair.getPublic());
    }

    /**
     * 产生密钥对
     */
    private KeyPair generateKey() {
        KeyPair keyPair = EccUtil.generateKeys();
        this.keyPair=keyPair;
        return keyPair;
    }

    /**
     * 产生新的钱包地址
     *
     * @param publicKey
     */
    private void generateAddress(PublicKey publicKey) {
        this.address= EccUtil.generateAddress(publicKey.getEncoded());
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
