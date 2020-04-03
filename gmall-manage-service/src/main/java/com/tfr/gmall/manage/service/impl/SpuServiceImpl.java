package com.tfr.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.tfr.gmall.bean.PmsProductInfo;
import com.tfr.gmall.manage.mapper.PmsProductInfoMapper;
import com.tfr.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;
    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo=new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoMapper.select(pmsProductInfo);
        return pmsProductInfos;
    }
}
