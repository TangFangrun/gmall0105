package com.tfr.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tfr.gmall.bean.PmsProductImage;
import com.tfr.gmall.bean.PmsProductInfo;
import com.tfr.gmall.bean.PmsProductSaleAttr;
import com.tfr.gmall.manage.util.PmsUploadUtil;
import com.tfr.gmall.service.SpuService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin
@RestController
public class SpuController {
    @Reference
    SpuService spuService;
    @RequestMapping("spuList")
    public List<PmsProductInfo> spuList(String catalog3Id) {
        List<PmsProductInfo>  pmsProductInfos=spuService.spuList(catalog3Id);
        return pmsProductInfos;
    }

    @RequestMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {
        spuService.saveSpuInfo(pmsProductInfo);
        return "success";
    }
    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
    String imgUrl= PmsUploadUtil.uploadImage(multipartFile);
     //   System.out.println(imgUrl);
        return imgUrl;
    }
    @RequestMapping("spuImageList")
    public List<PmsProductImage> spuImageList(String spuId){

        List<PmsProductImage> pmsProductImages = spuService.spuImageList(spuId);
        return pmsProductImages;
    }


    @RequestMapping("spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){

        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrList(spuId);
        return pmsProductSaleAttrs;
    }

}
