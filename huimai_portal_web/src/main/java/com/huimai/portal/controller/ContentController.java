package com.huimai.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.huimai.content.service.ContentService;
import com.huimai.pojo.TbContent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("content")
public class ContentController {

    //dubbo远程调用广告服务
    @Reference
    private ContentService contentService;

    //根据指定分类编号，获取对应的广告数据
    @RequestMapping("findByCategoryId")
    public List<TbContent> findByCategoryId(Long categoryId){
        return contentService.findContentListByCategoryId(categoryId);
    }
}
