package com.huimai.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.huimai.cart.CartService;
import com.huimai.entity.Result;
import com.huimai.group.Cart;
import com.huimai.utils.CookieUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {

    @Reference
    private CartService cartService;

    //读取购物全部数据
    @RequestMapping("findAll")
    public List<Cart> findAll(HttpServletRequest request,HttpServletResponse response){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户:"+userId);
        List<Cart> cartList_Cookie;
        //尝试从cookie读取购物车数据
        String cookieValue = CookieUtil.getCookieValue(request, "cartList", "UTF-8");

        //判断cookie读取到值如果为空，空白
        if (cookieValue == null || cookieValue.equals("")) {
            //设置一个初始化值
            cookieValue = "[]";
        }
        //把读取到cookie存储到购物车json字符串转换为集合
        cartList_Cookie = JSON.parseArray(cookieValue, Cart.class);
        //判断用户名是否等于anonymousUser
        if(userId.equals("anonymousUser")) {
         return cartList_Cookie;
        }else {
            //用户处于登录状态，从redis读取购物车数据
          List<Cart> cartList_Redis= cartService.findCartListFromRedis(userId);

          //当用户处于登录状态，判断cookie购物车数据是否为空
            if(cartList_Cookie!=null&&cartList_Cookie.size()>0){
                //合并cookie购物车数据到redis
                cartList_Redis=    cartService.mergeCartList(cartList_Cookie,cartList_Redis);
                //更新最新购物车数据到redis
                cartService.saveCartListToRedis(userId,cartList_Redis);
                //清空cookie购物车数据
                CookieUtil.deleteCookie(request,response,"cartList");
            }
          return cartList_Redis;
        }

    }

    //添加商品到购物车
    @RequestMapping("addGoodsToCart")
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodsToCart(HttpServletRequest request, HttpServletResponse response,Long itemId,Integer num){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户:"+userId);

        try {
            //1、获取原购物集合
            List<Cart> srcCartList = this.findAll(request,response);
            //2、调用购物车服务，添加到购物车方法
            List<Cart> cartListNew = cartService.addGoodsToCartList(srcCartList, itemId, num);
            //3、存储新购物车集合数据到cookie
            String jsonString = JSON.toJSONString(cartListNew);

            //判断当前用户名
            if(userId.equals("anonymousUser")) {
                CookieUtil.setCookie(request, response, "cartList", jsonString, 24 * 3600, "UTF-8");

            }else {
                //写入购物车数据到redis
                cartService.saveCartListToRedis(userId,cartListNew);
            }
            return new Result(true,"添加购物车成功");

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加购物车失败:"+e.getMessage());
        }
    }
}
