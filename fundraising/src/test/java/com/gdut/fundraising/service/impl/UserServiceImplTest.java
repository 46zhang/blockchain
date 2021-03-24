package com.gdut.fundraising.service.impl;

import com.gdut.fundraising.entities.UserTblEntity;
import com.gdut.fundraising.exception.BaseException;
import com.gdut.fundraising.mapper.UserMapper;
import com.gdut.fundraising.service.BCTService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserMapper userMapper;


    @Mock
    BCTService BCTService;


    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testContribution() {
        String token = "sfsgsg";
        String projectId = "3424234";
        int money = 100;
        UserTblEntity userTblEntity = new UserTblEntity();
        userTblEntity.setUserAddress("xxxx");
        userTblEntity.setUserId("1241234234234234");
        userTblEntity.setUserName("jense");
        userTblEntity.setUserToken(token);
        userTblEntity.setUserPhone("342342434");
        when(userMapper.contributionUpdateProject(anyDouble(), anyString())).thenReturn(1);
        when(userMapper.selectUserByToken(anyString())).thenReturn(userTblEntity);
        when(BCTService.contribution(userTblEntity.getUserId(), projectId, money)).thenReturn(true);
        Map res = userService.contribution(token, projectId, money);
        Assert.assertTrue(res.containsKey("money"));
        assertEquals((long) (Long) res.get("money"), 100L);
    }


    @Test
    public void testContributionNotFindProject() {
        String token = "sfsgsg";
        String projectId = "3424234";
        int money = 100;
        UserTblEntity userTblEntity = new UserTblEntity();
        userTblEntity.setUserAddress("xxxx");
        userTblEntity.setUserId("1241234234234234");
        userTblEntity.setUserName("jense");
        userTblEntity.setUserToken(token);
        userTblEntity.setUserPhone("342342434");
        when(userMapper.contributionUpdateProject(anyDouble(), anyString())).thenReturn(2);
        when(userMapper.selectUserByToken(anyString())).thenReturn(userTblEntity);
        when(BCTService.contribution(userTblEntity.getUserId(), projectId, money)).thenReturn(true);
        try {
            userService.contribution(token, projectId, money);
        }catch (BaseException e){
            Assert.assertTrue(e.getCode().equals(400));
        }
    }
}