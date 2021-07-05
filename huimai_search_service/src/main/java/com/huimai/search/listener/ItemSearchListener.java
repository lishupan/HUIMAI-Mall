package com.huimai.search.listener;

import com.alibaba.fastjson.JSON;
import com.huimai.pojo.TbItem;
import com.huimai.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        if(message instanceof TextMessage){
            TextMessage textMessage= (TextMessage) message;
            try {
                String jsonString = textMessage.getText();
                List<TbItem> itemList = JSON.parseArray(jsonString, TbItem.class);
                //调用搜索服务，导入数据方法
                itemSearchService.importSolr(itemList);
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }
    }
}
