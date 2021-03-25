package com.gdut.fundraising.service.impl;


import com.gdut.fundraising.dto.NodeQueryResult;
import com.gdut.fundraising.dto.ReadListResult;
import com.gdut.fundraising.entities.*;
import com.gdut.fundraising.exception.BaseException;
import com.gdut.fundraising.mapper.ManageMapper;
import com.gdut.fundraising.service.BCTService;
import com.gdut.fundraising.service.ManageService;
import com.gdut.fundraising.service.ProjectMachineService;
import com.gdut.fundraising.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    BCTService BCTService;

    @Resource
    ManageMapper manageMapper;

    @Resource
    ProjectMachineService projectMachineService;


    public Map readProjectList(String token, int pageIndex, int pageSize, int state) {
        if (pageSize <= 0)
            throw new BaseException(400, "页大小错误！");
        if (pageIndex < 0)
            throw new BaseException(400, "页下标错误！");
        //根据pageSize生成页数
        UserTblEntity userTblEntity = manageMapper.selectUserByToken(token);
        if (userTblEntity != null && "root".equals(userTblEntity.getUserId().substring(0, 4))) {
            int totalPage = manageMapper.projectCount(state);
            List<ReadListResult> project = manageMapper.readProjectList(pageIndex, pageSize, state);
            Map<String, Object> res = new HashMap<>();
            res.put("totalPage", totalPage);
            res.put("pageSize", pageSize);
            res.put("pageIndex", pageIndex);
            res.put("project", project);
            switch (state) {
                case 0:
                    res.put("state", "发起");
                    break;
                case 2:
                    res.put("state", "审核成功");
                    break;
                case 5:
                    res.put("state", "结束");
                    break;
                case 6:
                    res.put("state", "审核失败");
                    break;
                default:
                    res.put("state", "未知状态");
            }
            return res;
        } else {
            throw new BaseException(400, "token认证失败！");
        }
    }


    public ProjectTblEntity readProjectDetail(String token, String projectId) {
        UserTblEntity userTblEntity = manageMapper.selectUserByToken(token);
        if (userTblEntity != null && "root".equals(userTblEntity.getUserId().substring(0, 4))) {
            return manageMapper.readProjectDetail(projectId);
        } else {
            throw new BaseException(400, "token认证失败！");
        }
    }


    public List<NodeQueryResult> readNodeList(String token) {
        UserTblEntity userTblEntity = manageMapper.selectUserByToken(token);
        if (userTblEntity != null && "root".equals(userTblEntity.getUserId().substring(0, 4))) {
            return BCTService.getNodeQueryList();

        } else {
            throw new BaseException(400, "token认证失败！");
        }
    }


    public Map setProjectState(String token, Integer nowState, Integer nextState, String projectId) throws BaseException {
        if (nowState == null || nextState == null || projectId == null)
            throw new BaseException(400, "请求内容缺失！");
        UserTblEntity userTblEntity = manageMapper.selectUserByToken(token);
        if (userTblEntity != null && "root".equals(userTblEntity.getUserId().substring(0, 4))) {
            ProjectTblEntity project = manageMapper.selectProjectById(projectId);
            if (project == null)
                throw new BaseException(400, "项目未知！");
            if (nowState == project.getProjectState()) {
                if (projectMachineService.checkNextState(nowState, nextState)) {
                    if (manageMapper.SetProjectToNext(projectId, nowState, nextState) != 1)
                        throw new BaseException(500, "服务器设置项目状态出错！");
                    return new HashMap<String, Integer>() {
                        {
                            put("nowState", nextState);
                        }
                    };
                } else {
                    throw new BaseException(400, "项目不可设置该状态！");
                }
            } else {
                throw new BaseException(400, "项目当前状态错误！");
            }
        } else {
            throw new BaseException(400, "token认证失败！");
        }
    }


    public OrderTblEntity expenditure(String token, OrderTblEntity orderTblEntity) {
        UserTblEntity userTblEntity = manageMapper.selectUserByToken(token);
        if (userTblEntity != null && userTblEntity.getUserId().length() >= 4 && "root".equals(userTblEntity.getUserId().substring(0, 4))) {
            orderTblEntity.setOrderOperator(userTblEntity.getUserId());
            orderTblEntity.setOrderId(UUID.randomUUID().toString());
            orderTblEntity.setOrderTime((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
            if (orderTblEntity.getOrderMoney() <= 0) {
                throw new BaseException(400, "支出的金钱不可小于等于零！");
            }
            if (manageMapper.selectUserById(orderTblEntity) != 1) {
                throw new BaseException(400, "支出的目标不存在！");
            }
            if (manageMapper.selectProjectByIdAndUser(orderTblEntity) != 1) {
                throw new BaseException(400, "项目不存在或项目拥有者错误！");
            }
            if (manageMapper.expenditure(orderTblEntity) != 1) {
                throw new BaseException(500, "服务器存储数据出错！");
            }
            return orderTblEntity;
        } else {
            throw new BaseException(400, "token认证失败！");
        }
    }

    /**
     * 花费金额
     *
     * @param token
     * @param spendEntity
     * @return
     */
    public SpendEntity spend(String token, SpendEntity spendEntity) {
        UserTblEntity userTblEntity = manageMapper.selectUserByToken(token);
        if (userTblEntity != null && userTblEntity.getUserId().length() >= 4 && "root".equals(userTblEntity.getUserId().substring(0, 4))) {
            spendEntity.setOrderOperator(userTblEntity.getUserId());

            if (spendEntity.getMoney() <= 0) {
                throw new BaseException(400, "支出的金钱不可小于等于零!");
            }

            boolean res = BCTService.useMoney(spendEntity);
            if (!res) {
                throw new BaseException(400, "区块链服务存在异常!");
            }
            return spendEntity;
        } else {
            throw new BaseException(400, "token认证失败!");
        }
    }

    @Override
    public List<FundFlowEntity> readAllBlock(String token) {
        UserTblEntity userTblEntity = manageMapper.selectUserByToken(token);
        if (userTblEntity != null && userTblEntity.getUserId().length() >= 4 && "root".equals(userTblEntity.getUserId().substring(0, 4))) {
            return BCTService.getAllBlockFundFlow();
        } else {
            throw new BaseException(400, "token认证失败!");
        }
    }

    @Override
    public List<FundFlowEntity> readUserContribution(String token, String userId) {
        UserTblEntity userTblEntity = manageMapper.selectUserByToken(token);
        if (userTblEntity != null && userTblEntity.getUserId().length() >= 4 && "root".equals(userTblEntity.getUserId().substring(0, 4))) {
            return BCTService.getUserAllContributionFlow(userId);
        } else {
            throw new BaseException(400, "token认证失败!");
        }
    }

    @Override
    public List<FundFlowEntity> readProjectAllFund(String token, String projectId){
        UserTblEntity userTblEntity = manageMapper.selectUserByToken(token);
        if (userTblEntity != null && userTblEntity.getUserId().length() >= 4 && "root".equals(userTblEntity.getUserId().substring(0, 4))) {
            return BCTService.getProjectFundFlow(projectId);
        } else {
            throw new BaseException(400, "token认证失败!");
        }
    }


}
