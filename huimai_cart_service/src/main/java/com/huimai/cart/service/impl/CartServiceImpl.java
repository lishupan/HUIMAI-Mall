package com.huimai.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huimai.group.Cart;
import com.huimai.mapper.TbItemMapper;
import com.huimai.pojo.TbItem;
import com.huimai.pojo.TbOrderItem;
import com.huimai.cart.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> srcCartList, Long itemId, Integer num) {

        //判断原购物车集合是否为空
        if(srcCartList==null){
            //初始化一个空白的集合
            srcCartList=new ArrayList<>();
        }

        //1、根据sku编号，从数据库读取sku对象
        TbItem item = itemMapper.selectByPrimaryKey(itemId);

        //1.1 判断sku对象是否为空
        if(item==null){
            throw new RuntimeException("商品不存在,添加到购物车失败");
        }

        //1.2 判断商品状态是否 正常 1
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态不正确，添加购物车失败");
        }

        //2、获取商品所属商家名称
        String sellerName = item.getSeller();
        //获取商家编号
        String sellerId = item.getSellerId();

        //3、根据商家编号，查询该商家的购物车对象
        Cart cart = this.searchCartBySellerId(srcCartList, sellerId);

        //4、情况1：添加到购物车商家购物车对象不存在
        if(cart==null){
            cart=new Cart();
            //设置商家名称
            cart.setSellerName(sellerName);
            //设置商家编号
            cart.setSellerId(sellerId);
            //创建一个购物明细集合
            List<TbOrderItem> orderItemList=new ArrayList<>();
            //创建购物明细
            TbOrderItem orderItem = this.createOrderItem(item, num);
            //添加购物明细到购物明细的集合
            orderItemList.add(orderItem);
            //把购物明细集合关联到购物车对象
            cart.setOrderItemList(orderItemList);
            //添加购物车到原购物车集合
            srcCartList.add(cart);

        }else {
            //情况2：商家购物车对象已经存在
            TbOrderItem orderItem = this.searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if(orderItem==null){
                //情况2.1 购物明细不存在要添加的商品
                //创建购物明细
                orderItem=  this.createOrderItem(item,num);
                //把购物明细添加到购物集合的购物对象
                cart.getOrderItemList().add(orderItem);
            }else {
                //情况2.2 购物明细已经存在
                //更新当前购物明细的购买数量
                orderItem.setNum(orderItem.getNum()+num);
                //更新合计金额
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*item.getPrice().doubleValue()));

                //更新完购买数量后，产生几个特殊情况

                //情况1：当前商品购买数量为0
                if(orderItem.getNum().intValue()==0){
                    //把当前购物明细从购物明细集合移除
                    cart.getOrderItemList().remove(orderItem);
                }

                //情况2：当前购物车对象的购物明细集合为0，要从购物车集合移除当前购物车对象
                if(cart.getOrderItemList().size()==0){
                    srcCartList.remove(cart);
                }
            }

        }


        return srcCartList;
    }

    //判断指定商家编号的，购物车对象是否存在，存在返回该商家的购物车对象
    //参数1：购物车集合  参数2：商家编号
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        for (Cart cart : cartList) {
            //判断购物车的商家编号，和传递参数的商家编号，是否相同
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }

        return null;
    }

    //创建购物明细对象
    private TbOrderItem createOrderItem(TbItem item,Integer num){

        //判断购买数量是否小于1
        if(num<1){
            throw  new RuntimeException("购买数量小于1");
        }
        //创建一个空的购物明细对象
        TbOrderItem orderItem = new TbOrderItem();
        //设置spu 编号
        orderItem.setGoodsId(item.getGoodsId());
        //设置sku编号
        orderItem.setItemId(item.getId());
        //设置购买数量
        orderItem.setNum(num);
        //设置商品配图
        orderItem.setPicPath(item.getImage());
        //商品单价
        orderItem.setPrice(item.getPrice());
        //商品商家编号
        orderItem.setSellerId(item.getSellerId());
        //商品标题
        orderItem.setTitle(item.getTitle());
        //设置总金额=购买数量*单价
        orderItem.setTotalFee(new BigDecimal(num*item.getPrice().doubleValue()));

        return orderItem;

    }

    //搜索购物车的购物明细集合，判断指定商品购买明细是否存在
    //参数1：购物明细集合  参数2：sku商品编号
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){

        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }

        return null;
    }

    @Override
    public List<Cart> findCartListFromRedis(String userId) {
      List<Cart> cartList= (List<Cart>) redisTemplate.boundHashOps("cartList").get(userId);
      //判断从redis读取到购物车数据如果等于null
        if(cartList==null){
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String userId, List<Cart> cartList) {

        redisTemplate.boundHashOps("cartList").put(userId,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cookie_CartList, List<Cart> redis_CartList) {
        //遍历cookie购物车集合
        for (Cart cart : cookie_CartList) {
            //继续购物车明细
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                //调用添加到购物车方法，逐个把cookie购物车数据，添加的redis购物车
             redis_CartList=   this.addGoodsToCartList(redis_CartList,orderItem.getItemId(),orderItem.getNum());
            }
        }

        return redis_CartList;
    }
}
