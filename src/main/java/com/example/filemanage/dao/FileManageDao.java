package com.example.filemanage.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.Mapping;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface FileManageDao {
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
    List<Map<String, Object>> queryFolderList(@Param(value = "pId") String pId);

    /**
     * 批量删除
     * @param ids
     */
    void deleteByPrimaryKey(String[] ids);

    /**
     * 删除子集
     * @param pId
     */
    void deleteChildren(@Param(value = "pId") String pId);

    /**
     * 批量新增
     * @param list
     */
    void insertFileInfo(List<Map<String, Object>> list);

    /**
     * 批量修改
     * @param map
     */
    void updateFileInfo(Map<String, Object> map);
}
