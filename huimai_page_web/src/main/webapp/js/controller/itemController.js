app.controller('itemController',function ($scope,$http) {

    //点击加号或者减号，修改购买数量
    $scope.addNum=function (x) {
        $scope.num=$scope.num+x;

        //判断调整后购买数量，是否小于1
        if($scope.num<1){
            $scope.num=1;
        }
    }

    //定义一个对象，记录用户选中规格选项
    $scope.specificationItems={};

    //当用户点击规格选项，记录用户选中的规格选项
    //name 规格名称  value 选项的值
    $scope.selectSpecification=function (name, value) {
        $scope.specificationItems[name]=value;
        //调用查找匹配sku
        serachSku();
    }
    
    //判断指定规格和规格选项是否被选中
    $scope.isSelected=function (name, value) {
        if($scope.specificationItems[name]==value){
            return true;
        }else {
            return false;
        }
    }
    //加载默认sku数据
    $scope.loadSku=function () {
        //提取第一sku对象，就是默认sku信息
      $scope.sku=skuList[0];
      //设置sku对象里面规格属性，给记录用户选中 规格对象
        $scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));
    }

    //比对两个json对象，看是否完全相等
    matchObject=function (json1, json2) {
        //遍历json1，提取内容和json2对应的角标的内容进行比对
        for(var i in json1){
            if(json1[i]!=json2[i]){
                return false;
            }
        }

        //遍历json2，提取内容，和json1对应角标内容进行比对
        for(var j in json2){
            if(json2[j]!=json1[j]){
                return false;
            }
        }

        return true;
    }

    //当用户点选规格选项的时候，调用本方法，比对用户选中规格数据和对应sku的规格数据是否相同
    serachSku=function () {
        //遍历sku集合
        for(var i=0;i<skuList.length;i++){
            //比对sku的规格属性和 用户选中规格数据
            if(matchObject(skuList[i].spec,$scope.specificationItems)){
                //把当前sku节点对象赋值给 sku变量
                $scope.sku=skuList[i];
                //匹配成功，结束循环
                return;
            }
        }

        $scope.sku={id:0,title:'---',price:0};
    }

    //点击添加到购物车，调用方法
    $scope.addToCart=function () {
        //alert('添加商品:'+$scope.sku.id+"到购物车成功");
        $http.get('http://localhost:9107/cart/addGoodsToCart.do?itemId='+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(function (response) {
            if(response.success){
                alert("添加商品到购物车成功");
                location.href="http://localhost:9107/cart.html";
            }
        })
    }
})