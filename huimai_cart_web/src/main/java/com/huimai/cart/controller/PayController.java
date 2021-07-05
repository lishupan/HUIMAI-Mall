package com.huimai.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alipay.api.AlipayApiException;
import com.huimai.entity.Result;
import com.huimai.order.service.OrderService;
import com.huimai.pay.service.AliPayService;
import com.huimai.pojo.TbPayLog;
import com.huimai.utils.IdWorker;
import org.opensaml.xml.signature.P;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("pay")
public class PayController {

    @Reference(timeout = 20000)
    private AliPayService aliPayService;

    @Reference
    private OrderService orderService;

    @Autowired
    private IdWorker idWorker;


    //预下单方法
    @RequestMapping("createNative")
    public Map createNative(){
        Map map=new HashMap();
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        //调用订单服务，读取当前用户支付日志信息
        TbPayLog payLog = orderService.findPayLogByUserId(userId);

        //判断支付日志成功读取
        if(payLog!=null){
            map= aliPayService.createNative(payLog.getOutTradeNo()+"",payLog.getTotalFee()+"");
        }else {
            map.put("msg","支付日志读取失败");
        }

       return map;
    }

    //查询指定订单编号的交易状态
    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result=null;

        //定义一个变量记录查询次数
        int count=0;
        //循环查询交易状态
        while (true){
            Map map=null;
            try {
                 map = aliPayService.queryPayStatus(out_trade_no);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }

            //判断返回值map是否为空，查询返回空指针,系统异常，跳出循环
            if(map==null){
                result=new Result(false,"调用查询服务出现故障");
                break;
            }

            //正常调用查询服务，判断交易状态
            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_SUCCESS")){
                //创建成功返回对象
                result=new Result(true,"支付成功");
                //调用订单服务，更新状态
                orderService.updateOrderStatus(out_trade_no,(String) map.get("trade_no"));
                break;
            }

            //交易关闭，结束查询
            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_CLOSED")){
                //创建成功返回对象
                result=new Result(false,"未付款交易超时关闭，或支付完成后全额退款");
                break;
            }

            //交易结束，结束查询
            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_FINISHED")){
                //创建成功返回对象
                result=new Result(false,"交易结束，不可退款");
                break;
            }

            count++;

            //判断查询次数超过设定值，就跳出查询，返回 查询超时结果
            if(count>100){
                result=new Result(false,"查询超时");
                break;
            }

            //让线程等待3秒
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return  result;
    }
}
