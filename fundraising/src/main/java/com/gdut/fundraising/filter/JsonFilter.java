package com.gdut.fundraising.filter;

import org.springframework.core.Ordered;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(filterName = "JsonFilter", urlPatterns = {"/*"})
public class JsonFilter implements Filter, Ordered {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //判断是否为POST请求
        if("POST".equals(((HttpServletRequest)servletRequest).getMethod())){
            //判断是否为上传文件
            if(((HttpServletRequest) servletRequest).getRequestURL().indexOf("upload") != -1){
                //判断content-type是否正确
                if(((HttpServletRequest)servletRequest).getHeader("Content-Type").indexOf("multipart/form-data") == -1){
                    //跳转到错误输出方法
                    servletRequest.getRequestDispatcher("/contentTypeFailed").forward(servletRequest, servletResponse);
                }
                else{
                    filterChain.doFilter(servletRequest, servletResponse);
                }
                return ;
            }
            //判断content-type是否正确
            if(!"application/json".equals(servletRequest.getContentType())){
                //跳转到错误输出方法
                servletRequest.getRequestDispatcher("/contentTypeFailed").forward(servletRequest, servletResponse);
            }
            else{
                filterChain.doFilter(servletRequest, servletResponse);
            }
        }
        else{
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
