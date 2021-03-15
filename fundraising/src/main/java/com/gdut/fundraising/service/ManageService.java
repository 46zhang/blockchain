package com.gdut.fundraising.service;

import com.gdut.fundraising.entities.OrderTblEntity;
import com.gdut.fundraising.entities.ProjectTblEntity;
import com.gdut.fundraising.entities.SpendEntity;
import com.gdut.fundraising.exception.BaseException;



import java.util.Map;


public interface ManageService {


    Map readProjectList(String token, int pageIndex , int pageSize, int state);

    ProjectTblEntity readProjectDetail(String token, String projectId);

    Map setProjectState(String token, Integer nowState, Integer nextState, String projectId) throws BaseException;

    OrderTblEntity expenditure(String token, OrderTblEntity orderTblEntity);


    SpendEntity spend(String token, SpendEntity spendEntity);
}
