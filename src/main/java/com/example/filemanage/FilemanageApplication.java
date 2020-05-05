package com.example.filemanage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement//开启事务管理
@MapperScan("com.example.filemanage.dao")
@SpringBootApplication
public class FilemanageApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilemanageApplication.class, args);
    }

}
