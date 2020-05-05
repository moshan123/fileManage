package com.example.filemanage.controller;

import com.example.filemanage.dao.FileManageDao;
import com.example.filemanage.service.IFileManage;
import com.example.filemanage.util.R;
import com.example.filemanage.util.page.ChangePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class FileMangeController  {
    private  static  final Logger log = LoggerFactory.getLogger(FileMangeController.class);

    @Qualifier("ifileManage")
    @Autowired
    IFileManage ifileManage;

    static final Runtime runtime = Runtime.getRuntime();

    @RequestMapping( value = "queryInfo")
    public R queryInfo(@RequestParam Map<String , Object> map){
        ChangePage.change(map);
        List<Map<String, Object>> listInfo = ifileManage.queryInfo(map);
        return R.ok().put("rows", listInfo).put("total",ifileManage.queryTotal(map));

    }
    /**
     * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
     *
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
     * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
     *
     * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130,
     * 192.168.1.100
     *
     * 用户真实IP为： 192.168.1.110
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 树状图专用请求
     * @param map
     * @return
     */
    @RequestMapping( value = "queryFolderList")
    public List<Map<String, Object>> queryFolderList(@RequestParam Map<String , Object> map){
        List<Map<String, Object>> listInfo = ifileManage.queryFolderList(map.get("pId").toString());
        return listInfo;
    }

    /**
     * 打开本地文件
     * @param map
     */
    @RequestMapping( value = "openLocalFile")
    public R openLocalFile(HttpServletRequest request, @RequestParam Map<String , Object> map) throws UnknownHostException {
        String id = getIpAddress(request);
        if("127.0.0.1".equals(id.toString())){
            File file = new File(map.get("filePath").toString());
            if (!file.exists()) {
                return  R.error().put("msg","该盘符或文件不在您的电脑上！");
            }
            Process process = null;
            String cmd = "rundll32 url.dll FileProtocolHandler file://";//要打开的文件路径。
            cmd = cmd + map.get("filePath").toString();
            try {
                process = runtime.exec(cmd);
            } catch (final Exception e) {
                e.printStackTrace();
                log.info("打开文件失败！Error exec!");
                return  R.error().put("msg","该盘符或文件不在您的电脑上！");
            }
        }else{
            return  R.error().put("msg","该盘符或文件不在您的电脑上！");
        }
        return R.ok();

    }


    @RequestMapping( value = "deleteByPrimaryKey")
    public R deleteByPrimaryKey(@RequestParam Map<String , Object> map){
        try {
            ifileManage.deleteByPrimaryKey(map);
        }catch (Exception e){
            log.info("删除失败！");
            e.printStackTrace();
            return R.error();
        }
        log.info("删除成功！");
       return  R.ok();

    }
    @RequestMapping( value = "insertFileInfo")
    public R insertFileInfo(@RequestParam Map<String , Object> map){
        try {
            ifileManage.insertFileInfo(map);
        }catch (Exception e){
            log.info("添加失败！");
            e.printStackTrace();
            return R.error();
        }
        log.info("添加成功！");
        return  R.ok();

    }
    @RequestMapping( value = "updateFileInfo")
    public R  updateFileInfo(@RequestParam Map<String , Object> map){
        try {
            ifileManage.updateFileInfo(map);
        }catch (Exception e){
            log.info("更新失败！");
            e.printStackTrace();
            return R.error();
        }
        log.info("更新成功！");
        return  R.ok();

    }



}
