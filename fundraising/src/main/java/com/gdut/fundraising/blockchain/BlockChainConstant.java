package com.gdut.fundraising.blockchain;

public class BlockChainConstant {
    /**
     * 手续费0分钱
     */
    public static long FEE = 0;


    /**
     * 创币交易，一开始给1000元
     */
    public static long INIT_MONEY=100000;

    /**
     * 版本
     */
    public static String VERSION="1.0.0";

    /**
     * 是否允许从有效交易中提取UTXO创建交易
     */
    public static boolean ALLOW_UTXO_FORM_POOL=true;


}
