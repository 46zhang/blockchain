package com.gdut.fundraising.service.impl;

import com.gdut.fundraising.dto.LoginResult;
import com.gdut.fundraising.dto.ReadDonationResult;
import com.gdut.fundraising.dto.ReadExpenditureResult;
import com.gdut.fundraising.dto.ReadListResult;
import com.gdut.fundraising.entities.GiftTblEntity;
import com.gdut.fundraising.entities.ProjectTblEntity;
import com.gdut.fundraising.entities.UserTblEntity;
import com.gdut.fundraising.exception.BaseException;
import com.gdut.fundraising.mapper.UserMapper;
import com.gdut.fundraising.service.UserService;
import com.gdut.fundraising.util.TokenUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Value("${static.photo}")
    String photo;

    @Value("${static.url}")
    String url;

    @Resource
    UserMapper userMapper;


    public Map<String, Object> register(UserTblEntity userTblEntity)   {
        //判断前端发过来的数据是否完整
        if(userTblEntity.notNullRegister() == null){
            if(userMapper.selectUserByPhone(userTblEntity.getUserPhone()) == 0){
                Map<String, Object> map = new HashMap<>();
                //生成token
                String token = TokenUtil.getToken(userTblEntity);
                userTblEntity.setUserToken(token);
                //生成uuid
                userTblEntity.setUserId(UUID.randomUUID().toString());
                userMapper.register(userTblEntity);
                map.put("userPhone", userTblEntity.getUserPhone());
                map.put("token", userTblEntity.getUserToken());
                return map;
            }
            else{
                throw new BaseException(400,"该手机号码已存在!");
            }
        }
        else{
            throw new BaseException(400,"部分数据为空!");
        }
    }

    public LoginResult login(UserTblEntity userTblEntity) throws BaseException {
        //通过手机号码和密码获取账户信息
        LoginResult loginResult = userMapper.login(userTblEntity);
        //判断数据库是否该账户
        if(loginResult != null){
            //判断是否为管理员
            if("root".equals(userTblEntity.getUserPhone())){
                loginResult.setUserManage(1);
            }
            //生产新的token
            String token = TokenUtil.getToken(userTblEntity);
            loginResult.setToken(token);
            //设置新的token
            userTblEntity.setUserToken(token);
            userMapper.updateToken(userTblEntity);
            return loginResult;
        }else{
            throw new BaseException(400, "手机号码或账号密码出错！");
        }
    }


    public ProjectTblEntity launch(String token, ProjectTblEntity projectTblEntity) throws BaseException {
        //获取账户信息
        UserTblEntity userTblEntity = userMapper.selectUserByToken(token);
        //判断账户是否存在
        if(userTblEntity != null){
            try {
                if(projectTblEntity.checkTime() != null)
                    throw new BaseException(400, "请求字段出错！");
                projectTblEntity.setUserId(userTblEntity.getUserId());
                projectTblEntity.setProjectId(UUID.randomUUID().toString());
                projectTblEntity.setProjectMoneyTarget(-1);
                //插入项目信息
                userMapper.launch(projectTblEntity);
                return projectTblEntity;
            }
            catch (Exception e){
                throw new BaseException(400, "请求字段出错！");
            }
        }
        else{
            throw new BaseException(400, "token认证失败！");
        }
    }

    public Map uploadPhoto(String token, MultipartFile file) throws BaseException {
        UserTblEntity userTblEntity = userMapper.selectUserByToken(token);
        //判断账户是否存在
        if(userTblEntity == null){
            throw new BaseException(400, "token认证失败！");
        }
        //判断用户是否发送了图片
        if (!file.isEmpty()) {
            BufferedOutputStream out = null;
            try {
                //使用图片生成md5值
                InputStream fileInputStream = file.getInputStream();
                String md5 = DigestUtils.md5Hex(fileInputStream);
                fileInputStream.close();
                //生成图片名字
                String strPath = this.photo + md5 + '.' + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);;
                File file2 = new File(strPath);
                if (!file2.getParentFile().exists()) {
                    boolean result = file2.getParentFile().mkdirs();
                    if (!result) {
                        throw new BaseException(400, "服务器文件操作失败！");
                    }
                }
                out = new BufferedOutputStream(
                        new FileOutputStream(file2));
                System.out.println(file.getName());
                out.write(file.getBytes());
                out.flush();
                String add = this.url + strPath;
                Map<String, String> ret = new HashMap<>();
                ret.put("url", add);
                out.close();
                return ret;
            }
            catch (BaseException e){
                throw e;
            }
            catch (Exception e){
                throw new BaseException(400,"上传图片失败！");
            }
        }
        throw new BaseException(400,"上传的图片为空！");
    }

    public Map readProjectList(int pageIndex , int pageSize){
        if(pageSize <= 0)
            throw new BaseException(400, "页大小错误！");
        if(pageIndex < 0)
            throw new BaseException(400, "页下标错误！");
        //获取总数据条数
        int totalPage = userMapper.projectCount();
        List<ReadListResult> project = userMapper.readProjectList(pageIndex, pageSize);
        Map<String, Object> res = new HashMap<>();
        res.put("totalPage", totalPage);
        res.put("pageSize", pageSize);
        res.put("pageIndex", pageIndex);
        res.put("project", project);
        return res;
    }

    public ProjectTblEntity readProjectDetail(String projectId){
        return userMapper.readProjectDetail(projectId);
    }

    @Transactional
    public Map contribution(String token, String projectId, int money) throws BaseException {
        UserTblEntity userTblEntity = userMapper.selectUserByToken(token);
        if(money <= 0)
            throw new BaseException(400,"捐款不能为负！");
        //判断账户是否存在
        if(userTblEntity != null){
            //生成捐款实体
            GiftTblEntity gift = new GiftTblEntity();
            gift.setGiftMoney(money);
            gift.setGiftTime((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
            gift.setProjectId(projectId);
            gift.setUserId(userTblEntity.getUserId());
            gift.setGiftId(UUID.randomUUID().toString());
            //更新捐款的项目内容
            if(userMapper.contributionUpdateProject(gift.getGiftMoney(),gift.getProjectId()) != 1){
                throw new BaseException(400, "项目未找到或项目不在募捐状态！");
            }
            //插入捐款记录
            userMapper.contributionUpdateGiftTbl(gift);
            Map<String, Object> ret = new HashMap<>();
            ret.put("money", gift.getGiftMoney());
            return ret;
        }
        else{
            throw new BaseException(400, "token认证失败！");
        }
    }

    public List<ReadDonationResult> readDonation(String projectId){
        return userMapper.readDonation(projectId);
    }

    public List<ReadExpenditureResult> readExpenditureResult(String projectId){
        return userMapper.readExpenditureResult(projectId);
    }
}
