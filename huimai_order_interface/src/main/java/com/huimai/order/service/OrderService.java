package com.huimai.order.service;

import com.huimai.entity.PageResult;
import com.huimai.pojo.TbOrder;
import com.huimai.pojo.TbPayLog;

import java.util.List;

/**
 * 订单服务层接口
 * @author Administrator
 *
 */
public interface OrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(TbOrder order);


	/**
	 * 修改
	 */
	public void update(TbOrder order);


	/**
	 * 根据ID获取实体
	 * @param
	 * @return
	 */
	public TbOrder findOne(Long orderId);


	/**
	 * 批量删除
	 * @param
	 */
	public void delete(Long[] orderIds);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbOrder order, int pageNum, int pageSize);


	//读取指定用户的支付日志
	public TbPayLog findPayLogByUserId(String userId);


	//根据订单编号，或者支付宝返回交易流水号 修改订单状态
	public void updateOrderStatus(String out_trade_no,String trade_no);
	
}
