 //商品控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承

    //声明商品对象
    $scope.entity={"goods":{"isEnableSpec":"0"},"goodsDesc":{"itemImages":[],"specificationItems":[]},"itemList":[]};
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		//使用$location获取路径传递的参数值
	var id=	$location.search()['id'];
	//判断id是否存在
		if(id==null){
			//结束数据查询
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//把读取到商品介绍数据，设置到富文本编辑器
                editor.html($scope.entity.goodsDesc.introduction);
                //把读取到商品配图，从json字符串转换为josn对象
                $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
                //把商品扩展属性，从json字符串转换为json对象
                $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);

                //把读取到商品选中规格和规格选项json字符串转换为json对象
                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);

                //把读取到sku集合获取
				var items=$scope.entity.itemList;
				for(var i=0;i<items.length;i++){
					//提取每个sku对象的，规格值
                    items[i].spec=	JSON.parse(items[i].spec);
				}
			}
		);				
	}
	
	//保存 
	$scope.save=function(){
		//读取富文本编辑器
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	//$scope.reloadList();//重新加载
					alert(response.message);
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
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
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//保存商品方法
	$scope.add=function () {
		//从富文本编辑器读取内容
        $scope.entity.goodsDesc.introduction=editor.html();
		goodsService.add($scope.entity).success(function (response) {
			if(response.success){
				alert("保存商品成功");
				//清空entity对象
                $scope.entity={"goods":{"isEnableSpec":"0"},"goodsDesc":{"itemImages":[],"specificationItems":[]},"itemList":[]};
				//清空富文本编辑器
				editor.html('');
			}else {
				alert(response.message);
			}
        })
    }

    //上传处理方法
    $scope.uploadFile=function () {
        uploadService.uploadFile().success(function (response) {
            if(response.success){
                alert("文件上传成功");
                //把服务器返回的上传成功的图片地址，设置到图片对象地址属性上
                $scope.image_entity.url=response.message;
            }
        })
    }

    //点击保存，保存图片对象到 商品配图数组
    $scope.add_image_entity=function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //点击删除的时候，移除对应的图片
    $scope.remove_image_entity=function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index,1);

    }

    //获取全部一级分类数据
	$scope.selectItemCat1List=function () {
		itemCatService.findByParentId(0).success(function (response) {
		$scope.itemCat1List=response;
        })
    }
    //监听一级分类的变量，看是否发生变化，就执行调用对应一级分类所属的二级分类数据
	//newValue 变化后的值  oldValue 变化前的值
	$scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {
		//判断newValue是否被定义
		if(newValue){
			//获取该选中的一级分类对应的二级分类数据
			itemCatService.findByParentId(newValue).success(function (response) {
				$scope.itemCat2List=response;
            })
		}
    });

	//监听二级分类的变量，看是否发生变化，执行获取对应三级分类数据
	$scope.$watch('entity.goods.category2Id',function (newValue, oldValue) {
		if(newValue){
			//获取对应三级分类数据
			itemCatService.findByParentId(newValue).success(function (response) {
				$scope.itemCat3List=response;
            })
		}
    });
	//根据三级分类id，获取分类所属模板id
	$scope.$watch('entity.goods.category3Id',function (newValue, oldValue) {
		if(newValue){
			//获取该三级分类id，对应分类对象
			itemCatService.findOne(newValue).success(function (response) {
				//获取分类对象的模板id
				$scope.entity.goods.typeTemplateId=response.typeId;
            })
		}
    });

	//监听模板编号，看是否发生编号，根据最新模板编号获取对应模板对象
	$scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
		if(newValue){
			//调用模板服务，获取对应模板对象
			typeTemplateService.findOne(newValue).success(function (response) {
				$scope.typeTmplate=response;
				//要的是模板对象里面，品牌数据,采用json格式存储[{"id":1,"text":"联想"},{"id":3,"text":"三星"},{"id":2,"text":"华为"},{"id":5,"text":"OPPO"},{"id":4,"text":"小米"},{"id":9,"text":"苹果"},{"id":8,"text":"魅族"},{"id":6,"text":"360"},{"id":10,"text":"VIVO"},{"id":11,"text":"诺基亚"},{"id":12,"text":"锤子"}]
                $scope.typeTmplate.brandIds=JSON.parse($scope.typeTmplate.brandIds);
                //读取模板包含扩展属性，转换为json对象
				//判断url路径传递id参数是否存在
				if($location.search()['id']==null) {
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTmplate.customAttributeItems);
                }
            });
			//根据模板id，获取对应规格和规格选项数据
			typeTemplateService.findSpecList(newValue).success(function (response) {
				$scope.specList=response;
            })
		}
    });

   // $scope.entity={ goodsDesc:{itemImages:[],specificationItems:[]}  };
	//当用户点击 规格选项 复选框的时候，调用本方法
	//参数1：事件源对象 用来获取复选框的是否选中状态
	//参数2：规格名称
	//参数3：规格选项的值
	$scope.updateSpecAttribute=function ($event,name,value) {

		//1、根据规格名称，去查询记录用户选中规格选项数组是否包含该规格
	var obj=$scope.searchObj($scope.entity.goodsDesc.specificationItems,'attributeName',name);

	//2、判断指定规格json对象是否存在
		if(obj!=null){
			//判断复选框的状态
			if($event.target.checked){
				//把规格选项的值，存储到规格选项数组
				obj.attributeValue.push(value);
			}else {
				//取消勾选
				//把规格选项从数组移除
			   var index=	obj.attributeValue.indexOf(value);
				obj.attributeValue.splice(index,1);
				//判断规格选项数组，是否为空，移除整个规格和规格选项对象
				if(obj.attributeValue.length==0){
					//检查当前obj对象的角标
                  var index2=  $scope.entity.goodsDesc.specificationItems.indexOf(obj);
                    $scope.entity.goodsDesc.specificationItems.splice(index2,1);
				}
			}
		}else {
			//第一次选中规格
			//向规格数组，插入一个新的json对象
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]})
		}
    }
    //动态生成sku列表方法
	$scope.createItemList=function () {
		//1、首先声明一个sku集合，只有一条记录，存储sku初始化数据
		$scope.entity.itemList=[{"price":0.0,"num":0,"status":"1","isDefault":0,"spec":{}}];
		//2、根据用户选中规格和规格选项 变量
		var items=$scope.entity.goodsDesc.specificationItems;

		//3、循环遍历用户选中的规格和规格选项集合
		for(var i=0;i<items.length;i++){
			//每个节点元素就是一组规格和规格选项集合
			//{"attributeName":"机身内存","attributeValue":["16G","32G"]}
			//调用扩充sku集合的方法
            $scope.entity.itemList=	addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
    };

	//单独提取定义一个内部方法，扩充sku集合数据
	//参数1：原始sku集合
	//参数2：规格名称
	//参数3：规格选项集合
    addColumn=function (skulist,attributeName,attributeValue) {

    	//定义一个新集合，存储扩充后sku集合数据
		var newList=[];
    	//遍历原始sku集合
		for(var i=0;i<skulist.length;i++){
			//定义变量记录原始sku节点元素内容
			//{"price":0.0,"num":0,"status":"1","isDefault":0,"spec":{}}
			var oldRow=skulist[i];
			//遍历规格选项集合
			for(var j=0;j<attributeValue.length;j++){

				//做一个深克隆
			var newRow=	JSON.parse(JSON.stringify(oldRow));
			//扩充列
				newRow.spec[attributeName]=attributeValue[j];
				//把扩充后sku数据，填充到newList集合
				newList.push(newRow);
			}
		}

		//把全部扩充完成后newList返回
		return newList;
    }

    //定义商品状态数组
	$scope.status=['未审核','审核通过','审核未通过','关闭'];

    //定义一个数组，存放全部的商品分类数据
	$scope.itemCatList=[];

	//调用分类服务，获取全部分类数据
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(function (response) {
			//遍历服务端响应全部分类数据
			for(var i=0;i<response.length;i++){
				//提取分类的名称
			$scope.itemCatList[response[i].id]=response[i].name;
			}
        })
    }

    //判断各个规格选项是否被选中
	$scope.checkAttributeValue=function (specName, optionName) {
		//找到记录用户选中规格集合
	var obj=$scope.searchObj($scope.entity.goodsDesc.specificationItems,'attributeName',specName);
	if(obj!=null){
		//继续判断规格对象是否存在对应的规格选项值
		if(obj.attributeValue.indexOf(optionName)>=0){
			return true;
		}else {
			return false;
		}
	}else {
		return false;
	}
    }
});	