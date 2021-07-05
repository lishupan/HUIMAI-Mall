package com.huimai.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.huimai.entity.PageResult;
import com.huimai.group.Specification;
import com.huimai.mapper.TbSpecificationMapper;
import com.huimai.mapper.TbSpecificationOptionMapper;
import com.huimai.pojo.TbSpecification;
import com.huimai.pojo.TbSpecificationExample;
import com.huimai.pojo.TbSpecificationOption;
import com.huimai.pojo.TbSpecificationOptionExample;
import com.huimai.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 规格服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		//首先保存规格数据，到数据库
		specificationMapper.insert(specification.getTbSpecification());
		//提取规格选项集合
		for (TbSpecificationOption specificationOption : specification.getTbSpecificationOptionList()) {
			//设置规格id，关联到规格选项对象
			specificationOption.setSpecId(specification.getTbSpecification().getId());
			//保存规格选项数据到数据库
			specificationOptionMapper.insert(specificationOption);
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		specificationMapper.updateByPrimaryKey(specification.getTbSpecification());
		//创建一个删除条件，把指定规格id，规格选项数据，全部删除
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(specification.getTbSpecification().getId());
		//删除规格选项
		specificationOptionMapper.deleteByExample(example);
		//获取规格选项集合
		for (TbSpecificationOption specificationOption : specification.getTbSpecificationOptionList()) {
			//关联规格id
			specificationOption.setSpecId(specification.getTbSpecification().getId());
			//保存规格选项数据到数据库
			specificationOptionMapper.insert(specificationOption);
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//创建一个组合对象
		Specification specification = new Specification();
      //规格数据
		specification.setTbSpecification(specificationMapper.selectByPrimaryKey(id));

		//创建规格选项的查询条件
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(id);
		//查询满足指定规格id的规格选项数据
		List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample(example);
		specification.setTbSpecificationOptionList(specificationOptionList);
		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//删除规格
			specificationMapper.deleteByPrimaryKey(id);
			//删除对应的规格选项
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(id);
			specificationOptionMapper.deleteByExample(example);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		TbSpecificationExample.Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return specificationMapper.selectOptionList();
	}
}
