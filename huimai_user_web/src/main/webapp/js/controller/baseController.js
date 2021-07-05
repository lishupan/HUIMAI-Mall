app.controller('baseController',function ($scope) {
    //定义分页配置 paginationConf
    $scope.paginationConf={
        currentPage: 1,//当前页码
        totalItems: 10,//总记录数
        itemsPerPage: 10,//每页显示的记录数
        perPageOptions: [5,10,15,20],//下拉选择每页要显示的记录数
        onChange: function () {
            //调用后端，获取分页数据
            $scope.reloadList();
        }
    };

    //定义读取后端分页数据方法
    $scope.reloadList=function () {

        //调用findPage从后端获取数据
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }

    //定义一个数组，记录要删除的品牌的编号
    $scope.selectIds=[];

    //当用户选中，或者取消选中，调用本方法，更新要删除的数组记录
    //$event 事件源对象，可以捕获到复选框的状态
    //id 操作的品牌的id
    $scope.updateSelection=function ($event,id) {

        //判断复选框如果是选中，
        if($event.target.checked){
            //把选中的品牌的id，存储到要删除的品牌数组
            $scope.selectIds.push(id);
        }else {
            //如果是取消选中，把数组里面的要删除的品牌id移除
            var index=	$scope.selectIds.indexOf(id);
            //移除
            $scope.selectIds.splice(index,1);
        }
    }
    //提取指定json数组,指定key的值，拼接到一起
    $scope.jsonToString=function (jsonArrayStr, key) {
        //把json字符串转换为josn数组
       var jsonArray= JSON.parse(jsonArrayStr);
       var value="";
       //遍历json数组
        for(var i=0;i<jsonArray.length;i++){
            if(i>0){
                value+=",";
            }
          value+=  jsonArray[i][key];
        }

        return value;
    }
    //搜索指定json数组，查看指定key和value的数值是否存在
    //参数1：要搜索的数组
    $scope.searchObj=function (jsonArray,key,value) {

        //遍历json数组
        for(var i=0;i<jsonArray.length;i++){
            if(jsonArray[i][key]==value){
                //把当前json节点元素返回
                return jsonArray[i];
            }
        }

        return null;

    }
})

