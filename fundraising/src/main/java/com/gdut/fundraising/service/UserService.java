package com.gdut.fundraising.service;

import com.gdut.fundraising.dto.LoginResult;
import com.gdut.fundraising.dto.ReadDonationResult;
import com.gdut.fundraising.dto.ReadExpenditureResult;
import com.gdut.fundraising.entities.ProjectTblEntity;
import com.gdut.fundraising.entities.UserTblEntity;
import com.gdut.fundraising.exception.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

public interface UserService {

    Map<String, Object> register(UserTblEntity userTblEntity);

    LoginResult login(UserTblEntity userTblEntity) throws BaseException;


    ProjectTblEntity launch(String token, ProjectTblEntity projectTblEntity) throws BaseException;

    Map uploadPhoto(String token, MultipartFile file) throws BaseException;

    Map readProjectList(int pageIndex , int pageSize);

    ProjectTblEntity readProjectDetail(String projectId);

    @Transactional
    Map contribution(String token, String projectId, int money) throws BaseException;

    List<ReadDonationResult> readDonation(String projectId);

    List<ReadExpenditureResult> readExpenditureResult(String projectId);
}
