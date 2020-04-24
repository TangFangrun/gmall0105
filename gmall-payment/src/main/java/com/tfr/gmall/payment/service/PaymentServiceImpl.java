package com.tfr.gmall.payment.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.tfr.gmall.bean.PaymentInfo;
import com.tfr.gmall.payment.mapper.PaymentInfoMapper;
import com.tfr.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

@Service
public class PaymentServiceImpl  implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        String orderSn = paymentInfo.getOrderSn();
        Example e=new Example(PaymentInfo.class);
        e.createCriteria().andEqualTo("orderSn",orderSn);
        paymentInfoMapper.updateByExample(paymentInfo,e);

    }
}
