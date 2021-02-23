package com.gdut.fundraising;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan(basePackages = "com.gdut.fundraising.filter")
@MapperScan("com.gdut.fundraising.mapper")
@SpringBootApplication
public class FundraisingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FundraisingApplication.class, args);
    }

}
