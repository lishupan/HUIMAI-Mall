package com.huimai.shop.user;

import com.huimai.pojo.TbSeller;
import com.huimai.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserLoginImpl implements UserDetailsService {

    //定义商家服务
    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //1、权限控制
        List<GrantedAuthority> list=new ArrayList<GrantedAuthority>();

        //向权限集合添加权限
        list.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        list.add(new SimpleGrantedAuthority("ROLE_GUEST"));
        System.out.println();
        //2、根据商家编号，去调用商家服务，查询商家信息
        TbSeller seller = sellerService.findOne(username);

        //判断商家信息是否为空
        if(seller!=null){
            //判断商家状态是否为审核通过
            if(seller.getStatus().equals("1")){
                //创建认证对象，返回springSecurity进行认证处理
                return new User(username,seller.getPassword(),list);
            }else {
                System.out.println("商家:"+username+" 状态不对:"+seller.getStatus());
                return null;
            }
        }else {
            return null;
        }

    }
}
