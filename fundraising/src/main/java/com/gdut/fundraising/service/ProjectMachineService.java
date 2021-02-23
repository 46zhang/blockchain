package com.gdut.fundraising.service;


/**
 * 活动状态管理
 */
public interface ProjectMachineService {

    /***
     * 判断是否可推进到下一个状态
     * @param nowState 当前状态
     * @param nextState 下一个 状态
     * @return
     */
    boolean checkNextState(Integer nowState, Integer nextState);
}
