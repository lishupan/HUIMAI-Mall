package com.huimai.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huimai.entity.PageResult;
import com.huimai.entity.Result;
import com.huimai.pojo.TbUser;
import com.huimai.user.service.UserService;
import com.huimai.utils.PhoneFormatCheckUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户表controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll(){
		return userService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows){
		return userService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user, String smscode){
		//调用用户服务，比对用户输入验证码和服务器redis存储的是否相同
		boolean is = userService.checkSmsCode(user.getPhone(), smscode);
		if(!is){
			return new Result(false,"用户注册失败，验证码验证不通过");
		}
		try {
			userService.add(user);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user){
		try {
			userService.update(user);
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
	public TbUser findOne(Long id){
		return userService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			userService.delete(ids);
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
	public PageResult search(@RequestBody TbUser user, int page, int rows  ){
		return userService.findPage(user, page, rows);		
	}


	//获取用户验证码
	@RequestMapping("sendSmsCode")
	public Result sendSmsCode(String mobile){
		boolean is = PhoneFormatCheckUtils.isChinaPhoneLegal(mobile);

		if(is) {
			try {
				userService.createSmsCode(mobile);
				return new Result(true, "短信验证码发送成功");
			} catch (Exception e) {
				e.printStackTrace();
				return new Result(false, "短信验证码发送失败:" + e.getMessage());

			}
		}else {
			return new Result(false,"手机号码不合法");
		}
	}
	@RequestMapping(value = "/showName")
	public Map showName(){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();//得到登录人的账号

		Map map = new HashMap();
		map.put("loginName",name);

		return  map;
	}
}
