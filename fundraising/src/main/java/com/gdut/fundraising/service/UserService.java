package com.gdut.fundraising.service;

import com.gdut.fundraising.dto.*;
import com.gdut.fundraising.entities.FundFlowEntity;
import com.gdut.fundraising.entities.ProjectTblEntity;
import com.gdut.fundraising.entities.UserTblEntity;
import com.gdut.fundraising.exception.BaseException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {

    Map<String, Object> register(UserTblEntity userTblEntity);

    LoginResult login(UserTblEntity userTblEntity) throws BaseException;


    ProjectTblEntity launch(String token, ProjectTblEntity projectTblEntity) throws BaseException;

    Map uploadPhoto(String token, MultipartFile file) throws BaseException;

    Map readProjectList(int pageIndex , int pageSize);

    ProjectTblEntity readProjectDetail(String projectId);

    List<NodeQueryResult> readNodeList();

    List<UserTblEntity> readUserList();


    @Transactional
    Map contribution(String token, String projectId, int money) throws BaseException;

    List<ReadDonationResult> readDonation(String projectId);

    List<ReadExpenditureResult> readExpenditureResult(String projectId);

    List<FundFlowEntity> getUserContribution(String token, String userId);

    List<FundFlowEntity> getUserOneProjectFund(String token, String projectId, String userId);

    FundFlowGraphResult getUserOneProjectFundGraph(String token, String projectId, String userId);
}
