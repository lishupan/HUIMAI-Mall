app.service('cartService',function ($http) {
    //读取全部购物车数据
    this.findAll=function () {
    return    $http.get('/cart/findAll.do');
    }

    //调用添加到购物车方法
    this.addGoodsToCart=function (itemId, num) {
      return  $http.get('/cart/addGoodsToCart.do?itemId='+itemId+"&num="+num);
    }
    //计算指定购物车集合，合计对象（购买总数量、总金额）
    this.sum=function (cartList) {
        //定义一个合计对象
        var totalObj={"totalNum":0,"totalFee":0.0};
        //遍历购物车集合
        for(var i=0;i<cartList.length;i++){
            //获取各个购物车对象
            var cart=cartList[i];
            //遍历购物车购物明细
            for(var j=0;j<cart.orderItemList.length;j++){
                var orderItem=cart.orderItemList[j];

                totalObj.totalNum+=orderItem.num;

                totalObj.totalFee+=orderItem.totalFee;
            }
        }

        return totalObj;
    }

    //读取指定用户地址信息
    this.findAddressList=function () {
     return   $http.get('/address/findAddressList.do');
    }
    
    //保存订单方法
    this.submitOrder=function (order) {
      return  $http.post('/order/add.do',order);
    }


    //分页
    this.findPage=function(page,rows){
        return $http.get('../address/findPage.do?page='+page+'&rows='+rows);
    }

    //查询实体
    this.findOne=function(id){
        return $http.get('../address/findOne.do?id='+id);
    }
    //增加
    this.add=function(entity){
        return  $http.post('../address/add.do',entity);
    }
    //修改
    this.update=function(entity){
        return  $http.post('../address/update.do',entity );
    }
    //删除
    this.deleAddess=function(ids){
        return $http.get('../address/delete.do?ids='+ids);
    }
    //搜索
    this.search=function(page,rows,searchEntity){
        return $http.post('../address/search.do?page='+page+"&rows="+rows, searchEntity);
    }
})