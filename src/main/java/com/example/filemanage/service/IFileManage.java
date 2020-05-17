package com.example.filemanage.service;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@Service
public interface IFileManage {

    /**
     * 查询文件夹下的文件
     * @param map
     * @return
     */
    List<Map<String, Object>> queryInfo(Map<String , Object> map);

    /**
     * 查询文件数量
     * @param map
     * @return
     */
    int queryTotal(Map<String , Object> map);

    /**
     * 查询树状图
     * @param pId
     * @return
     */
    List<Map<String, Object>> queryFolderList(String pId);

    /**
     * 批量删除
     * @param map
     */
    void deleteByPrimaryKey(Map<String , Object> map) throws Exception;

    /**
     * 批量新增
     * @param map
     */
    void insertFileInfo(Map<String , Object> map);

    /**
     * 批量修改
     * @param map
     */
    void updateFileInfo(Map<String, Object> map);

    /**
     * 上传文件
     * @param request
     */
    void upload(HttpServletRequest request) throws IOException;

    /**
     * 下载文件
     * @param response
     */
    void download(HttpServletResponse response);
}
