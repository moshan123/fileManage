package com.example.filemanage.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.example.filemanage.dao.FileManageDao;
import com.example.filemanage.util.FileUtil;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


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
    public void deleteByPrimaryKey(Map<String , Object> map) throws Exception {
        String[] ids = map.get("ids").toString().split(",");
        if("folder".equals(map.get("flag"))){
            dao.deleteChildren(ids.toString());
            //删除物理文件
            File file = new File(map.get("path").toString());
            if(file.exists()){
                FileUtil.delete(map.get("path").toString());
            }
        }else{
            ArrayList<String> paths  =
                    JSON.parseObject((String) map.get("paths"), new TypeReference<ArrayList<String>>(){});
            for (int i =0; i < paths.size(); i++ ){
                //删除物理文件
                File file = new File(paths.get(i));
                if(!file.exists()){
                    throw new Exception("未找到文件！");
                }
                cn.hutool.core.io.FileUtil.del(paths.get(i));
            }
        }

        dao.deleteByPrimaryKey(ids);

    }

    @Override
    @Transactional(readOnly = false)
    public void insertFileInfo(Map<String , Object> map) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if("folder".equals(map.get("flag"))){
            if(map.get("rootNodePath") != null &&  map.get("rootNodePath") != ""){
                String path = map.get("path").toString();
                int index = path.indexOf("\\");
                index = path.indexOf("\\", index+1);
                map.put("path", path.substring(index));
            }
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
                String path = addRow.get("path").toString();
                int index=path.indexOf("\\");
                index=path.indexOf("\\", index+1);
                addRow.put("path",path.substring(index));
                addRow.put("id", FileUtil.newUUID());
                list.add(addRow);
            }
        }
        dao.insertFileInfo(list);
        //创建文件夹
        if("folder".equals(map.get("flag"))){
            File file = new File(map.get("rootNodePath").toString());
            if(!file.exists()  && !file .isDirectory()){
                throw new Exception(map.get("rootNodePath").toString()+"文件夹不存在！");
            }
            File fileChiled = new File(map.get("rootNodePath").toString() + map.get("path").toString());
            if  (fileChiled .exists()  && fileChiled .isDirectory()) {
                throw new Exception(map.get("rootNodePath").toString() + map.get("path").toString()+
                        "当前文件夹已存在！");
            }
                FileUtil.createFolder(map.get("rootNodePath").toString() + map.get("path").toString());


        }
    }

    @Override
    @Transactional(readOnly = false)
    public void updateFileInfo(Map<String , Object> map) {


        if("folder".equals(map.get("flag"))){

        } else {

        }
        dao.updateFileInfo(map);
    }
    public String pictureName=null ;
    public String picturePath=null ;
    @Override
    public void upload(HttpServletRequest request) throws IOException {
            //获取文件需要上传到的路径
            picturePath = request.getParameter("path").toString();
            // 判断存放上传文件的目录是否存在（不存在则创建）
            File dir = new File(picturePath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            StandardMultipartHttpServletRequest req = (StandardMultipartHttpServletRequest) request;
            //获取formdata的值
            Iterator<String> iterator = req.getFileNames();
            while (iterator.hasNext()) {
                MultipartFile file = req.getFile(iterator.next());
                //将文件信息存入数据库中
                pictureName = file.getOriginalFilename();
                File file1 = new File(picturePath + File.separator + pictureName);
                OutputStream out = null;
                try {
                    out = new FileOutputStream(file1);
                    out.write(file.getBytes());
                } finally {
                    out.close();
                }
            }
    }

    @Override
    public void download(HttpServletResponse response) {
        File file = new File(picturePath+pictureName);
        if (pictureName != null) {
            if (file.exists()) {
                response.setContentType("application/force-download");// 设置强制下载不打开
                Date currentTime = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                String dataTime=dateFormat.format(currentTime);
                //文件重新命名
                String pictureNewName = dataTime+pictureName.substring(pictureName.indexOf("."));
                response.addHeader("Content-Disposition",
                        "attachment;fileName=" + pictureNewName);// 设置文件名
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    OutputStream os = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }
                    System.out.println(pictureNewName+"下载成功！！！");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(pictureNewName+"下载失败！！！"+e);
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }







}
