package com.huimai.pay.service;

import com.alipay.api.AlipayApiException;

import java.util.Map;

public interface AliPayService {

    //预下单请求方法
    //参数1：电商平台生成的订单编号
    //参数2：预下单金额 单位分
    public Map createNative(String out_trade_no,String total_fee);

    //查询指定订单编号，交易状态
    public Map queryPayStatus(String out_trade_no)throws AlipayApiException;
}
