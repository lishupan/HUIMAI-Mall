package com.huimai.page.service.impl;

import com.huimai.mapper.TbGoodsDescMapper;
import com.huimai.mapper.TbGoodsMapper;
import com.huimai.mapper.TbItemCatMapper;
import com.huimai.mapper.TbItemMapper;
import com.huimai.pojo.TbGoods;
import com.huimai.pojo.TbGoodsDesc;
import com.huimai.pojo.TbItem;
import com.huimai.pojo.TbItemExample;
import com.huimai.page.service.ItemPageService;
import com.huimai.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {
    //把配置文件，声明静态页面生成目录，注入
    @Value("${pageDir}")
    private String pageDir;

    //把freemarker模板配置注入
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;
    
    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Override
    public void genItemHtml(Long goodsId) {


        //使用spring声明freeMarkerConfigurer获取配置对象
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        //加载模板
        try {
            Template template = configuration.getTemplate("item.ftl");

            //创建数据模型
            Map dataModel=new HashMap();

            //读取商品基本信息
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            //把商品基本信息封装到数据模型
            dataModel.put("goods",goods);
            
            //根据一级分类id，获取分类对象
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();

            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);
            //读取商品扩展信息
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            //把商品扩展信息封装到数据模型
            dataModel.put("goodsDesc",goodsDesc);

            //读取sku集合
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            //设置条件，对应goodsid
            criteria.andGoodsIdEqualTo(goodsId);
            //设置sku状态为1 正常
            criteria.andStatusEqualTo("1");
            //设置按照是否默认来降序排序
            example.setOrderByClause("is_default desc");

            List<TbItem> itemList = itemMapper.selectByExample(example);

            //封装到数据模型
            dataModel.put("itemList",itemList);

            //指定输出文件
            FileWriter out = new FileWriter(new File(pageDir, goodsId + ".html"));

            //调用模板执行渲染输出
            template.process(dataModel,out);

            out.close();

            System.out.println("静态页面生成成功:"+goodsId);


        } catch (IOException e) {
            e.printStackTrace();
        }catch (TemplateException e){
            e.printStackTrace();
        }

    }

    @Override
    public void deleteHtml(Long goodsId) {
        try {
            new File(pageDir,goodsId+".html").delete();
            System.out.println("删除id："+goodsId+" 静态页面成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
