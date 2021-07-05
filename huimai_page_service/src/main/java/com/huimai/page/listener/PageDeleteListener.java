package com.huimai.page.listener;

import com.huimai.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class PageDeleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        if(message instanceof ObjectMessage){
            ObjectMessage objectMessage= (ObjectMessage) message;
            try {
                Long[] ids= (Long[]) objectMessage.getObject();

                for (Long id : ids) {
                    itemPageService.deleteHtml(id);
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
