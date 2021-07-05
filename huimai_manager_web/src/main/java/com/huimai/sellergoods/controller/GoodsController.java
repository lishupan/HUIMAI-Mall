package com.huimai.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.huimai.entity.PageResult;
import com.huimai.entity.Result;
import com.huimai.group.Goods;
import com.huimai.pojo.TbGoods;
import com.huimai.pojo.TbItem;
import com.huimai.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.dc.pr.PRError;

import javax.jms.*;
import java.util.Arrays;
import java.util.List;

/**
 * 商品controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

//	@Reference
//	private ItemSearchService itemSearchService;

//	@Reference
//	private ItemPageService itemPageService;

	@Autowired
	private JmsTemplate jmsTemplate;

	//注入导入到solr传递消息队列
	@Autowired
	private Destination dongyimai_queue_solr;

	//注入传递删除消息的solr队列
	@Autowired
	private Destination dongyimai_queue_solr_delete;

	//注入审核通过，传递消息的主题
	@Autowired
	private Destination dongyimai_topic_page;

	//注入删除消息主题
	@Autowired
	private Destination dongyimai_topic_page_delete;


	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows){
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//调用搜索服务，删除solr的数据
			//itemSearchService.deleteSolr(Arrays.asList(ids));
			//发送消息到主题
			jmsTemplate.send(dongyimai_topic_page_delete, new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			jmsTemplate.send(dongyimai_queue_solr_delete, new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

	//修改商品状态
	@RequestMapping("updateStatus")
	public Result updateStatus(final Long[] ids, String status){
		try {
			goodsService.updateStatus(ids, status);
			//判断转态是否是审核通过
			if(status.equals("1")){
				//根据商品id，获取对应的sku集合
				List<TbItem> skuList = goodsService.findItemListByGoodsIdandStatus(ids, status);
				//调用搜索服务，更新sku集合数据到搜索引擎
				//itemSearchService.importSolr(skuList);
				//准备发送消息到消息中间件

				//转换sku集合为json字符串
				final String jsonString = JSON.toJSONString(skuList);
				jmsTemplate.send(dongyimai_queue_solr, new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(jsonString);
					}
				});

				//调用静态页面生成服务，生成静态页面
				/*for (Long id : ids) {
					//itemPageService.genItemHtml(id);
				}*/
				//发送消息到主题，传递审核通过信息
				jmsTemplate.send(dongyimai_topic_page, new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
						return session.createObjectMessage(ids);
					}
				});


			}
			return new Result(true,"修改状态成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"修改状态失败");
		}
	}


/*	//临时生成页面生成调用方法
	@RequestMapping("genHtml")
	public void genHtml(Long goodsId){
		itemPageService.genItemHtml(goodsId);
	}*/
}
