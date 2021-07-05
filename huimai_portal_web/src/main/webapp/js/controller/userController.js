 //用户表控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		userService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		userService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		userService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=userService.update( $scope.entity ); //修改  
		}else{
			serviceObject=userService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		userService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		userService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//定义entity
	$scope.entity={};
	//新增，注册保存方法
	$scope.reg=function () {

		//校验用户是否输入 用户账号
		if($scope.entity.username==null){
			//提示，用户账号不能为空
			alert('用户账号不能为空');
			return;
		}

		//校验二次密码是否相同
		if($scope.entity.password!=$scope.password){
			alert('二次输入的密码不一致');
			return;
		}

		//校验手机号
		if($scope.entity.phone==null){
			alert('手机号码必须输入');
		}

		//校验通过，开始保存
		userService.add($scope.entity,$scope.smscode).success(function (response) {
			if(response.success){
				alert('用户注册成功');

			}else {
				alert(response.message);
			}
        })
    }

    //发送验证码
	$scope.sendSmsCode=function () {
		if($scope.entity.phone==null){
			alert("手机号码不能为空");
			return;
		}
		userService.sendSmsCode($scope.entity.phone).success(function (response) {
			if(response.success){
				alert("验证码发送成功");
			}else {
				alert(response.message);
			}
        })
    }
    
});	