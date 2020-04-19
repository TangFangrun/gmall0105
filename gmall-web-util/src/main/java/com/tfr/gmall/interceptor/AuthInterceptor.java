package com.tfr.gmall.interceptor;

import com.alibaba.fastjson.JSON;
import com.tfr.gmall.annotations.LoginRequire;
import com.tfr.gmall.util.CookieUtil;
import com.tfr.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截代码

        //判断被拦截的请求访问的方法的注解（是否被拦截）
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequire methodAnnotation = hm.getMethodAnnotation(LoginRequire.class);

        if (methodAnnotation == null) {
            return true;
        }

        String token = "";

        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);

        String newToken = request.getParameter("token");

        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }
        //是否必须登录
        boolean loginSuccess = methodAnnotation.loginSuccess();//该请求是否必须登录成功
        //调用认证中心进行验证
        String success = "fail";
//        if (StringUtils.isNotBlank(token)) {
//            success = HttpclientUtil.doGet("http://localhost:8085/verify?token=" + token);
//        }
        Map<String, String> successMap = new HashMap<>();
        if (StringUtils.isNotBlank(token)) {
            String ip = request.getHeader("x-forwarded-for");//通过nginx转发的客户端ip
            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();//从request中获取客户端ip
                if (StringUtils.isBlank(ip)) {
                    ip = "127.0.0.1";
                }
            }
            String successJson  = HttpclientUtil.doGet("http://localhost:8085/verify?token=" + token+"&currentIp="+ip);
            successMap = JSON.parseObject(successJson, Map.class);
            success = successMap.get("status");
        }
        if (loginSuccess) {
            //必须登录成功才能使用
            if (!success.equals("success")) {
                //重定向passport登录
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://localhost:8085/index?ReturnUrl=" + requestURL);
                return false;
            }
            //验证通过，覆盖cookie中的token
            request.setAttribute("memberId", successMap.get("memberId"));
            request.setAttribute("nickname", successMap.get("nickname"));
            if (StringUtils.isNotBlank(token)) {

                CookieUtil.setCookie(request, response, "oldToken", token, 60 * 30 * 2, true);
            }

//            if (StringUtils.isNotBlank(token)) {
//                //踢回认证中心
//            } else {
//                //验证
//            }
        } else {
            //没有登录也能使用，但是必须验证
            if (!success.equals("success")) {
                //需要将token带入的用户信息写入
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));
                if (StringUtils.isNotBlank(token)){
                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
                }
            }
            //验证
        }

        System.out.println("进入拦截器的方法");
        return true;
    }
}