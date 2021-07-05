 //控制层 
app.controller('addressController' ,function($scope,$controller,addressService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取地址列表数据绑定到表单中
	$scope.findAll=function(){
		addressService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		addressService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		addressService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){
		alert("aaa");
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=addressService.update( $scope.entity ); //修改  
		}else{
			serviceObject=addressService.add( $scope.entity  );//增加 
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
		addressService.dele( $scope.selectIds ).success(
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
		addressService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}


	$scope.entity={};
	$scope.add=function () {
		//校验用户是否输入 用户账号
		if($scope.entity.contact==null){
			//提示，用户账号不能为空
			alert('收货人不能为空');
			return;
		}
		//校验用户是否输入 用户账号
		if($scope.entity.address==null){
			//提示，用户账号不能为空
			alert('收货地址不能为空');
			return;
		}
		//校验用户是否输入 用户账号
		if($scope.entity.mobile==null){
			//提示，用户账号不能为空
			alert('手机号不能为空');
			return;
		}
	    addressService.add().success(function (response) {
            if(response.success){
                //重新查询
				alert('收货地址新增成功');
                $scope.reloadList();//重新加载
            }else{
                alert(response.message);
            }
        });

    }

});	