package com.gdut.fundraising.filter;

import org.springframework.core.Ordered;


import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(filterName = "TokenFilter",urlPatterns = {"/user/launch","/user/uploadPhoto","/user/contribution","/manage/*"})
public class TokenFilter implements Filter, Ordered {

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String token =  ((HttpServletRequest)servletRequest).getHeader("AUTHORIZATION");
        if(token == null || !token.matches("Bearer.*")){
            //跳转到错误输出方法
            servletRequest.getRequestDispatcher("/tokenFailed").forward(servletRequest, servletResponse);
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
        return 1;
    }
}
