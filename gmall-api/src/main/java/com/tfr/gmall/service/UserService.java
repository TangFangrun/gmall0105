package com.tfr.gmall.service;

import com.tfr.gmall.bean.UmsMember;
import com.tfr.gmall.bean.UmsMember;
import com.tfr.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);
}
