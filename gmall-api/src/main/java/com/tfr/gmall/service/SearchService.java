package com.tfr.gmall.service;

import com.tfr.gmall.bean.PmsSearchParam;
import com.tfr.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
