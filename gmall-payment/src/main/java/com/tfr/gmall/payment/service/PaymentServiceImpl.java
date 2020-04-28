package com.tfr.gmall.payment.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.tfr.gmall.bean.PaymentInfo;
import com.tfr.gmall.mq.ActiveMQUtil;
import com.tfr.gmall.payment.mapper.PaymentInfoMapper;
import com.tfr.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        //幂等性检查
        PaymentInfo paymentInfo1 = new PaymentInfo();
        paymentInfo1.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo paymentInfo2 = paymentInfoMapper.selectOne(paymentInfo1);
        if (StringUtils.isNotBlank(paymentInfo2.getPaymentStatus()) && paymentInfo2.getPaymentStatus().equals("已支付")) {
            return;
        } else {
            String orderSn = paymentInfo.getOrderSn();
            Example e = new Example(PaymentInfo.class);
            e.createCriteria().andEqualTo("orderSn", orderSn);
            Connection connection = null;
            Session session = null;
            try {
                connection = activeMQUtil.getConnectionFactory().createConnection();
                session = connection.createSession(true, Session.SESSION_TRANSACTED);
            } catch (JMSException ex) {
                ex.printStackTrace();
            }

            try {
                paymentInfoMapper.updateByExample(paymentInfo, e);
                //调用mq发送支付成功消息
                Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
                MessageProducer producer = session.createProducer(payment_success_queue);

                MapMessage mapMessage = new ActiveMQMapMessage();
                mapMessage.setString("out_trade_no", paymentInfo.getOrderSn());

                producer.send(mapMessage);

                session.commit();

            } catch (Exception e1) {
                try {
                    session.rollback();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            } finally {
                try {
                    session.close();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    @Override
    public Map<String, Object> checkAlipayPayment(String out_trade_no) {
        Map<String, Object> resultMap = new HashMap<>();

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("out_trade_no", out_trade_no);
        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("有可能交易已创建，调用成功");
            resultMap.put("out_trade_no", response.getOutTradeNo());
            resultMap.put("trade_no", response.getTradeNo());
            resultMap.put("trade_status", response.getTradeStatus());
            resultMap.put("call_back_content", response.getMsg());
        } else {
            System.out.println("有可能交易未创建，调用失败");

        }

        return resultMap;

    }

    @Override
    public void sendDelayPaymentResultCheckQueue(String out_trade_no, Integer count) {
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException ex) {
            ex.printStackTrace();
        }

        try {
            //调用mq发送支付成功消息
            Queue payment_success_queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);

            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no", out_trade_no);
            //为消息加入延迟时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000 * 30);
            producer.send(mapMessage);

            session.commit();

        } catch (Exception e1) {
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                session.close();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }

    }
}
