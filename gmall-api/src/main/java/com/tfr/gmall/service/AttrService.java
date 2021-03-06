package com.tfr.gmall.service;

import com.tfr.gmall.bean.PmsBaseAttrInfo;
import com.tfr.gmall.bean.PmsBaseAttrValue;
import com.tfr.gmall.bean.PmsBaseSaleAttr;

import java.util.List;
import java.util.Set;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);


    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);



    List<PmsBaseSaleAttr> baseSaleAttrList();

    List<PmsBaseAttrInfo> getAttrValueListByValuedId(Set<String> valueIdSet);
}
