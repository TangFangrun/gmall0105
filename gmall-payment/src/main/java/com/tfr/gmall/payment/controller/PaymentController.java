package com.tfr.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.tfr.gmall.annotations.LoginRequire;
import com.tfr.gmall.bean.OmsOrder;
import com.tfr.gmall.bean.PaymentInfo;
import com.tfr.gmall.payment.config.AlipayConfig;
import com.tfr.gmall.service.OrderService;
import com.tfr.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;

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
            form = alipayClient.pageExecute(alipayRequest).getBody();//调用sdk生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("商品一件.......");
        paymentInfo.setTotalAmount(totalAmount);
        //向消息中间件发送一个检查支付状态的延迟消息队列
        paymentService.sendDelayPaymentResultCheckQueue(outTradeNo, 5);
        //生成并且保存用户的支护信息
        paymentService.savePaymentInfo(paymentInfo);
        return form;
    }

    @RequestMapping("alipay/callback/return")
    @LoginRequire
    public String alipayCallBackReturn(String outTradeNo, BigDecimal totalAmount, ModelMap modelMap, HttpServletRequest request) {

        //回调请求中获取支付宝参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();
        if (StringUtils.isNotBlank(sign)) {
            // 验签成功
            // 更新用户的支付状态

            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);// 支付宝的交易凭证号
            paymentInfo.setCallbackContent(call_back_content);//回调请求字符串
            paymentInfo.setCallbackTime(new Date());
            //更新用户的支付状态
            paymentService.updatePayment(paymentInfo);

        }

        //更新用户的支付状态
        return "finish";
    }
}
