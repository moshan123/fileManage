package com.example.filemanage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@SpringBootTest
class FilemanageApplicationTests {

    @Test
    void contextLoads() throws IOException {
        String path = "http://down.360safe.com/yunpan/360wangpan_setup.exe";
        // 1、连接服务器，获取一个文件，获取文件的长度，在本地创建一个大小跟服务器文件大小一样的临时文件
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        int code = conn.getResponseCode();
        int length = conn.getContentLength();//单位是b
        int a =0;
    }

}
