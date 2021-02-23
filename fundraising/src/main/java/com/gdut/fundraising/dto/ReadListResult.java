package com.gdut.fundraising.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadListResult {
    /**
     * 项目的id
     */
    private String projectId;
    /**
     * 项目的名字
     */
    private String projectName;
    /**
     * 募捐的人数次
     */
    private int projectPeopleNums;
    /**
     * 目前获得的钱数
     */
    private double projectMoneyNow;
    /**
     * 项目的图片链接
     */
    private String projectPhoto;
}
