package com.gdut.fundraising.blockchain;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.spec.ECGenParameterSpec;

/**
 * 椭圆曲线加密工具类
 * 用来产生密钥以及产生、验证数字签名
 */
public class EccUtil {

    /**
     * 产生密钥
     *
     * @return
     */
    public static KeyPair generateKeys() {
        KeyPairGenerator keyPairGenerator = null;
        ECGenParameterSpec ecGenParameterSpec = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("EC");
            //secp256k1算法
            ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
            keyPairGenerator.initialize(ecGenParameterSpec, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;

    }

    /**
     * 产生数字签名
     *
     * @param algorithm
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] signData(String algorithm, byte[] data, PrivateKey key) throws Exception {
        Signature signer = Signature.getInstance(algorithm);
        signer.initSign(key);
        signer.update(data);
        return (signer.sign());
    }

    /**
     * 校验签名
     *
     * @param algorithm
     * @param data
     * @param key
     * @param sig
     * @return
     * @throws Exception
     */
    public static boolean verifySign(String algorithm, byte[] data, PublicKey key, byte[] sig) throws Exception {
        Signature signer = Signature.getInstance(algorithm);
        signer.initVerify(key);
        signer.update(data);
        return (signer.verify(sig));
    }


    /**
     * 获取钱包地址
     * <P>先用sha26加密</>
     * <P>再用ripeMD160加密</P>
     * <p>添加0x00到字节头</p>
     * <p>BASE58编码</p>
     *
     * @return
     */
    public static String generateAddress(byte[] publicKey) {
        MessageDigest sha = null;
        String result = "";
        try {
            sha = MessageDigest.getInstance("SHA-256");

            byte[] s1 = sha.digest(publicKey);
//
//            s1 = sha.digest(s1);
//            System.out.println("  sha2: " + byte2HexString(s1).toUpperCase());

            //加入BouncyCastleProvider的支持
            Security.addProvider(new BouncyCastleProvider());

            MessageDigest rmd = MessageDigest.getInstance("RipeMD160");
            byte[] r1 = rmd.digest(s1);

            //r2开头填充0x00
            byte[] r2 = new byte[r1.length + 1];
            r2[0] = 0;
            for (int i = 0; i < r1.length; ++i) {
                r2[i + 1] = r1[i];
            }
//            System.out.println("  rmd: " + byte2HexString(r2).toUpperCase());
//
//            System.out.println("  adr: " + Base58.encode(r2));
            result= Base58.encode(r2);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * byte转String
     *
     * @param b
     * @return
     */
    public static String byte2HexString(byte[] b) {
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

    /**
     * 产生加密信息内容
     *
     * @param s
     * @return
     */
    public static byte[] buildMessage(String s) {
        return Sha256Util.doubleSHA256(s).getBytes();
    }

//    @Test
//    public void testSignVerify() throws Exception {
//        // 需要签名的数据
//        byte[] data = new byte[1000];
//        for (int i = 0; i < data.length; i++)
//            data[i] = 0xa;
//
//        // 生成秘钥，在实际业务中，应该加载秘钥
//        KeyPair keyPair = generateKeys();
//        //KeyPair keyPair = KeyUtil.createKeyPairGenerator("secp256k1");
//        PublicKey publicKey1 = keyPair.getPublic();
//        PrivateKey privateKey1 = keyPair.getPrivate();
//
//        // 生成第二对秘钥，用于测试
//        keyPair = generateKeys();
//        // keyPair = KeyUtil.createKeyPairGenerator("secp256k1");
//        PublicKey publicKey2 = keyPair.getPublic();
//        PrivateKey privateKey2 = keyPair.getPrivate();
//
//        // 计算签名
//        byte[] sign1 = signData("SHA256withECDSA", data, privateKey1);
//        byte[] sign2 = signData("SHA256withECDSA", data, privateKey1);
//
//        // sign1和sign2的内容不同，因为ECDSA在计算的时候，加入了随机数k，因此每次的值不一样
//        // 随机数k需要保密，并且每次不同
//
//
//        // 用对应的公钥验证签名，必须返回true
//        Assert.isTrue(verifySign("SHA256withECDSA", data, publicKey1, sign1), "error");
//        // 数据被篡改，返回false
//        data[1] = 0xb;
//        Assert.isTrue(!verifySign("SHA256withECDSA", data, publicKey1, sign1), "error");
//        data[1] = 0xa;
//
//        Assert.isTrue(verifySign("SHA256withECDSA", data, publicKey1, sign1), "error");
//        // 签名被篡改，返回false
//        // 签名为DER格式，前三个字节是标识和数据长度，如果修改了这三个会抛出异常，无效签名格式
//        sign1[20] = (byte) ~sign1[20];
//        Assert.isTrue(!verifySign("SHA256withECDSA", data, publicKey1, sign1), "error");
//
//        // 使用其他公钥验证，返回false
//        Assert.isTrue(!verifySign("SHA256withECDSA", data, publicKey2, sign1), "error");
//    }


}
