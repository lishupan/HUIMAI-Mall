package com.huimai.page.service;

public interface ItemPageService {

    //创建一个，根据商品id，生成指定静态页面
    public void genItemHtml(Long goodsId);

    //根据商品id，删除指定静态页面
    public void deleteHtml(Long goodsId);
}
