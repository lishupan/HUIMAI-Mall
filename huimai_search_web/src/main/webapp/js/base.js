var app=angular.module('dongyimai',[]);
//在自定义模块上创建一个自定义过滤器
app.filter('trustHtml',['$sce',function ($sce) {

    //定义转换普通html的内容为安全html内容
  return  function (data) {
      return  $sce.trustAsHtml(data);
    }
}])