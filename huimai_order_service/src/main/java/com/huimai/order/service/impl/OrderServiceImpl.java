package com.huimai.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.huimai.order.service.OrderService;
import com.huimai.utils.IdWorker;
import com.huimai.entity.PageResult;
import com.huimai.group.Cart;
import com.huimai.mapper.TbOrderItemMapper;
import com.huimai.mapper.TbOrderMapper;
import com.huimai.mapper.TbPayLogMapper;
import com.huimai.pojo.TbOrder;
import com.huimai.pojo.TbOrderExample;
import com.huimai.pojo.TbOrderExample.Criteria;
import com.huimai.pojo.TbOrderItem;
import com.huimai.pojo.TbPayLog;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private TbPayLogMapper payLogMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//1、读取购物车数据
	List<Cart> cartList= (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
	//判断从redis读取购物车数据不为空
		if(cartList==null){
			throw new RuntimeException("购物车数据为空，保存订单失败");
		}

		//记录全部订单的金额
		double total_money=0.0;
		//创建一个集合，存储全部的订单编号
		List orderList=new ArrayList();
		//2、遍历购物车集合
		for (Cart cart : cartList) {
			//3、使用idWorker生成器生成一个订单编号
			long orderId = idWorker.nextId();
			//当地订单编号，加入到订单数组
			orderList.add(orderId);
			//创建订单对象
			TbOrder tborder = new TbOrder();
			//tborder.setReceiver(order.getReceiver());
			//使用对象复制工具类，复制目标对象的值到另一个对象
			BeanUtils.copyProperties(order,tborder);
			//设置订单编号
			tborder.setOrderId(orderId);
			//设置订单的状态 1 未付款
			tborder.setStatus("1");
			//设置订单创建时间
			tborder.setCreateTime(new Date());
			//设置更新时间
			tborder.setUpdateTime(new Date());
			//设置商家编号
			tborder.setSellerId(cart.getSellerId());

			//定义一个变量记录本购物车全部订单明细合计金额
			double money=0.0;
			//获取购物明细集合，遍历集合
			for (TbOrderItem orderItem : cart.getOrderItemList()) {

				//设置订单明细id
				orderItem.setId(idWorker.nextId());
				//关联订单编号
				orderItem.setOrderId(orderId);
				//设置商家编号
				orderItem.setSellerId(cart.getSellerId());


				money+=orderItem.getTotalFee().doubleValue();

				//保存订单明细到数据库
				orderItemMapper.insert(orderItem);
			}

			//设置订单的实付金额
			tborder.setPayment(new BigDecimal(money));

			//累加各个订单的金额
			total_money+=money;
			//保存订单到数据库
			orderMapper.insert(tborder);




		}

		//判断支付类型，是否是在线扫码支付，产生支付日志
		if("1".equals(order.getPaymentType())){
			//创建支付日志对象
			TbPayLog payLog = new TbPayLog();

			//设置支付日志支付订单号
		String out_trade_no=	idWorker.nextId()+"";
			payLog.setOutTradeNo(out_trade_no);
			//设置创建日期时间
			payLog.setCreateTime(new Date());

			BigDecimal bigDecimal_total_money_Yuan = new BigDecimal(total_money);
			BigDecimal cs = new BigDecimal(100);
			BigDecimal bigDecimal_total_money_Fen = bigDecimal_total_money_Yuan.multiply(cs);

			//设置当前用户要支付的总金额
			payLog.setTotalFee(bigDecimal_total_money_Fen.toBigInteger().longValue());

			//设置用户编号
			payLog.setUserId(order.getUserId());
			//设置交易状态 0 未付款
			payLog.setTradeState("0");
			//转换list集合为字符串 [ 1001,1002,1003]
			String orderStr = orderList.toString().replace("[", "").replace("]", "").replace(" ", "");
			//设置订单编号列表到支付日志
			payLog.setOrderList(orderStr);
			//设置支付类型
			payLog.setPayType("1");

			//保存支付日志到数据库
			payLogMapper.insert(payLog);

			//同时保存支付日志到redis
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);


		}

		//清空当前用户购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public TbPayLog findPayLogByUserId(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String trade_no) {
		//1、修改支付日志的状态
		//根据订单编号，读取数据库中支付日志
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		//修改支付状态 1支付成功
		payLog.setTradeState("1");
		//更新保存支付完成时间
		payLog.setPayTime(new Date());
		//设置支付宝返回的交易流水号
		payLog.setTransactionId(trade_no);
		//更新保存支付日志到数据库
		payLogMapper.updateByPrimaryKey(payLog);

		//2、修改对应的订单饿状态
		String orderListStr = payLog.getOrderList();
		String[] orderIds = orderListStr.split(",");
		//遍历订单集合
		for (String orderId : orderIds) {
			//根据订单编号，查询数据库订单信息
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
			//修改订单状态
			order.setStatus("2");
			//更新保存订单到数据库
			orderMapper.updateByPrimaryKey(order);
		}

		//3、清理redis缓存中支付日志记录
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());



	}
}
