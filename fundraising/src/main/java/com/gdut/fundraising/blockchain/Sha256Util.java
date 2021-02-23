package com.gdut.fundraising.blockchain;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256Util {

    /**
     * 利用java原生的类实现SHA256加密
     * @param str 加密后的报文
     * @return
     */
    public static String getSHA256(String str){
        MessageDigest messageDigest;
        String encodestr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodestr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    /**
     * 俩次SHA256加密
     * @param str
     * @return
     */
    public static String doubleSHA256(String str){
        return getSHA256(getSHA256(str));
    }

    /**
     * 将byte转为16进制
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes){
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i=0;i<bytes.length;i++){
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length()==1){
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    public static void main(String[] args) {
        System.out.println( getSHA256("11132423fasdfsdfgsdg"));

        System.out.println(getSHA256(getSHA256("11132423fasdfsdfgsdg")));
        System.out.println(doubleSHA256("11132423fasdfsdfgsdg"));
    }
}
