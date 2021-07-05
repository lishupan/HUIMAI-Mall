package com.huimai.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.huimai.pojo.TbItem;
import com.huimai.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        Map<String, Object> result=new HashMap<>();

        //判断搜索关键字是否保护空格，移除空格
      String keywords=  (String)searchMap.get("keywords");
        if(keywords.indexOf(" ")>=0){
            //移除空格
         keywords=   keywords.replaceAll(" ","");
         //替换searchMap
            searchMap.put("keywords",keywords);
        }
       result.putAll(searchList(searchMap));

       //调用根据查询关键字获取对应分类方法
        List categoryList = searchCategoryList(searchMap);

        //判断，前端传递过来查询条件，分类是否为空白
        if(!"".equals(searchMap.get("category"))){
            //根据前端传递过来分类名称查询对应品牌和规格数据
          result.putAll(searchBrandAndSpecList((String) (searchMap.get("category"))));
        }else {
            //判断分类数据集合是否为空
            if(categoryList!=null&&categoryList.size()>0){
                //提取第一组分类名称，根据分类名称查询对应品牌和规格
                result.putAll(searchBrandAndSpecList((String) (categoryList.get(0))));
            }
        }


        //把分类集合封装到map
        result.put("categoryList",categoryList);



        return result;
    }

    //根据搜索关键字，进行高亮查询
    private Map searchList(Map searchMap){
        Map map=new HashMap();
        //1、创建一个支持高亮的查询器对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //2、创建一个高亮选项对象
        HighlightOptions highlightOptions = new HighlightOptions();
        //配置高亮选项1：高亮处理的域名称
        highlightOptions.addField("item_title");
        //配置高亮前缀内容：
        highlightOptions.setSimplePrefix("<span style='color:red'>");
        //配置高亮后缀
        highlightOptions.setSimplePostfix("</span>");

        //关联高亮选项到查询器对象
        query.setHighlightOptions(highlightOptions);

        //2、创建查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //关联查询条件到查询器对象
        query.addCriteria(criteria);

        //添加过滤条件1：按照用户选中的分类进行过滤
        if(!"".equals(searchMap.get("category"))){
            //创建查询条件
            Criteria criteriaCategory = new Criteria("item_category").is(searchMap.get("category"));

            //创建一个过滤器对象
            SimpleFilterQuery filterQueryCategory = new SimpleFilterQuery(criteriaCategory);
            //关联过滤器对象到查询器对象
            query.addFilterQuery(filterQueryCategory);

        }

        //添加过滤条件2：按照用户选中品牌进行过滤
        if(!"".equals(searchMap.get("brand"))){
            Criteria criteriaBrand = new Criteria("item_brand").is(searchMap.get("brand"));
            SimpleFilterQuery filterQueryBrand = new SimpleFilterQuery(criteriaBrand);
            query.addFilterQuery(filterQueryBrand);
        }

        //添加过滤条件3：按照用户选中的规格选项进行过滤
        if(searchMap.get("spec")!=null){
            //获取规格的查询条件的数据 {机身内存: "32G", 网络: "移动4G", 手机屏幕尺寸: "5寸"}
         Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
         //遍历map
            for (String key : specMap.keySet()) {
                Criteria criteriaSpec = new Criteria("item_spec_" + Pinyin.toPinyin(key, "").toLowerCase()).is(specMap.get(key));
                SimpleFilterQuery filterQuerySpec = new SimpleFilterQuery(criteriaSpec);
                query.addFilterQuery(filterQuerySpec);
            }

        }

        //添加筛选过滤条件4：按照价格区间
       String priceStr= (String) searchMap.get("price");
        //按照中划线切开字符串
        if(priceStr!=null&&priceStr.length()>0){
            String[] prices = priceStr.split("-");

            //设置开始价格筛选条件
            if(!prices[0].equals("0")) {
                Criteria item_priceBegin = new Criteria("item_price").greaterThanEqual(prices[0]);
                SimpleFilterQuery filterQueryBegin = new SimpleFilterQuery(item_priceBegin);
                query.addFilterQuery(filterQueryBegin);
            }
            //设置结束价格的筛选条件
            if(!prices[1].equals("*")) {
                Criteria criteriaEnd = new Criteria("item_price").lessThan(prices[1]);
                SimpleFilterQuery filterQueryEnd = new SimpleFilterQuery(criteriaEnd);
                query.addFilterQuery(filterQueryEnd);
            }
        }

        //接收前端传递的分页参数
        //参数1：要跳转到页码
      Integer pageNo= (Integer) searchMap.get("pageNo");
        //判断前端传递页码参数如果为空，设置一个初始化值 1
        if(pageNo==null){
            pageNo=1;
        }
        //参数2：每页显示的记录数
     Integer pageSize= (Integer) searchMap.get("pageSize");
        //判断前端传递每页显示的记录数为空，就设置一个默认值 10
        if(pageSize==null){
            pageSize=10;
        }

        //计算游标开始位置
      Integer start=  (pageNo-1)*pageSize;

        //设置游标开始位置
        query.setOffset(start);
        //设置每页显示的记录数
        query.setRows(pageSize);

        //接收排序的参数
        //1、接收排序字段
      String sortField= (String) searchMap.get("sortField");
      //2、排序方式
      String sortValue= (String) searchMap.get("sort");

      //判断排序字段是否为空
        if(sortField!=null&&sortField.length()>0){
            //根据排序方式，判断是升序，创建升序排序对象
            if(sortValue!=null&&sortValue.equals("ASC")){
                //创建排序对象
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                //关联排序对象到查询器对象
                query.addSort(sort);
            }
            if(sortValue!=null&&sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }

        //3、发出支持高亮，带分页查询
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //获取高亮实体的结果集合
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        //遍历高亮实体结果集合
        for (HighlightEntry<TbItem> highlightEntry : highlightEntryList) {
            //判断highlightEntry包含高亮数据是否为空
            if(highlightEntry.getHighlights().size()>0&&highlightEntry.getHighlights().get(0).getSnipplets().size()>0){
                //提取标题高亮数据
                List<String> titltHightList = highlightEntry.getHighlights().get(0).getSnipplets();
                //提取高亮结果
                String hightTitlte = titltHightList.get(0);
                //获取查询到数据对象
                highlightEntry.getEntity().setTitle(hightTitlte);

            }
        }


        //4、获取满足查询条件总记录数
        System.out.println("查询到总记录数:"+page.getTotalElements());

        //5、返回查询到结果集合
       map.put("rows",page.getContent()) ;

       //获取总页码
        map.put("totalPages",page.getTotalPages());
        //获取总记录数
        map.put("total",page.getTotalElements());

       return map;


    }

    //根据搜索关键字，进行分组，获取对应分类数据集合
    private List searchCategoryList(Map searchMap){
        List list=new ArrayList();

        //1、创建查询器对象
        SimpleQuery query = new SimpleQuery();
        //2、创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //关联查询条件到查询器对象
        query.addCriteria(criteria);
        //3、创建分组选项对象
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        //关联分组选项对象到查询器对象
        query.setGroupOptions(groupOptions);

        //4、发出分组查询
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        //5、获取分组结果
        List<GroupEntry<TbItem>> groupEntryList = groupPage.getGroupResult("item_category").getGroupEntries().getContent();

        //遍历分组结果集合
        for (GroupEntry<TbItem> groupEntry : groupEntryList) {
          list.add(groupEntry.getGroupValue())  ;
        }

        return list;
    }

    //根据分类名称，获取对应的品牌和规格数据
    private Map searchBrandAndSpecList(String ctaegory){
        Map map=new HashMap();
        //1、使用分类名称作为键，去reids缓存中读取数据（模板id）
      Long typeTemplateId= (Long) redisTemplate.boundHashOps("itemCat").get(ctaegory);
      //判断从reids是否读取到了模板id
        if (typeTemplateId==null){
            throw new RuntimeException("指定分类的模板id，从redis缓存读取失败");
        }else {
            //根据模板id，从reids缓存读取对应品牌数据
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeTemplateId);
            //把读取到品牌数据集合封装到map
            if (brandList != null) {
                map.put("brandList", brandList);
            } else {
                System.out.println("根据指定的模板id:" + typeTemplateId + " 读取对应的品牌数据失败");
            }

            //根据模板id，从redis缓存读取对应规格和规格选项的数据
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeTemplateId);
            if (specList != null) {
                map.put("specList", specList);
            } else {
                System.out.println("根据指定的模板id:" + typeTemplateId + " 读取对应的规格数据失败");
            }
        }
        return map;
    }

    @Override
    public void importSolr(List<TbItem> skuList) {
        //判断sku集合是否为空
        if(skuList!=null&&skuList.size()>0){
            //遍历sku集合
            for (TbItem item : skuList) {

                //获取规格json字符串
                String specjsonStr = item.getSpec();
                //转换为map key 是中文 {"机身内存":"16G","网络":"联通3G"}
                Map<String,String> specMap = JSON.parseObject(specjsonStr, Map.class);
                //创建一个新map，key转换为拼音
                Map<String,String> specMapPY=new HashMap<>();
                for (String key : specMap.keySet()) {
                    specMapPY.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
                }

                //关联拼音map到动态域
                item.setSpecMap(specMapPY);


            }

            //保存到搜索引擎
            solrTemplate.saveBeans(skuList);
            solrTemplate.commit();
            System.out.println("导入商品到solr搜索引擎成功:"+skuList.size());
        }
    }

    @Override
    public void deleteSolr(List goodsIdList) {
     /*   if(goodsIdList!=null&&goodsIdList.size()>0){
            for (Object id : goodsIdList) {
                solrTemplate.deleteById((String) id);
            }
        }*/
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        SimpleQuery query = new SimpleQuery(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();

        System.out.println("删除sorl数据成功:"+goodsIdList.size());
    }
}
