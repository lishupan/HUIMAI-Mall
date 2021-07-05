package com.huimai;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppStart {

    public static void main(String[] args) {
        //加载spring配置文件
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
        //获取导入程序
       SolrUtil solrUtil= (SolrUtil) context.getBean("solrUtil");

       //调用导入方法
        solrUtil.importItemToSolr();
    }
}
