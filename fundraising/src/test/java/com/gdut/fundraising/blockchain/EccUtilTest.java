package com.gdut.fundraising.blockchain;

import com.gdut.fundraising.blockchain.EccUtil;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

class EccUtilTest {

    @Test
    void generateKeys() {
        KeyPair keyPair = EccUtil.generateKeys();
        byte[] pk = keyPair.getPublic().getEncoded();
        byte[] sk = keyPair.getPrivate().getEncoded();

        KeyFactory keyFactory = null;
        PublicKey newPk=null;
        PrivateKey newSk=null;
        try {
            keyFactory = KeyFactory.getInstance("EC");
            newPk=keyFactory.generatePublic(new X509EncodedKeySpec(pk));
            newSk= keyFactory.generatePrivate(new PKCS8EncodedKeySpec(sk));

//            System.out.println(byte2HexString(pk));
//            System.out.println(byte2HexString(newPk.getEncoded()));
//            System.out.println(byte2HexString(sk));
//            System.out.println(byte2HexString(newSk.getEncoded()));

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        KeyPair keyPair1 = new KeyPair(newPk,newSk);
        Assert.assertTrue(StringUtils.equals(byte2HexString(keyPair1.getPublic().getEncoded()),byte2HexString(pk)));
        Assert.assertTrue(StringUtils.equals(byte2HexString(keyPair1.getPrivate().getEncoded()),byte2HexString(sk)));
    }

    /**
     * byte转String
     *
     * @param b
     * @return
     */
    private String byte2HexString(byte[] b) {
        String a = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            a = a + hex;
        }

        return a;
    }
}