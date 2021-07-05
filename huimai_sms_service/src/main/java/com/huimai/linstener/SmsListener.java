package com.huimai.linstener;

import com.alibaba.fastjson.JSON;
import com.huimai.SmsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;
@Component
public class SmsListener implements MessageListener {

    @Autowired
    private SmsTemplate smsTemplate;
    public void onMessage(Message message) {
        if(message instanceof MapMessage){
            MapMessage mapMessage= (MapMessage) message;
            //接收发送到手机号码
            try {
                String mobile = mapMessage.getString("mobile");
                //接收短信验证码值
                String smscode = mapMessage.getString("smscode");

                //调用短信发送模板工具类，发送短信
                String sendMsg = smsTemplate.smsSend(mobile, smscode);

                //转换响应内容
                Map smsMap = JSON.parseObject(sendMsg, Map.class);
               String code= (String) smsMap.get("return_code");
               if(code!=null&&code.equals("00000")){
                   System.out.println("短信发送成功");
               }else {
                   System.out.println("短信发送失败:"+code+"");
               }

            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
