package com.gdut.fundraising.blockchain;

import com.gdut.fundraising.util.FileUtils;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.security.*;
import java.security.cert.CertificateFactory;

public class ExportPrivateKey {
    private File keystoreFile;
    private String keyStoreType;
    private char[] password;
    private String alias;
    private File exportedFilePrivate;
    private File exportedFilePublic;

    public static KeyPair getKeyPair(KeyStore keystore, String alias, char[] password) {
        try {
            Key key=keystore.getKey(alias,password);
            if(key instanceof PrivateKey) {
                Certificate cert= (Certificate) keystore.getCertificate(alias);
                PublicKey publicKey=cert.getPublicKey();
                return new KeyPair(publicKey,(PrivateKey)key);
            }
        } catch (UnrecoverableKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (KeyStoreException e) {
        }
        return null;
    }

    public void export() throws Exception{
        KeyStore keystore=KeyStore.getInstance(keyStoreType);
        BASE64Encoder encoder=new BASE64Encoder();
        keystore.load(new FileInputStream(keystoreFile),password);
        KeyPair keyPair=getKeyPair(keystore,alias,password);

        //提取私钥
        PrivateKey privateKey=keyPair.getPrivate();
        String encoded=encoder.encode(privateKey.getEncoded());
        FileWriter fw=new FileWriter(exportedFilePrivate);
        //fw.write("—–BEGIN PRIVATE KEY—–\n");
        fw.write(encoded);
        //fw.write("\n");
        //fw.write("—–END PRIVATE KEY—–");
        fw.close();

        //提取公钥
        PublicKey publickey=keyPair.getPublic();
        String encoded2=encoder.encode(publickey.getEncoded());
        FileWriter fw2=new FileWriter(exportedFilePublic);
        //fw2.write("—–BEGIN PRIVATE KEY—–\n");
        fw2.write(encoded2);
        //fw2.write("\n");
        //fw2.write("—–END PRIVATE KEY—–");
        fw2.close();

        System.out.println("私钥encoded:"+privateKey.getEncoded());
        System.out.println("私钥encoded2:"+encoded);
        System.out.println("公钥encoded:"+publickey.getEncoded());
        System.out.println("公钥encoded2:"+encoded2);
    }
    public static void main(String args[]) throws Exception{
        ExportPrivateKey export=new ExportPrivateKey();
        export.keystoreFile=new File(FileUtils.getRootFilePath()+"\\mytestkey.jks");
        export.keyStoreType="JKS";
        export.password="123456".toCharArray();
        export.alias="certificatekey";
        export.exportedFilePrivate=new File(FileUtils.getRootFilePath()+"\\privateKey.PEM");
        export.exportedFilePublic=new File(FileUtils.getRootFilePath()+"\\publicKey.PEM");
        //export.export();
        export.testvaliate();
    }

    /*怎么来验证提取的私钥是否正确呢?(因为公钥私钥必须成对出现,我们可以通过证书提取去公钥,然后用公钥加密,使用刚刚获得的私钥解密)
      提取.cer证书的方法:
                  keytool -export -alias 别名 -keystore f:/keystore.jks -file server.cer
    */
    public void testvaliate(){
        try {
            //提取私钥
            KeyStore keystore=KeyStore.getInstance(keyStoreType);
            keystore.load(new FileInputStream(keystoreFile),password);
            BASE64Encoder encoder=new BASE64Encoder();
            KeyPair keyPair=getKeyPair(keystore,alias,password);
            PrivateKey privateKey=keyPair.getPrivate();

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            FileInputStream in = new FileInputStream("f:/server.cer");

            //生成一个证书对象并使用从输入流 inStream 中读取的数据对它进行初始化。
            Certificate c = (Certificate) cf.generateCertificate(in);
            PublicKey publicKey = c.getPublicKey();


            //通过下面这段代码提取的私钥是否正确
            String before = "asdf";
            byte[] plainText = before.getBytes("UTF-8");
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            // 用公钥进行加密，返回一个字节流
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] cipherText = cipher.doFinal(plainText);

            // 用私钥进行解密，返回一个字节流
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] newPlainText = cipher.doFinal(cipherText);
            System.out.println(new String(newPlainText, "UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
