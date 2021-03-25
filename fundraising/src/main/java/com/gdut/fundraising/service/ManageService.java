package com.gdut.fundraising.service;

import com.gdut.fundraising.dto.NodeQueryResult;
import com.gdut.fundraising.entities.FundFlowEntity;
import com.gdut.fundraising.entities.OrderTblEntity;
import com.gdut.fundraising.entities.ProjectTblEntity;
import com.gdut.fundraising.entities.SpendEntity;
import com.gdut.fundraising.exception.BaseException;


import java.util.List;
import java.util.Map;


public interface ManageService {


    Map readProjectList(String token, int pageIndex , int pageSize, int state);

    ProjectTblEntity readProjectDetail(String token, String projectId);

    Map setProjectState(String token, Integer nowState, Integer nextState, String projectId) throws BaseException;

    OrderTblEntity expenditure(String token, OrderTblEntity orderTblEntity);

    List<NodeQueryResult> readNodeList(String token);

    SpendEntity spend(String token, SpendEntity spendEntity);

    List<FundFlowEntity> readAllBlock(String token);

    List<FundFlowEntity> readUserContribution(String token,String userId);

    List<FundFlowEntity> readProjectAllFund(String token,String projectId);
}
