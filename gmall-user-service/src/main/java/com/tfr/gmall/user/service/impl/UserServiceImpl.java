package com.tfr.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.tfr.gmall.bean.UmsMember;
import com.tfr.gmall.bean.UmsMemberReceiveAddress;
import com.tfr.gmall.service.UserService;
import com.tfr.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.tfr.gmall.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;

import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;


    public List<UmsMember> getAllUser() {

        List<UmsMember> umsMemberList = userMapper.selectAll();
        return umsMemberList;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        Example e = new Example(UmsMemberReceiveAddress.class);
        e.createCriteria().andEqualTo("memberId", memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(e);
        return umsMemberReceiveAddresses;
    }
}
