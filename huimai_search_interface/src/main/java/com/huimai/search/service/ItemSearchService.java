package com.huimai.search.service;

import com.huimai.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    //根据搜索条件进行查询，返回搜索结果
    public Map<String,Object> search(Map searchMap);

    //获取传递过来sku数据集合，导入sku数据到搜索引擎
    public void importSolr(List<TbItem> skuList);

    //根据商品编号，删除搜索引擎的数据
    public void deleteSolr(List goodsIdList);
}
