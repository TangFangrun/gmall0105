package com.tfr.gmall.user.controller;

import com.tfr.gmall.bean.UmsMember;
import com.tfr.gmall.bean.UmsMemberReceiveAddress;
import com.tfr.gmall.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {
    @Autowired()
    UserService userService;

    @RequestMapping("index")
    @ResponseBody
    public String index() {
        return "hello world";
    }

    @RequestMapping("getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser() {
        List<UmsMember> userMembers = userService.getAllUser();
        return userMembers;
    }

    @RequestMapping("getReceiveAddressByMemberId")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getReceiveAddressByMemberId(memberId);
        return umsMemberReceiveAddresses;
    }

}
