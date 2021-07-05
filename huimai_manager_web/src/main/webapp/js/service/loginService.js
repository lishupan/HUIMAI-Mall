app.service('loginService',function ($http) {
    //读取当前登录用户名
    this.showLoginName=function () {
      return  $http.get('../user/showLoginName.do');
    }
})