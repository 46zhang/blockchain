package com.gdut.fundraising.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectTblEntity {
    /**
     * 发起项目的用户的id UUID生成
     */
    private String userId;
    /**
     * 项目的id UUID生成
     * 主键
     */
    private String projectId;
    /**
     * 项目开始时间  Date类型
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String projectStartTime;
    /**
     * 项目结束时间  Date类型
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String projectFinishTime;
    /**
     * 项目的状态
     * 状态：0-发起；1-审核；6-审核失败；2-审核成功；3-募捐；4-执行；5-项目结束
     * 状态机：0->1->2->3->4->5;
     *       1->6
     */
    private int projectState;
    /**
     * 项目名字
     */
    private String projectName;
    /**
     * 募捐人次
     */
    private int projectPeopleNums;
    /**
     * 项目目标募捐钱数
     */
    private double projectMoneyTarget;
    /**
     * 项目当前获得钱数
     */
    private double projectMoneyNow;
    /**
     * 项目图片链接
     */
    private String projectPhoto;
    /**
     * 项目简介
     */
    private String projectExplain;

    public String getProjectStartTime(){
        return projectStartTime.substring(0, 19);
    }
    public String getProjectFinishTime(){
        return projectFinishTime.substring(0, 19);
    }

    public String checkTime(){
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date1 = formatter.parse(projectStartTime);
            Date date2 = formatter.parse(projectFinishTime);
            if(!projectStartTime.equals(formatter.format(date1) ) || !projectFinishTime.equals(formatter.format(date2)) || date1.after(date2))
                return "time false";
        } catch (Exception e) {
            return "time false";
        }

        return null;
    }

}
