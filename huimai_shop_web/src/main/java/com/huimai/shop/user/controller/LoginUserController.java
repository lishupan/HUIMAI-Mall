package com.huimai.shop.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("user")
public class LoginUserController {

    //获取当前登录用户名
    @RequestMapping("showLoginName")
    public Map showLoginName(){
        Map map=new HashMap();

        //使用springSecurity上下文来获取当前登录的用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        //封装到map
        map.put("loginName",name);

        return map;
    }
}
