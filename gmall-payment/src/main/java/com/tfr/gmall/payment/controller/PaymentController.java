package com.tfr.gmall.payment.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.tfr.gmall.annotations.LoginRequire;
import com.tfr.gmall.payment.config.AlipayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;

    @RequestMapping("index")
    @LoginRequire
    public String index(String outTradeNo, BigDecimal totalAmount, ModelMap map, HttpServletRequest request) {

        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickName");

        map.put("nickName", nickName);
        map.put("outTradeNo", outTradeNo);
        map.put("totalAmount", totalAmount);

        return "index";
    }

    @RequestMapping("wx/submit")
    @LoginRequire
    public String wx(String outTradeNo, BigDecimal totalAmount, ModelMap map, HttpServletRequest request) {
        return "";
    }

    @RequestMapping("alipay/submit")
    @LoginRequire
    @ResponseBody
    public String alipay(String outTradeNo, BigDecimal totalAmount, ModelMap modelMap, HttpServletRequest request) {

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request

        // 回调函数
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", "outTradeNo");
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", 0.01);
        map.put("subject", "尚硅谷感光徕卡Pro300瞎命名系列手机");

        String param = JSON.toJSONString(map);

        alipayRequest.setBizContent(param);
        //获得一个支付宝请求的客户端（不是一个连接，是一个封装好的http的表单请求）
        String form = null;
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return form;
    }

    @RequestMapping("alipay/callback/return")
    @LoginRequire
    public String alipayCallBackReturn(String outTradeNo, BigDecimal totalAmount, ModelMap modelMap, HttpServletRequest request) {
        return "finish";
    }
}
