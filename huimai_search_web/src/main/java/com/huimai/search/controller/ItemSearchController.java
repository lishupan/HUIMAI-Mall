package com.huimai.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huimai.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemsearch")
public class ItemSearchController {

    @Reference
    private ItemSearchService itemSearchService;

    //搜索方法
    @RequestMapping("search")
    public Map<String,Object> search(@RequestBody Map searchMap){
        return itemSearchService.search(searchMap);
    }
}
