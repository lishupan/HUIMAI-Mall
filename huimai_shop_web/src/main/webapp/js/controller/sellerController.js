 //商户控制层 
app.controller('sellerController' ,function($scope,$location,$controller   ,sellerService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中
	$scope.findAll=function(){
		sellerService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		sellerService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//获取当前登录用户
	$scope.showLoginName=function () {
		sellerService.showLoginName().success(function (response) {
			$scope.loginName=response.loginName;
		})
	}

	//查询实体 
	$scope.findOne=function(sellerId){
		if(sellerId==null){
			alert("用户名或密码输入有误，请检查！");
			sellerId=$scope.loginName;
		}

		sellerService.findOne(sellerId).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}

	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=sellerService.update( $scope.entity ); //修改  
		}else{
			serviceObject=sellerService.add( $scope.entity  );//增加 
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
		sellerService.dele( $scope.selectIds ).success(
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
		sellerService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//商户注册保存方法
	$scope.add=function () {
		sellerService.add($scope.entity).success(function (response) {
			if(response.success){
				alert("已提交审核给平台，审核结果发送到您注册的邮箱中，请耐心等候");

				location.href="shoplogin.html";

			}else {
				alert(response.message);
			}
        })
    }
    
});	