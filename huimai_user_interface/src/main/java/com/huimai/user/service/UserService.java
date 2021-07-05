package com.huimai.user.service;

import com.huimai.entity.PageResult;
import com.huimai.pojo.TbUser;

import java.util.List;

/**
 * 用户表服务层接口
 * @author Administrator
 *
 */
public interface UserService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbUser> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(TbUser user);


	/**
	 * 修改
	 */
	public void update(TbUser user);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbUser findOne(Long id);


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
	public PageResult findPage(TbUser user, int pageNum, int pageSize);


	//创建验证码方法
	public void createSmsCode(String mobile);

	//校验指定用户手机号验证码是否相同
	public boolean checkSmsCode(String mobile,String UserSmscode);
}
