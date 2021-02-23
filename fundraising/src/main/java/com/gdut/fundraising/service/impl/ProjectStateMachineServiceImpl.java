package com.gdut.fundraising.service.impl;

import com.gdut.fundraising.entities.ProjectStateEnum;
import com.gdut.fundraising.exception.BaseException;
import com.gdut.fundraising.service.ProjectMachineService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 项目状态机管理服务
 */
@Service
public class ProjectStateMachineServiceImpl implements ProjectMachineService {

    private HashMap<Integer, HashSet<Integer>> stateMap = new HashMap<Integer,
            HashSet<Integer>>() {
        {
            //项目初始化可推进的状态为项目审核成功或者项目审核失败
            put(ProjectStateEnum.INIT.getCode(),new HashSet<Integer>(){
                {
                    add(ProjectStateEnum.MANUAL_FAIL.getCode());
                    add(ProjectStateEnum.MANUAL_PASS.getCode());
                }
            });

            //项目审核成功可推进的状态为项目结束
            put(ProjectStateEnum.MANUAL_PASS.getCode(),new HashSet<Integer>(){
                {
                    add(ProjectStateEnum.OVER.getCode());
                }
            });

        }
    };

    /**
     * 判断是否可以推进到下一个状态
     * @param nowState  当前状态
     * @param nextState 下一个 状态
     * @return
     */
    @Override
    public boolean checkNextState(Integer nowState, Integer nextState) {
        if(!stateMap.containsKey(nowState)){
            throw new BaseException(400, "状态错误！");
        }
        HashSet<Integer> projectStateEnumHashSet = stateMap.get(nowState);
        if(projectStateEnumHashSet.contains(nextState)){
            return true;
        }
        return false;
    }
}
