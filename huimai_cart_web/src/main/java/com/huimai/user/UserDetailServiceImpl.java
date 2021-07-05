package com.huimai.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //创建一个权限集合
        List<GrantedAuthority> list=new ArrayList<GrantedAuthority>();
        //添加权限
        list.add(new SimpleGrantedAuthority("ROLE_STU"));
        list.add(new SimpleGrantedAuthority("ROLE_TEACHER"));

        return new User(username,"",list);
    }
}
