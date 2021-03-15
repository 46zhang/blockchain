package com.gdut.fundraising;


import com.gdut.fundraising.dto.ReadDonationResult;
import com.gdut.fundraising.dto.raft.VoteRequest;
import com.gdut.fundraising.mapper.UserMapper;

import com.gdut.fundraising.util.FileUtils;
import com.gdut.fundraising.util.NetworkUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


class FundraisingApplicationTests {

    @Resource
    UserMapper userMapper;
    @Test
    void contextLoads() throws IOException {
        char s = (char)200;
        System.out.println(s);
    }

    @Test
    void sendData(){
        String url="http://localhost:8091"+"/fundraising/node/send";
        String pathName= FileUtils.buildPath(FileUtils.getRootFilePath(),"10000");
        FileUtils.createDir(pathName);
        int count=0;
        while(true) {
            try {
                com.gdut.fundraising.entities.raft.Test t=new com.gdut.fundraising.entities.raft.Test();
                String data=new Date().toString();
                t.setData(data);
                String res= NetworkUtils.postByHttp(url,t);
                System.out.println(res);
                FileUtils.write(FileUtils.buildPath(pathName, count +".json"),data);
                ++count;
                //1min发一次
                Thread.sleep(60000);


            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
