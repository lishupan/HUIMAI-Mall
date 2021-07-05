package com.huimai;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.huimai.mapper.TbItemMapper;
import com.huimai.pojo.TbItem;
import com.huimai.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    //注入sku表数据操作接口
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    //读取sku数据，执行导入
    public void importItemToSolr(){
        //创建sku查询条件
        TbItemExample example = new TbItemExample();
        example.createCriteria().andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(example);
        //遍历sku集合
        for (TbItem item : itemList) {
            System.out.println("标题:"+item.getTitle());
            //导入到搜索引擎代码
            //提取规格数据 {"机身内存":"16G","网络":"联通3G"}
            String specJsonStr = item.getSpec();
            //转换json字符串为Map
            Map<String,String> specMap = JSON.parseObject(specJsonStr, Map.class);
            //创建一个新的map，存储key是拼音规格值
            Map<String,String> specMapPY=new HashMap<String, String>();
            //遍历specMap
            for (String key : specMap.keySet()) {
                specMapPY.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
            }
            //把map关联到动态域
            item.setSpecMap(specMapPY);
        }

        //调用solrTemplate批量保存数据到solr
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("导入搜索引擎成功");
    }



}
