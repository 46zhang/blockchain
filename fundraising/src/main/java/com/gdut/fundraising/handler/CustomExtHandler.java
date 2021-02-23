package com.gdut.fundraising.handler;

import com.gdut.fundraising.exception.BaseException;

import com.gdut.fundraising.util.JsonResult;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.gdut.fundraising.constant.Status.UNKNOWN_ERROR;

@RestControllerAdvice
public class CustomExtHandler {

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    Map HttpMessageNotReadableException(HttpRequestMethodNotSupportedException e, HttpServletRequest request){
        e.printStackTrace();
        return JsonResult.error(400, "请求方法错误！").result();
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    Map HttpMessageNotReadableException(Exception e, HttpServletRequest request){
        e.printStackTrace();
        return JsonResult.error(400, "请求参数错误！").result();
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    Map MissingServletRequestParameterException(Exception e, HttpServletRequest request){
        e.printStackTrace();
        return JsonResult.error(400, "请求字段错误！").result();
    }

    @ExceptionHandler(value = BaseException.class)
    Map MyWebException(BaseException e, HttpServletRequest request){
        e.printStackTrace();
        return JsonResult.error(e.getCode(),e.getMessage()).result();
    }

    @ExceptionHandler(value = Exception.class)
    Map Exception(Exception e, HttpServletRequest request){
        e.printStackTrace();
        return JsonResult.error(UNKNOWN_ERROR.getCode(),UNKNOWN_ERROR.getMessage()).result();
    }

}
