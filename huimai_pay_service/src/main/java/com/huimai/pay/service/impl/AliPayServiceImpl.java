package com.huimai.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.huimai.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
@Service
public class AliPayServiceImpl implements AliPayService {

    //注入向支付宝平台发送请求客户端
    @Autowired
    private DefaultAlipayClient defaultAlipayClient;


    @Override
    public Map createNative(String out_trade_no, String total_fee) {

        Map result=new HashMap();

        //把接收到金额，是字符串转换为整数 单位是分
        long totalFee_Long = Long.parseLong(total_fee);

        //创建高精度分
        BigDecimal totalFee_bigDecimal_Fen = new BigDecimal(totalFee_Long);
        //准备一个高精度除数 100
        BigDecimal CS = new BigDecimal(100);
        //进行高精度计算，把分转换为元
        BigDecimal totalFee_bigDecimal_Yuan = totalFee_bigDecimal_Fen.divide(CS);

        System.out.println("预下单金额:"+totalFee_bigDecimal_Yuan.doubleValue());
        //1、创建一个预下单请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

        //2、设置请求参数
        request.setBizContent("{"   +
                "\"out_trade_no\":\""+out_trade_no+"\"," + //商户订单号
                "\"total_amount\":\""+totalFee_bigDecimal_Yuan.doubleValue()+"\","   + //预下单金额（要求单位是元）
                "\"subject\":\"东易买电商商品\","   +//商品名称
                "\"store_id\":\"WZ_001\","   + //店铺编号
                "\"timeout_express\":\"90m\"}" );//有效时间

        //3、调用客户端，发出预下单请求，
        while (true) {
            try {
                AlipayTradePrecreateResponse response = defaultAlipayClient.execute(request);

                //判断响应是否成功
               if(response.isSuccess()){
                   //4、获取响应状态码
                   String code = response.getCode();
                   //获取全部的响应数据
                   String body = response.getBody();

                   System.out.println("预下单响应结果，状态码:" + code + " 返回数据:" + body);

                   //5、根据状态码判断成功
                   if (code.equals("10000")) {
                       //获取支付宝返回的订单二维码地址
                       String qrCode = response.getQrCode();
                       //封装返回结果到map
                       result.put("qrcode", qrCode);
                       result.put("out_trade_no", out_trade_no);
                       result.put("total_fee", total_fee);
                   }
                   break;
               }


                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    @Override
    public Map queryPayStatus(String out_trade_no) throws AlipayApiException {
        Map result=new HashMap();
        //1、创建查询交易状态请求对象
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        //2、设置查询请求参数
        request.setBizContent("{" +
                "\"out_trade_no\":\""+out_trade_no+"\"," +
                "\"trade_no\":\"\"}");

        //3、发出查询请求

            AlipayTradeQueryResponse response = defaultAlipayClient.execute(request);

            String code = response.getCode();
            String body = response.getBody();
            System.out.println("交易状态查询,状态码:"+code+" 返回值:"+body);

            if(code.equals("10000")){
                //读取交易状态
                String tradeStatus = response.getTradeStatus();
                //封装交易状态
                result.put("tradestatus",tradeStatus);
                //封装订单编号
                result.put("out_trade_no",out_trade_no);
                //获取支付宝返回的交易流水号
                result.put("trade_no",response.getTradeNo());
            }else {
                System.out.println("请求失败,状态码:"+code);
            }

        return result;
    }
}
