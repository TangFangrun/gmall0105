package com.tfr.gmall.gmallport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.tfr.gmall.bean.UmsMember;
import com.tfr.gmall.service.UserService;
import com.tfr.gmall.util.HttpclientUtil;
import com.tfr.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @RequestMapping("vlogin")
    public String vlogin(String code, HttpServletRequest request) {

        //授权码获取access_token

        String s3 = "https://api.weibo.com/oauth2/access_token";//?client_id=3313740909&client_secret=c8f76c3081fe3554f394d9b3305e7529&grant_type=authorization_code&redirect_uri=http://127.0.0.1:8085/vlogin&code=CODE";
        Map<String, String> map = new HashMap<>();

        map.put("client_id", "3313740909");
        map.put("client_secret", "c8f76c3081fe3554f394d9b3305e7529");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://127.0.0.1:8085/vlogin");
        map.put("code", code);

        String access_token = HttpclientUtil.doPost(s3, map);
        Map<String, Object> access_map = JSON.parseObject(access_token, Map.class);
        //access_token换区用户信息
        String uid = (String) access_map.get("uid");
        String accessToken = (String) access_map.get("access_token");

        String show_user_url = "https://api.weibo.com/2/users/show.json?access_token=" + accessToken + "&uid=" + uid;
        String user_json = HttpclientUtil.doGet(show_user_url);
        Map<String, Object> user_map = JSON.parseObject(user_json, Map.class);

        //将用户信息保存到数据库，用户类型设置为微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType("2");
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(accessToken);
        umsMember.setSourceUid((String) user_map.get("idstr"));
        umsMember.setCity((String) user_map.get("location"));
        umsMember.setNickname((String) user_map.get("screen_name"));

        String gender = (String) user_map.get("gender");
        String seax = "0";
        if (gender.equals("m")) {
            seax = "1";
        }
        umsMember.setGender(seax);

        //检查数据库用户是否存在
        UmsMember umsCheck = new UmsMember();
        umsCheck.setSourceUid(umsMember.getSourceUid());
        UmsMember umsMemberCheck = userService.checkOauthUser(umsCheck);
        if (umsMemberCheck == null) {
            userService.addOauthUser(umsMember);
        } else {
            umsMember = umsMemberCheck;
            //      userService.getOauthUser(umsMemberCheck);
        }

        //生成jwt的token，并且重定向到首页，携带该token
        String token = "";
        jwtToken(umsMember,request,token);
    /*    String token = null;
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("memberId", memberId);
        userMap.put("nickname", nickname);


        String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
        if (StringUtils.isBlank(ip)) {
            ip = request.getRemoteAddr();// 从request中获取ip
            if (StringUtils.isBlank(ip)) {
                ip = "127.0.0.1";
            }
        }

        // 按照设计的算法对参数进行加密后，生成token
        token = JwtUtil.encode("2019gmall0105", userMap, ip);

        // 将token存入redis一份
        userService.addUserToken(token, memberId);*/
        return "redirect:http://localhost:8083/index";
    }

    /**
     * @param token
     * @param currentIp 通过拦截器之前的ip（原始ip）
     * @return
     */
    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token, String currentIp, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();


        Map<String, Object> decode = JwtUtil.decode(token, "2020gmall0105", currentIp);

        if (decode != null) {
            map.put("status", "success");
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickname", (String) decode.get("nickname"));
        } else {
            map.put("status", "fail");
        }
        return JSON.toJSONString(map);
    }


    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        String token = "";
        //调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);
        if (umsMemberLogin != null) {
            //登录成功


            //用jwt制作token
            jwtToken(umsMember,request,token);
//            String memberId = umsMemberLogin.getId();
//            String nickname = umsMemberLogin.getNickname();
//            Map<String, Object> userMap = new HashMap<>();
//            userMap.put("memberId", memberId);
//            userMap.put("nickname", nickname);
//
//            String ip = request.getHeader("x-forwarded-for");//通过nginx转发的客户端ip
//            if (StringUtils.isBlank(ip)) {
//                ip = request.getRemoteAddr();//从request中获取客户端ip
//                if (StringUtils.isBlank(ip)) {
//                    ip = "127.0.0.1";
//                }
//            }
//            //需要按照设计的算法进行加密，生成token
//            token = JwtUtil.encode("2020gmall0105", userMap, ip);
//
//            //将token存入redis
//            userService.addUserToken(token, memberId);

        } else {
            //登录失败
            token = "fail";

        }
        return token;
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap map) {
        map.put("ReturnUrl", ReturnUrl);
        return "index";
    }
    public void jwtToken(UmsMember umsMember,HttpServletRequest request,String token){
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();
        Map<String,Object> userMap = new HashMap<>();
        userMap.put("memberId",memberId);
        userMap.put("nickname",nickname);


        String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();// 从request中获取ip
            if(StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }
        }

        // 按照设计的算法对参数进行加密后，生成token
        token = JwtUtil.encode("2019gmall0105", userMap, ip);

        // 将token存入redis一份
        userService.addUserToken(token,memberId);
    }
}

