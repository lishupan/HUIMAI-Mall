package com.huimai;

import com.huimai.utils.HttpUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class SmsTemplate {

    //注入appcode
    @Value("${appcode}")
    private String appcode;

    //注入模板编号
    @Value("${tpl}")
    private String tpl_id;

    //1、指定发送短信需要调用网关地址
    String host="http://dingxintz.market.alicloudapi.com";
    //2、调用的接口的路径
    String path="/dx/notifySms";
    //http://dingxintz.market.alicloudapi.com/dx/notifySms
    //3、http协议请求方法 get\post
    String method="POST";

    //编写短信发送方法
    //参数1：要接收手机号码 参数2：短信验证码
    public String smsSend(String mobile,String smscode){
        //1、创建map，封装全部的请求头数据
        Map heads=new HashMap();
        heads.put("Authorization","APPCODE "+appcode);
        //2、创建map，封装请求参数
        Map querys = new HashMap();
        querys.put("mobile",mobile);
        querys.put("param","code:"+smscode);
        querys.put("tpl_id",tpl_id);

        //3、创建请求体
        Map bodys=new HashMap();

        //判断请求方法是不是post
        if(method.equals("POST")){
            try {
                HttpResponse response = HttpUtils.doPost(host, path, method, heads, querys, bodys);
                //打印显示基本响应内容
                System.out.println("基本响应信息:"+response.toString());
                //获取响应对象
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity,"UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            System.out.println("请求方法不支持!");
        }

        return  null;
    }
}
