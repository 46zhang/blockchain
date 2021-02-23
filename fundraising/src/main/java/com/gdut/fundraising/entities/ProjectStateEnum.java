package com.gdut.fundraising.entities;

/**
 * 活动状态机
 */

public enum ProjectStateEnum {
    /**
     * 活动初始化,用户创建活动默认就进入初始化阶段
     */
    INIT(0,"初始化"),
    /**
     * 找个状态是否不需要捏？因为我们也没去做项目结束时间判断？
     */
    OVER(5,"项目结束"),
    /**
     * 管理员审核通过
     */
    MANUAL_PASS(2,"审核成功"),
    /**
     * 审核失败
     */
    MANUAL_FAIL(6,"审核失败");

    ProjectStateEnum(int code, String desc){
        this.code= code ;
        this.desc=desc;
    }
    /**
     * 活动状态码
     */
    private int code;

    /**
     * 活动描述
     */
    private String desc;


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
