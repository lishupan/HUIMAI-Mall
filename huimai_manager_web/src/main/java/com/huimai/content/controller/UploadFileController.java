package com.huimai.content.controller;

import com.huimai.entity.Result;
import com.huimai.utils.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadFileController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    //文件上传方法
    @RequestMapping("upload")
    public Result upload(MultipartFile file){
        //1、获取上传文件的原始名称  1.png  2.txt 3.xlsx   1.1.111.1.txt
        String filename = file.getOriginalFilename();
        //2、获取文件扩展名
        String extName = filename.substring(filename.lastIndexOf(".") + 1);

        //3、初始化FastDFS工具类
        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:conf/fast_dfs.conf");
            //4、调用客户端工具类，进行上传
            String fileId = fastDFSClient.uploadFile(file.getBytes(), extName);
            String fileUrl=FILE_SERVER_URL+fileId;

            return new Result(true,fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败:"+e.getMessage());
        }
    }
}
