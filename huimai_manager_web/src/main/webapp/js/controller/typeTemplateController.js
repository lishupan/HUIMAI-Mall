 //类型模板控制层 
app.controller('typeTemplateController' ,function($scope,$controller   ,typeTemplateService,brandService,specificationService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		typeTemplateService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		typeTemplateService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		typeTemplateService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//把读取到品牌数据转换为json对象
                $scope.entity.brandIds=	JSON.parse($scope.entity.brandIds);
                //把读取到规格数据转换为json对象
                $scope.entity.specIds=JSON.parse($scope.entity.specIds);
                //把读取到扩展属性，转换为json对象
                $scope.entity.customAttributeItems=JSON.parse($scope.entity.customAttributeItems);
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=typeTemplateService.update( $scope.entity ); //修改  
		}else{
			serviceObject=typeTemplateService.add( $scope.entity  );//增加 
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
		typeTemplateService.dele( $scope.selectIds ).success(
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
		typeTemplateService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//定义一个品牌下拉菜单数据对象
	$scope.brandList={data:[{"id":1,"text":"小米"},{"id":2,"text":"华为"},{"id":3,"text":"三星"}]};


	//调用品牌服务，读取下拉菜单数据
	$scope.findBrandList=function () {
		brandService.selectOptionList().success(function (response) {

			$scope.brandList={data:response};
        })
    }

    //定义一个规格下拉菜单 数据对象
	$scope.specList={data:[{"id":1,"text":"颜色"},{"id":2,"text":"尺寸"}]};

	//调用规格服务，读取规格的下拉菜单数据
	$scope.findSpecList=function () {
		specificationService.selectOptionList().success(function (response) {
			$scope.specList={data:response};
        })
    }

    //定义模板对象
	$scope.entity={"customAttributeItems":[]};

	//当用户点击 新增扩展属性按钮 新增扩展属性行
	$scope.addTableRow=function () {
		$scope.entity.customAttributeItems.push({});
    }

    //当用户点击  删除  按钮 删除对应扩展属性行
	$scope.deleteTableRow=function (index) {
		$scope.entity.customAttributeItems.splice(index,1);
    }

    
});	