app.service('uploadService',function ($http) {
    //定义文件上传方法
    this.uploadFile=function () {
        //创建一个封装表单数据对象
      var formData=  new FormData();
      //把选择好的上传文件封装到表单对象
        formData.append("file",file.files[0]);

      return  $http({
            //请求方法 get、post
            method: 'POST',
            //请求地址
            url: '/upload.do',
            //要上传文件数据
            data: formData,
            //设置请求头为未定义格式 （避免按照原来的json格式请求头）
           headers: {'Content-Type':undefined},
            //设置数据传输格式按照文件方式进行编码
            transformRequest: angular.identity
        });
    }
})