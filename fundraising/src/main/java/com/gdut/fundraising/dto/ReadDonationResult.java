package com.gdut.fundraising.dto;

import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class ReadDonationResult {
    /**
     * 用户的名字
     */
    private String userName;
    /**
     * 募捐的id
     */
    private String giftId;
    /**
     * 募捐的钱数
     */
    private double giftMoney;
    /**
     * 项目的id
     */
    private String projectId;
    /**
     * 募捐的时间  Date
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String giftTime;

    public String getUserName() {
        String reg = ".{1}";
        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(userName);
        int i = 0;
        while(m.find()){
            i++;
            if(i==1)
                continue;
            m.appendReplacement(sb, "*");
        }
        m.appendTail(sb);
        return sb.toString();
    }
    public String getGiftTime()  {
        return giftTime.substring(0, 19);
    }
}
