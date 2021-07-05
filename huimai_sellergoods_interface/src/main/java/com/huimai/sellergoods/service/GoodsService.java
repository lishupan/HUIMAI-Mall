package com.huimai.sellergoods.service;

import com.huimai.entity.PageResult;
import com.huimai.group.Goods;
import com.huimai.pojo.TbGoods;
import com.huimai.pojo.TbItem;

import java.util.List;
import java.util.Map;

/**
 * 商品服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(Goods goods);


	/**
	 * 修改
	 */
	public void update(Goods goods);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);

	//修改商品状态
	public void updateStatus(Long[] ids,String status);

	//根据审核商品编号数组，状态，获取对应的sku数据集合
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] ids, String status);

	//读取品牌下拉菜单数据方法
	public List<Map> selectOptionList();
}
