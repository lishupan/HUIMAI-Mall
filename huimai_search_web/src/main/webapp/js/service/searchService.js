app.service('searchService',function ($http) {
    //调用搜索接口
    this.search=function (searchMap) {
      return  $http.post('/itemsearch/search.do',searchMap);

    }
})