package com.gdut.fundraising.blockchain;

import com.gdut.fundraising.util.FileUtils;
import sun.misc.BASE64Encoder;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class ExportCert {
    //导出证书 base64格式
    public static void exportCert(KeyStore keystore, String alias, String exportFile) throws Exception {
        Certificate cert = keystore.getCertificate(alias);
        BASE64Encoder encoder = new BASE64Encoder();
        String encoded = encoder.encode(cert.getEncoded());
        FileWriter fw = new FileWriter(exportFile);
        fw.write("-----BEGIN CERTIFICATE-----\r\n");	//非必须
        fw.write(encoded);
        fw.write("\r\n-----END CERTIFICATE-----");	//非必须
        fw.close();
    }

    //得到KeyPair
    public static KeyPair getKeyPair(KeyStore keystore, String alias,char[] password) {
        try {
            Key key = keystore.getKey(alias, password);
            if (key instanceof PrivateKey) {
                Certificate cert = keystore.getCertificate(alias);
                PublicKey publicKey = cert.getPublicKey();
                return new KeyPair(publicKey, (PrivateKey) key);
            }
        } catch (UnrecoverableKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (KeyStoreException e) {
        }
        return null;
    }

    //导出私钥
    public static void exportPrivateKey(PrivateKey privateKey,String exportFile) throws Exception {
        BASE64Encoder encoder = new BASE64Encoder();
        String encoded = encoder.encode(privateKey.getEncoded());
        FileWriter fw = new FileWriter(exportFile);
        fw.write("—–BEGIN PRIVATE KEY—–\r\n");	//非必须
        fw.write(encoded);
        fw.write("\r\n—–END PRIVATE KEY—–");		//非必须
        fw.close();
    }

    //导出公钥
    public static void exportPublicKey(PublicKey publicKey,String exportFile) throws Exception {
        BASE64Encoder encoder = new BASE64Encoder();
        String encoded = encoder.encode(publicKey.getEncoded());
        FileWriter fw = new FileWriter(exportFile);
        fw.write("—–BEGIN PUBLIC KEY—–\r\n");		//非必须
        fw.write(encoded);
        fw.write("\r\n—–END PUBLIC KEY—–");		//非必须
        fw.close();
    }

    public static void main(String args[]) throws Exception {

        try{

            KeyStore keyStore = KeyStore.getInstance("JKS");

            keyStore.load(null,null);

            keyStore.store(new FileOutputStream(FileUtils.getRootFilePath()+"\\mytestkey.jks"), "password".toCharArray());

        }catch(Exception ex){

            ex.printStackTrace();

        }


        try{

            KeyStore keyStore = KeyStore.getInstance("JKS");

            keyStore.load(new FileInputStream("mytestkey.jks"),"password".toCharArray());

            CertAndKeyGen gen = new CertAndKeyGen("RSA","SHA1WithRSA");

            gen.generate(1024);

            Key key=gen.getPrivateKey();

            X509Certificate cert=gen.getSelfCertificate(new X500Name("CN=ROOT"), (long)365*24*3600);

            X509Certificate[] chain = new X509Certificate[1];

            chain[0]=cert;

            keyStore.setKeyEntry("mykey", key, "password".toCharArray(), chain);

            keyStore.store(new FileOutputStream("mytestkey.jks"), "password".toCharArray());

        }catch(Exception ex){

            ex.printStackTrace();

        }
    }

}
