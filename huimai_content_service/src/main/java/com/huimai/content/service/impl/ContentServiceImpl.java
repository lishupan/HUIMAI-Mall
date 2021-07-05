package com.huimai.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.huimai.content.service.ContentService;
import com.huimai.entity.PageResult;
import com.huimai.mapper.TbContentMapper;
import com.huimai.pojo.TbContent;
import com.huimai.pojo.TbContentExample;
import com.huimai.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 内容服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
    private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//清理缓存
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//根据要修改的广告编号，从数据库读取修改前广告数据
		TbContent tbContentDb = contentMapper.selectByPrimaryKey(content.getId());
		//从数据库获取到广告对象，获取对应分类id
		Long categoryIdBefore = tbContentDb.getCategoryId();
		//清理修改前分类id，所对应缓存数据
		redisTemplate.boundHashOps("content").delete(categoryIdBefore);


		contentMapper.updateByPrimaryKey(content);

		//比对广告的分类是否发生修改
		if(content.getCategoryId().longValue()!=categoryIdBefore.longValue()){
			//分类发生修改，清除新分类所对应广告缓存
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//根据删除的广告id，获取对应广告信息，提取对应分类编号
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
			contentMapper.deleteByPrimaryKey(id);
			//清理该分类所对应的缓存
			redisTemplate.boundHashOps("content").delete(categoryId);

		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findContentListByCategoryId(Long categoryId) {
	    //1、从redis，尝试读取广告轮播图的数据
        List<TbContent> contentList= (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
		//2、判断是否从redis缓存读取到广告轮播图数据
        if(contentList==null) {
            TbContentExample example = new TbContentExample();
            example.createCriteria().andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
            //设置排序条件
            example.setOrderByClause("sort_order");
            contentList= contentMapper.selectByExample(example);
            //3、把从数据库读取到广告轮播图数据，写入到redis缓存
            redisTemplate.boundHashOps("content").put(categoryId,contentList);
        }else {
            System.out.println("从缓存成功读取到轮播图数据");
        }

        return contentList;
	}
}
