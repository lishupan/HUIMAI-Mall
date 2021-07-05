app.controller('contentController',function ($scope,contentService) {

    //在前端定义一个数组，存储全部的广告数据
    $scope.contentList=[];

    //获取指定分类广告数据
    $scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(function (response) {
            //把获取到指定分类的广告数据存放到全部的广告数据数组
       $scope.contentList[categoryId]= response;
        })
    }

    //定义，当用户点击搜索，跳转到搜索web，执行搜索
    $scope.search=function () {
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }

})