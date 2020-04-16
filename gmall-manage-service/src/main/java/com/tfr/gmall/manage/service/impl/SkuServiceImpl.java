package com.tfr.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.tfr.gmall.bean.PmsSkuAttrValue;
import com.tfr.gmall.bean.PmsSkuImage;
import com.tfr.gmall.bean.PmsSkuInfo;
import com.tfr.gmall.bean.PmsSkuSaleAttrValue;
import com.tfr.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.tfr.gmall.manage.mapper.PmsSkuImageMapper;
import com.tfr.gmall.manage.mapper.PmsSkuInfoMapper;
import com.tfr.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.tfr.gmall.service.SkuService;
import com.tfr.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    RedisUtil redisUtil;


    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        // 插入skuInfo
        int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();

        // 插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        // 插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        // 插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }


    }

    public PmsSkuInfo getSkuByIdFromDb(String skuId) {
        //sku的商品对象
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        //sku的图片
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(pmsSkuImages);
        return skuInfo;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        Jedis jedis = null;
        //链接缓存
        try {
            jedis = redisUtil.getJedis();
            //查询缓存
            String skuKey = "sku:" + skuId + ":info";
            String skuJson = jedis.get(skuKey);
            if (StringUtils.isNotBlank(skuJson)) {
                pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
            } else {
                //设置分布式锁
                String token = UUID.randomUUID().toString();
                String OK = jedis.set("sku:" + skuId + ":lock", token, "nx", "px", 10 * 1000);
                if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {
                    //查询数据库
                    //设置成功，有权在10s过期时间访问数据库
                    pmsSkuInfo = getSkuByIdFromDb(skuId);
                    if (pmsSkuInfo != null) {
                        //结果存入redis
                        jedis.set("sku:" + skuId + ":info", JSON.toJSONString(pmsSkuInfo));

                    } else {
                        //数据库不存在sku  ，解决缓存穿透（给redis设置null或者空）
                        jedis.setex("sku:" + skuId + ":info", 60 * 3, JSON.toJSONString(" "));
                    }
                    //释放锁
                    String lockToken = jedis.get("sku:" + skuId + ":info");
                    if(StringUtils.isNotBlank(lockToken)&&lockToken.equals(token)){
                        //用token确认删除的是自己的锁
                        jedis.del("sku:" + skuId + ":lock");
                    }

                } else {
                    //设置失败，自旋（该线程在睡眠几秒后，重新尝试访问该方法）
                    Thread.sleep(3000);
                    return getSkuById(skuId);
                }

            }

            return pmsSkuInfo;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return null;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySqu(String productId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySqu(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSku(String catalog3Id) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();
            PmsSkuAttrValue pmsSkuAttrValue=new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(select);
        }
        return pmsSkuInfos;
    }
}
