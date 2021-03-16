package com.gdut.fundraising.service.impl;

import com.gdut.fundraising.entities.SpendEntity;
import com.gdut.fundraising.entities.UserTblEntity;
import com.gdut.fundraising.exception.BaseException;
import com.gdut.fundraising.mapper.ManageMapper;
import com.gdut.fundraising.service.BlockChainService;
import com.gdut.fundraising.service.ProjectMachineService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.annotation.Resource;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class ManageServiceImplTest {

    @InjectMocks
    ManageServiceImpl manageService;

    @Mock
    BlockChainService blockChainService;

    @Mock
    ManageMapper manageMapper;

    @Mock
    ProjectMachineService projectMachineService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testSpend() {
        String token = "sfsdfsfsdf";
        String userId = "root34324234";
        SpendEntity spendEntity = new SpendEntity();
        spendEntity.setFormUserId(userId);
        spendEntity.setMoney(100L);
        spendEntity.setProjectId("x4235245345");
        spendEntity.setToAddress("xxxx");


        UserTblEntity userTblEntity = new UserTblEntity();
        userTblEntity.setUserAddress("xxxx");
        userTblEntity.setUserId(userId);
        userTblEntity.setUserName("jense");
        userTblEntity.setUserToken(token);
        userTblEntity.setUserPhone("342342434");
        when(manageMapper.selectUserByToken(token)).thenReturn(userTblEntity);
        when(blockChainService.useMoney(spendEntity)).thenReturn(true);

        SpendEntity res = manageService.spend(token, spendEntity);
        Assert.assertEquals(res.getFormUserId(),spendEntity.getFormUserId());
        Assert.assertEquals(res.getToAddress(),spendEntity.getToAddress());
        Assert.assertEquals(res.getMoney(),spendEntity.getMoney());
        Assert.assertEquals(res.getProjectId(),spendEntity.getProjectId());
    }


    @Test
    public void testSpendTokenError() {
        String token = "sfsdfsfsdf";
        String userId = "roo34324234";
        SpendEntity spendEntity = new SpendEntity();
        spendEntity.setFormUserId(userId);
        spendEntity.setMoney(100L);
        spendEntity.setProjectId("x4235245345");
        spendEntity.setToAddress("xxxx");


        UserTblEntity userTblEntity = new UserTblEntity();
        userTblEntity.setUserAddress("xxxx");
        userTblEntity.setUserId(userId);
        userTblEntity.setUserName("jense");
        userTblEntity.setUserToken(token);
        userTblEntity.setUserPhone("342342434");
        when(manageMapper.selectUserByToken(token)).thenReturn(userTblEntity);
        when(blockChainService.useMoney(spendEntity)).thenReturn(true);
        try {
            manageService.spend(token, spendEntity);
            Assert.fail("No exception");
        }catch (BaseException e){
            Assert.assertEquals(e.getMessage(),"token认证失败!");
        }

    }


    @Test
    public void testSpendBlockError() {
        String token = "sfsdfsfsdf";
        String userId = "root34324234";
        SpendEntity spendEntity = new SpendEntity();
        spendEntity.setFormUserId(userId);
        spendEntity.setMoney(100L);
        spendEntity.setProjectId("x4235245345");
        spendEntity.setToAddress("xxxx");


        UserTblEntity userTblEntity = new UserTblEntity();
        userTblEntity.setUserAddress("xxxx");
        userTblEntity.setUserId(userId);
        userTblEntity.setUserName("jense");
        userTblEntity.setUserToken(token);
        userTblEntity.setUserPhone("342342434");
        when(manageMapper.selectUserByToken(token)).thenReturn(userTblEntity);
        when(blockChainService.useMoney(spendEntity)).thenReturn(false);
        try {
            manageService.spend(token, spendEntity);
            Assert.fail("No exception");
        }catch (BaseException e){
            Assert.assertEquals(e.getMessage(),"区块链服务存在异常!");
        }

    }
}