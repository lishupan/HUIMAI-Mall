package com.huimai.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.huimai.entity.PageResult;
import com.huimai.mapper.TbSpecificationOptionMapper;
import com.huimai.mapper.TbTypeTemplateMapper;
import com.huimai.pojo.TbSpecificationOption;
import com.huimai.pojo.TbSpecificationOptionExample;
import com.huimai.pojo.TbTypeTemplate;
import com.huimai.pojo.TbTypeTemplateExample;
import com.huimai.sellergoods.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * 类型模板服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		//保存模板包含的品牌和规格数据到redis缓存
			this.saveToRedis();
		PageHelper.startPage(pageNum, pageSize);
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		TbTypeTemplateExample.Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> findSpecList(Long id) {
		//根据模板id，读取数据库中模板对象
		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		//获取模板对象里面 规格 json字符串 [{"id":29,"text":"机身内存"},{"id":28,"text":"手机屏幕尺寸"},{"id":27,"text":"网络"}]
		String specJsonStr = typeTemplate.getSpecIds();
		//把json字符串转换为集合
		List<Map> specList = JSON.parseArray(specJsonStr, Map.class);
		//判断json集合是否为空
		if(specList!=null&&specList.size()>0){
			//遍历json集合 {"id":29,"text":"机身内存"}
			for (Map map : specList) {
				//获取规格的编号
			Long specId=new Long((Integer)map.get("id"));
			//创建查询条件，根据规格id，读取对应规格选项数据
				TbSpecificationOptionExample example = new TbSpecificationOptionExample();
				TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
				criteria.andSpecIdEqualTo(specId);
				//发出查询，获取对应规格id的规格选项数据
				List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample(example);
				//把读取到规格选项数据封装到map
				map.put("options",specificationOptionList);
			}
		}
		return specList;
	}

	//保存模板对应的品牌和规格数据到redis缓存
	private void saveToRedis(){
		//1、读取全部的模板数据
		List<TbTypeTemplate> typeTemplateList = this.findAll();
		//遍历模板数据集合
		for (TbTypeTemplate typeTemplate : typeTemplateList) {
			//获取模板对象的品牌数据 [{"id":1,"text":"联想"},{"id":3,"text":"三星"},{"id":2,"text":"华为"},{"id":5,"text":"OPPO"},{"id":4,"text":"小米"},{"id":9,"text":"苹果"},{"id":8,"text":"魅族"},{"id":6,"text":"360"},{"id":10,"text":"VIVO"},{"id":11,"text":"诺基亚"},{"id":12,"text":"锤子"}]
			String brandJsonStr = typeTemplate.getBrandIds();
			//转换为品牌集合
			List<Map> brandList = JSON.parseArray(brandJsonStr, Map.class);
			//写入品牌数据到redis缓存
			redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandList);

			//获取模板对象的规格数据
			/*String specJsonStr = typeTemplate.getSpecIds();
			List<Map> specList = JSON.parseArray(specJsonStr, Map.class);*/
			//调用之前写好的根据模板id，读取对应规格和规格选项数据方法
			List specList=this.findSpecList(typeTemplate.getId());
			//写入规格数据到redis缓存
			redisTemplate.boundHashOps("specList").put(typeTemplate.getId(),specList);
		}

		System.out.println("保存品牌和规格数据到缓存成功");
	}
}
