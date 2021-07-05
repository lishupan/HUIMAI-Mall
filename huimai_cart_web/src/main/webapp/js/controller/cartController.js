app.controller('cartController',function ($scope,$controller   ,cartService) {

    $controller('baseController',{$scope:$scope});//继承

    //读取全部购物车数据方法
    $scope.findAll=function () {
        cartService.findAll().success(function (response) {
            $scope.cartList=response;
            //调用合计方法
         $scope.totalObj=   cartService.sum($scope.cartList);
        })
    }

    //添加购物车方法
    $scope.addGoodsToCart=function (itemId, num) {
        cartService.addGoodsToCart(itemId,num).success(function (response) {
            if(response.success){
                //alert("添加到购物车成功")
                //刷新购物车列表
                $scope.findAll();
            }else {
                alert(response.message);
            }
        })
    }

    //读取当前登录账号地址集合
    $scope.findAddressList=function () {
        cartService.findAddressList().success(function (response) {
            $scope.addressList=response;
            //遍历地址集合
            for(var i=0;i<$scope.addressList.length;i++){
                //判断地址是否默认
                if($scope.addressList[i].isDefault=='1'){
                    //设置该地址对象到记录选中地址变量
                    $scope.address=$scope.addressList[i];
                }
            }
        })
    }

    //当用户选择指定地址，调用方法，记录选中地址
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }
    //判断指定地址是否被选中
    $scope.isSelect=function (address) {
        if(address==$scope.address){
            return true;
        }else {
            return false;
        }
    }

    //定义支付方式存储
    $scope.order={"paymentType":"1"};

    //当用户选中支付方式，记录支付方式
    $scope.selectPayType=function (type) {
        $scope.order.paymentType=type;
    }
    
    //保存提交订单方法
    $scope.submitOrder=function () {
        //关联送货地址、收货人、电话 信息到订单对象
        $scope.order.receiverAreaName=$scope.address.address;
        $scope.order.receiver=$scope.address.contact;
        $scope.order.receiverMobile=$scope.address.mobile;

        cartService.submitOrder($scope.order).success(function (response) {
            if(response.success){
                //判断支付方式，如果是在线支付，跳转到扫码付款页面
                if($scope.order.paymentType=='1'){
                    location.href="pay.html";
                }else {
                    location.href="paysuccess.html";
                }
            }else {
                alert(response.message);
            }
        })
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


    $scope.entity={};
    $scope.addAddress=function () {
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
        cartService.add($scope.entity).success(function (response) {
            if(response.success){
                //重新查询
                alert('收货地址新增成功');
                $scope.reloadList();//重新加载
            }else{
                alert(response.message);
            }
        });

    }

    //查询实体
    $scope.findOne=function(id){
        cartService.findOne(id).success(
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
            serviceObject=cartService.update( $scope.entity ); //修改
        }else{
            serviceObject=cartService.add( $scope.entity  );//增加
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
    $scope.deleAddress=function(){
        //获取选中的复选框
        cartService.dele( $scope.selectIds ).success(
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
    $scope.search=function(page,rows) {
        cartService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );

    }



})