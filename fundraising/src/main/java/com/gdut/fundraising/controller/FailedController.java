package com.gdut.fundraising.controller;


import com.gdut.fundraising.exception.BaseException;
import com.gdut.fundraising.util.JsonResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@ControllerAdvice//全局异常处理
@ResponseBody//返回json
@RestController
public class FailedController {
    //转发filter处产生的错误
    @RequestMapping("/tokenFailed")
    public JsonResult requestFailed() throws BaseException {
        throw new BaseException(400, "未携带token！");
    }
    //转发filter处产生的错误
    @RequestMapping("/contentTypeFailed")
    public JsonResult contentTypeFailed() throws BaseException {
        throw new BaseException(400, "content-type未设置！");
    }

}
