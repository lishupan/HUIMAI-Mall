app.controller('searchController',function ($scope,$location, searchService) {
    
    //读取查询
    $scope.search=function () {
        //把要跳转到页码转换为整数
        $scope.searchMap.pageNo=  parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap=response;
            //调用分页构建方法
            buildPageLabel();
        })
    }
    //定义一个json对象，存储搜索条件
    //keywords 搜索关键字  category 商品分类   brand 商品品牌  spec 规格选项 price 价格区间
    //pageNo 当前页码 pageSize 每页显示的记录数
    //排序方式 sort   排序的字段 sortField
    $scope.searchMap={"keywords":"","category":"","brand":"","spec":{},"price":"","pageNo":1,"pageSize":10,"sort":"","sortField":""};

    //新增一个方法：当用户点击 分类、品牌、规格选项的时候，调用此方法，记录用户点选的记录
    //key 搜索条件的 key值
    //value 具体的数据
    $scope.addSearchItem=function (key, value) {

        //判断key的类型 如果是 品牌或分类，直接value赋值
        if(key=='category'||key=='brand'||key=='price'){
            $scope.searchMap[key]=value;
        }else  {
            $scope.searchMap.spec[key]=value;
        }

        //重置当前页码为1
        $scope.searchMap.pageNo=1;
        //向后端发出查询请求
        $scope.search();

    }

    //当用户点击 移除的时候，移除对应的搜索条件
    $scope.removeSearchItem=function (key) {
        if(key=='category'||key=='brand'||key=='price'){
            $scope.searchMap[key]='';
        }else {
            //从json对象把对应key节点移除
         delete   $scope.searchMap.spec[key];
        }

        //重置当前页码为1
        $scope.searchMap.pageNo=1;
        //向后端发出查询请求
        $scope.search();
    }

    //构建分页页码方法
    buildPageLabel=function () {
        //创建一个数组，用来存储要显示的页码
        $scope.pageLabel=[];

        //获取最大页码（总页码）
      var maxPageNo=  $scope.resultMap.totalPages;
      //开始页码
        var firstPage=1;
        //截至页码
        var lastPage=maxPageNo;

        //设置前面显示省略号
        $scope.firstDot=true;
        //设置后面显示省略号
        $scope.lastDot=true;

        //判断总页码大于5页，就按照3中情况进行处理
        if(maxPageNo>5){
            //第一种情况，当前页码 小于等于3 显示的是前5页
            if($scope.searchMap.pageNo<=3){
                //设置截至页码等于5
                lastPage=5;
                //开始位置不显示省略号
                $scope.firstDot=false;
            }else if($scope.searchMap.pageNo+2>=maxPageNo){
                //设置开始页码
                firstPage=maxPageNo-4;
                //最后3页，后面不显示省略号
                $scope.lastDot=false;
            }else {
                //设置开始页码
                firstPage=$scope.searchMap.pageNo-2;
                //设置截至页码
                lastPage=$scope.searchMap.pageNo+2;

            }
        }else {
            //总页码小于等于5，前后都不显示省略号
            $scope.firstDot=false;
            $scope.lastDot=false;
        }

        //按照开始、截至位置循环，写入页码到数组
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }

    }

    //跳转到指定页码方法
    $scope.queryByPage=function (page) {
        if(page<1||page>$scope.resultMap.totalPages){
            return;
        }
        //设置页码为当前页码
        $scope.searchMap.pageNo=page;

        //重新搜索
        $scope.search();
    }

    //判断当前页是否是第一页
    $scope.isTopPage=function () {
        if($scope.searchMap.pageNo==1){
            return true;
        }else {
            return false;
        }
    }

    //定义返回结果的数据结构
    $scope.resultMap={totalPages:1};
    //判断当前页码是最后一页
    $scope.isEndPage=function () {
        if($scope.resultMap.totalPages==$scope.searchMap.pageNo){
            return true;
        }else {
            return false;
        }
    }

    //判断要跳转到的页码和搜索条件存储的当前页码是否相等
    $scope.isPage=function (page) {
        if($scope.searchMap.pageNo==page){
            return true;
        }else {
            return false;
        }
    }

    //当用户点击 指定字段，进行排序规则设置
    $scope.sortSearch=function (sortField, sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        //发出查询
        $scope.search();
    }

    //判断搜索关键字，是否包含了品牌
    $scope.keywordsIsBrand=function () {
        //获取全部的品牌集合，变量品牌集合
      for(var i=0;i<$scope.resultMap.brandList.length;i++){
          //比对搜索关键字里面是否保护了品牌
          if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
              return true;
          }
      }

      return false;
    }

    //获取页面传递查询的关键字
    $scope.loadkeywords=function () {
        //捕获首页传递饿搜索关键字路径参数
     //设置到searchMap
        $scope.searchMap.keywords= $location.search()['keywords'];
        //发出查询
        $scope.search();
    }
})