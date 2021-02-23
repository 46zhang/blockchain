package com.gdut.fundraising;


import com.gdut.fundraising.dto.ReadDonationResult;
import com.gdut.fundraising.mapper.UserMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


class FundraisingApplicationTests {

    @Resource
    UserMapper userMapper;
    @Test
    void contextLoads() throws IOException {
        char s = (char)200;
        System.out.println(s);
    }

}
