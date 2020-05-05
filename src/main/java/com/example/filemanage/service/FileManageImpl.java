package com.example.filemanage.service;

import com.alibaba.fastjson.JSONArray;
import com.example.filemanage.dao.FileManageDao;
import com.example.filemanage.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service(value = "ifileManage")
@Transactional
public class FileManageImpl implements IFileManage {

    @Autowired
    FileManageDao dao;

    @Override
    public List<Map<String, Object>> queryInfo(Map<String , Object> map) {
        return dao.queryInfo(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return dao.queryTotal(map);
    }

    @Override
    public List<Map<String, Object>> queryFolderList(String pId) {
        return dao.queryFolderList(pId);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteByPrimaryKey(Map<String , Object> map) {
        String[] ids = map.get("ids").toString().split(",");
        if("folder".equals(map.get("flag"))){
            dao.deleteChildren(ids.toString());
        }
        dao.deleteByPrimaryKey(ids);

    }

    @Override
    @Transactional(readOnly = false)
    public void insertFileInfo(Map<String , Object> map) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if("folder".equals(map.get("flag"))){
            map.put("id", FileUtil.newUUID());
            map.put("createTime" ,new Date());
            map.put("remarks", "");
            map.put("fileSize","");
            list.add(map);
        } else {
            String rows = (String)map.get("rows");
            JSONArray jsonArray = JSONArray.parseArray(rows);
            Object[] obj = jsonArray.toArray();
            for (int i = 0; i < obj.length; i++){
                Map<String, Object> addRow = (Map<String, Object>)obj[i];
                addRow.put("id", FileUtil.newUUID());
                list.add(addRow);
            }
        }
        dao.insertFileInfo(list);
    }

    @Override
    @Transactional(readOnly = false)
    public void updateFileInfo(Map<String , Object> map) {
        if("folder".equals(map.get("flag"))){

        } else {

        }
        dao.updateFileInfo(map);
    }
}
