package com.tfr.gmall.gmallport.controller;

import com.tfr.gmall.util.HttpclientUtil;

import java.util.HashMap;
import java.util.Map;

public class TestOauth2 {
    public static void main(String[] args) {
       String s1 = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=3313740909&response_type=code&redirect_uri=http://127.0.0.1:8085/vlogin");

        String s2="http://127.0.0.1:8085/vlogin?code=e552700d74e9038630f7f6f50cb7d25f";


//        http://127.0.0.1:8085/vlogin?code=7c436f382658367f3849e24ddee0df7a
        String s3="https://api.weibo.com/oauth2/access_token";//?client_id=3313740909&client_secret=c8f76c3081fe3554f394d9b3305e7529&grant_type=authorization_code&redirect_uri=http://127.0.0.1:8085/vlogin&code=CODE";
        Map<String,String> map=new HashMap<>();

        map.put("client_id","3313740909");
        map.put("client_secret","c8f76c3081fe3554f394d9b3305e7529");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://127.0.0.1:8085/vlogin");
        map.put("code","7c436f382658367f3849e24ddee0df7a");
        String access_token = HttpclientUtil.doPost(s3, map);
        System.out.println(access_token);
//
//        String s31="https://api.weibo.com/oauth2/access_token?client_id=3313740909&client_secret=c8f76c3081fe3554f394d9b3305e7529&grant_type=authorization_code&redirect_uri=http://127.0.0.1:8085/vlogin&code=e552700d74e9038630f7f6f50cb7d25f";
//        String s = HttpclientUtil.doGet(s31);
//
//        System.out.println(s+".0.0.0.0.0.0");
    }
}
