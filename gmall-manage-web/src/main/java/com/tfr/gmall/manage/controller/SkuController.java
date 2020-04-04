package com.tfr.gmall.manage.controller;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.tfr.gmall.bean.PmsSkuInfo;
import com.tfr.gmall.service.SkuService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SkuController {

    @Reference
    SkuService skuService;


    @RequestMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){
        //将spoId封装给productId
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        // 处理默认图片
     /*   String skuDefaultImg = pmsSkuInfo.getSkuDefaultImg();
        if(StringUtils.isBlank(skuDefaultImg)){
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }
*/

        skuService.saveSkuInfo(pmsSkuInfo);

        return "success";
    }
}
