package com.huimai.cart;

import com.huimai.group.Cart;

import java.util.List;

public interface CartService {

    //添加商品到购物车
    //参数1：原购物车集合
    //参数2：要添加到购物车商品编号(SKU编号)
    //参数3：添加到购物车商品数量
    //返回值，最新购物车集合数据
    public List<Cart> addGoodsToCartList(List<Cart> srcCartList,Long itemId,Integer num);

    //根据指定账号，从redis读取购物车数据
    public List<Cart> findCartListFromRedis(String userId);

    //保存指定账号购物车数据到redis
    public void saveCartListToRedis(String userId,List<Cart> cartList);

    //合并购物车
    public List<Cart> mergeCartList(List<Cart> cookie_CartList,List<Cart> redis_CartList);
}
