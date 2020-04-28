package com.tfr.gmall.service;

import com.tfr.gmall.bean.OmsOrder;

import java.math.BigDecimal;

public interface OrderService {
    String genTradeCode(String memberId);

    String checkTradeCode(String memberId, String tradeCode);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeNo(String outTradeNo);

    void updateOrder(OmsOrder omsOrder);


    //   boolean checkPrice(String productSkuId, BigDecimal productPrice);
}
