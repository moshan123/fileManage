package com.example.filemanage.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.example.filemanage.dao.FileManageDao;
import com.example.filemanage.service.IFileManage;
import com.example.filemanage.util.R;
import com.example.filemanage.util.page.ChangePage;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.util.*;


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
            return R.error(e.getMessage());
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
            return R.error(e.getMessage());
        }
        log.info("更新成功！");
        return  R.ok();

    }

    /**
     * 文件上传
     * @param request
     * @throws Exception
     */
   /* @RequestMapping(value = "/upload")
    public void upload(HttpServletRequest request){
        try {
            ifileManage.upload(request);
        }catch (Exception e){
            log.error("上传文件出错!");
            e.printStackTrace();
        }

    }*/

    /**
     * 文件下载相关代码
     * @param response
     */
    @RequestMapping("/download")
    public void download( HttpServletResponse response){
        try {
            ifileManage.download(response);
        }catch (Exception e){
            e.printStackTrace();
            log.error("下载失败！");
        }

    }


    /**********************************************************写法2***************************************************************/
    /*private static String FILENAME = "";


    //@Value("${xdja.upload.file.path}")
    private String decryptFilePath ="E:\\非遗影像数据库";

   // @Value("${xdja.upload.file.path.temp}")
    private String decryptFilePathTemp="E:\\临时文件夹";

    @GetMapping("/webuploader")
    public String webuploader() {
        return "webupload";
    }*/

    /**
     * 分片上传
     *
     * @return ResponseEntity<Void>
     */
   /* @PostMapping("/upload")
    @ResponseBody
    public R decrypt(HttpServletRequest request, @RequestParam(value = "file", required = false) MultipartFile file,
                     Integer chunks, Integer chunk, String name, String guid)  {
        try{
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                if (file == null) {
                    //throw new ServiceException(ExceptionEnum.PARAMS_VALIDATE_FAIL);
                    throw new RuntimeException("error");
                }
                log.info("guid:" + guid);
                if (chunks == null && chunk == null) {
                    chunk = 0;
                }
                File outFile = new File(decryptFilePathTemp+File.separator+guid, chunk + ".part");
                if ("".equals(FILENAME)) {
                    FILENAME = name;
                }
                InputStream inputStream = file.getInputStream();
                FileUtils.copyInputStreamToFile(inputStream, outFile);
            }
            return R.ok();
        }catch (Exception e){
            log.error("上传失败！");
            e.printStackTrace();
            return R.error(e.getMessage());
        }

    }*/

    /**
     * 合并所有分片
     *
     * @throws Exception Exception
     */
   /* @GetMapping("/merge")
    @ResponseBody
    public R byteMergeAll(String guid)  {
        try{
            log.info("merge:"+guid);
            File file = new File(decryptFilePathTemp+File.separator+guid);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    File partFile = new File(decryptFilePath + File.separator + FILENAME);
                    for (int i = 0; i < files.length; i++) {
                        File s = new File(decryptFilePathTemp+File.separator+guid, i + ".part");
                        FileOutputStream destTempfos = new FileOutputStream(partFile, true);
                        FileUtils.copyFile(s, destTempfos);
                        destTempfos.close();
                    }
                    FileUtils.deleteDirectory(file);
                    FILENAME = "";
                }
            }
        }catch (Exception e){
            log.error("合并失败");
            e.printStackTrace();
            return R.error(e.getMessage());
        }
        return R.ok();

    }*/

    /***************************************************写法3********************************************/
    /**
     * @author van
     * 检查文件存在与否
     */
    @PostMapping("checkFile")
    @ResponseBody
    public Boolean checkFile(@RequestParam(value = "md5File") String md5File, @RequestParam(value = "path") String path) {
        Boolean exist = false;
        File file = new File(path);
        if(file.exists())
           exist = true;
        //实际项目中，这个md5File唯一值，应该保存到数据库或者缓存中，通过判断唯一值存不存在，来判断文件存不存在，这里我就不演示了
		/*if(true) {
			exist = true;
		}*/
        return exist;
    }

    /**
     * @author van
     * 检查分片存在与否
     */
    @PostMapping("checkChunk")
    @ResponseBody
    public Boolean checkChunk(@RequestParam(value = "md5File") String md5File,
                              @RequestParam(value = "chunk") Integer chunk,
                              @RequestParam(value = "path") String path,
                              @RequestParam(value = "total") Integer total) {
        Boolean exist = false;
        path = path + File.separator + md5File ;//分片存放目录
        String chunkName = chunk+ ".tmp";//分片名
        File file = new File(path+File.separator+chunkName);
        // 判断存在的同时应该判断下大小是否一致
        if (file.exists() ) {
            exist = true;
        }
        return exist;
    }

    /**
     * @author van
     * 上传，这里根据文件md5值生成目录，并将分片文件放到该目录下
     */
    @PostMapping("upload")
    @ResponseBody
    public Boolean upload(@RequestParam(value = "file") MultipartFile file,
                          @RequestParam(value = "md5File") String md5File,
                          @RequestParam(value = "path") String path,
                          @RequestParam(value = "chunk",required= false) Integer chunk) { //第几片，从0开始
        path = path + File.separator + md5File + File.separator;
        File dirfile = new File(path);
        if (!dirfile.exists()) {//目录不存在，创建目录
            dirfile.mkdirs();
        }
        String chunkName;
        if(chunk == null) {//表示是小文件，还没有一片
            chunkName = "0.tmp";
        }else {
            chunkName = chunk+ ".tmp";
        }
        String filePath = path+chunkName;
        File savefile = new File(filePath);

        try {
            if (!savefile.exists()) {
                savefile.createNewFile();//文件不存在，则创建
            }
            file.transferTo(savefile);//将文件保存
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * @author van
     * 合成分片
     */
    @PostMapping("merge")
    @ResponseBody
    public Boolean merge(@RequestParam(value = "chunks",required =false) Integer chunks,
                          @RequestParam(value = "md5File") String md5File,
                          @RequestParam(value = "path") String path,
                          @RequestParam(value = "name") String name,
                          @RequestParam Map<String , Object> map) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream(path + File.separator + name);  //合成后的文件
        try {
            byte[] buf = new byte[10*1024];
            for(long i=0;i<chunks;i++) {
                String chunkFile=i+".tmp";
                File file = new File(path+"/"+md5File+"/"+chunkFile);
                InputStream inputStream = new FileInputStream(file);
                int len = 0;
                while((len=inputStream.read(buf))!=-1){
                    fileOutputStream.write(buf,0,len);
                }
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            //合并完，要删除md5目录及临时文件，节省空间。这里代码省略
            File delFile = new File(path+"/"+md5File+"/");
            FileUtils.deleteDirectory(delFile);
            fileOutputStream.close();
            ifileManage.insertFileInfo(map);
        }
        return true;
    }

}
