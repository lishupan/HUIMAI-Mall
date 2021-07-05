package com.huimai.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.huimai.entity.PageResult;
import com.huimai.group.Goods;
import com.huimai.mapper.*;
import com.huimai.pojo.*;
import com.huimai.sellergoods.service.GoodsService;
import com.huimai.mapper.*;
import com.huimai.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 商品服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbItemMapper itemMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//设置商品基本信息对象，状态是待审核
		goods.getGoods().setAuditStatus("0");
		//保存spu数据到数据库
		goodsMapper.insert(goods.getGoods());

		//模拟一个错误
		/*try {
			int i=1/0;
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		//关联商品id到商品扩展信息对象
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		//保存商品信息扩展表数据
		goodsDescMapper.insert(goods.getGoodsDesc());
		//调用公共的保存sku的方法
		saveItemList(goods);
	}

	//提取出一个公共的方法，复制保存sku数据到数据
	private void saveItemList(Goods goods){
		//判断规格是否启用
		if("1".equals(goods.getGoods().getIsEnableSpec())) {
			//保存sku数据
			for (TbItem item : goods.getItemList()) {
				//sku商品标题
				//获取商品名称
				String goodsName = goods.getGoods().getGoodsName();
				//{"手机屏幕尺寸":"5寸","机身内存":"32G","网络":"移动4G"}
				Map specMap = JSON.parseObject(item.getSpec(), Map.class);
				for (Object key : specMap.keySet()) {
					goodsName += " " + specMap.get(key);
				}
				//设置sku商品标题
				item.setTitle(goodsName);

				//调用公共的设置sku属性的方法
				setItem(goods,item);

				//保存sku数据到数据库

				itemMapper.insert(item);


			}
		}else {
			//如果不启用规格，保存一条sku记录到sku数据表
			TbItem item = new TbItem();
			//设置sku商品标题
			item.setTitle(goods.getGoods().getGoodsName());
			//价格
			item.setPrice(goods.getGoods().getPrice());
			//设置商品状态
			item.setStatus("1");
			//是否默认
			item.setIsDefault("1");
			//库存
			item.setNum(9999);
			//规格
			item.setSpec("{}");
			//调用公共的设置sku属性的方法
			setItem(goods,item);
			//保存sku到数据库
			itemMapper.insert(item);
		}
	}

	//提取一个公共设置sku对象属性方法
	private void setItem(Goods goods,TbItem item){
		//设置商品编号
		item.setGoodsId(goods.getGoods().getId());
		//商品所属商家编号
		item.setSellerId(goods.getGoods().getSellerId());
		//设置分类id 设置三级分类id
		item.setCategoryid(goods.getGoods().getCategory3Id());
		//设置sku创建日期
		item.setCreateTime(new Date());
		//设置更新时间
		item.setUpdateTime(new Date());
		//设置商品品牌名称
		Long brandId = goods.getGoods().getBrandId();
		//从数据库读取品牌信息
		TbBrand brand = brandMapper.selectByPrimaryKey(brandId);
		//设置品牌名称到sku对象
		item.setBrand(brand.getName());

		//商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		//设置商家名称到sku对象
		item.setSeller(seller.getName());

		//分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		//设置分类名称到sku对象
		item.setCategory(itemCat.getName());

		//设商品配图
		//获取商品扩展信息对象商品配图集合
		String itemImagesJson = goods.getGoodsDesc().getItemImages();
		//解析配图json字符串为集合
		List<Map> imageList = JSON.parseArray(itemImagesJson, Map.class);
		//判断配图集合不为空，存在数据，提取集合第一张图片，设置到sku图片属性
		if (imageList != null && imageList.size() > 0) {
			item.setImage((String) (imageList.get(0).get("url")));
		}
	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//当发生修改，要把商品状态重置未待审核
		goods.getGoods().setAuditStatus("0");

		//1、修改商品基本信息表
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//2、修改扩展信息表
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//3、创建sku删除条件
		TbItemExample example = new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		//4	调用公共的保存sku的方法
		saveItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		//创建一个组合对象
		Goods goods = new Goods();

		goods.setGoods(goodsMapper.selectByPrimaryKey(id));
		//继续查询对应商品扩展信息

		//查询扩展信息
		goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));
		//查询sku集合
		TbItemExample example = new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(id);
		goods.setItemList(itemMapper.selectByExample(example));

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//goodsMapper.deleteByPrimaryKey(id);
			//根据商品编号，获取商品信息
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//修改商品删除的状态
			goods.setIsDelete("1");
			//更新保存到数据库
			goodsMapper.updateByPrimaryKey(goods);

			//创建查询条件，根据商品id，获取对应的sku数据集合
			TbItemExample example = new TbItemExample();
			example.createCriteria().andGoodsIdEqualTo(id);

			List<TbItem> itemList = itemMapper.selectByExample(example);
			//遍历sku集合
			for (TbItem item : itemList) {
				//修改sku的状态为 3 删除
				item.setStatus("3");
				//更新保存到数据库
				itemMapper.updateByPrimaryKey(item);
			}
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		TbGoodsExample.Criteria criteria = example.createCriteria();
		//test test001
		if(goods!=null){
			//精确匹配商家编号
			if(goods.getSellerId()!=null&&goods.getSellerId().length()>0) {
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
						/*if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
			}	*/		if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}		/*	if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}	*/
		}

		//统一排除删除标记位不为空
			criteria.andIsDeleteIsNull();
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		//遍历商品id数组
		for (Long id : ids) {
			//1、修改商品基本信息审核状态
			//1.1、获取商品基本信息对象
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//1.2、修改商品的状态
			goods.setAuditStatus(status);
			//1.3、更新保存到数据库
			goodsMapper.updateByPrimaryKey(goods);

			//2.1根据商品编号，读取sku对象
			TbItemExample example = new TbItemExample();
			example.createCriteria().andGoodsIdEqualTo(id);
			List<TbItem> itemList = itemMapper.selectByExample(example);
			//遍历sku集合
			for (TbItem item : itemList) {
				//修改状态
				if(status.equals("0")||status.equals("2")||status.equals("3")){
					//修改sku状态为下架
					item.setStatus("2");
				}else  if(status.equals("1")){
					//修改sku状态为正常
					item.setStatus("1");
				}
				//更新保存sku信息到数据库
				itemMapper.updateByPrimaryKey(item);

			}
		}
	}

	@Override
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] ids, String status) {

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(ids));
		criteria.andStatusEqualTo(status);
		return itemMapper.selectByExample(example);
	}

	@Override
	public List<Map> selectOptionList() {
		return goodsMapper.selectOptionList();
	}
}
