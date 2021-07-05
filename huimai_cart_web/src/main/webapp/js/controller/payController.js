app.controller('payController',function ($scope,$location, payService) {
    //发出预下单请求，根据响应结果，生成支付二维码
    $scope.createNative=function () {
        payService.createNative().success(function (response) {

            //存储订单号
            $scope.out_trade_no=response.out_trade_no;
            //订单金额
            $scope.money=((response.total_fee)/100).toFixed(2);
            //创建二维码对象
            new QRious({
              element: document.getElementById('erweima'),
              size: 400,
              level: 'H',
              value:response.qrcode
            });
            //调用查询交易状态方法
            queryPayStatus($scope.out_trade_no);
        })
    }
    
    //订单查询交易状态方法
    queryPayStatus=function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function (response) {
            if(response.success){
                //跳转到支付成功页面
                location.href="paysuccess.html#?money="+$scope.money;
            }else {

                if(response.message=='查询超时'){
                    //在页面显示超时文字
                    document.getElementById('msg').innerHTML='二维码已过期，刷新页面重新获取二维码。';
                }else {
                    alert(response.message);
                }

            }
        })
    }

    //捕获静态路径参数
    $scope.loadMoney=function () {
    return    $location.search()['money'];
    }
})