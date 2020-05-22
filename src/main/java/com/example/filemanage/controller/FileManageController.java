package com.example.filemanage.controller;

import com.example.filemanage.service.IFileManage;
import com.example.filemanage.util.R;
import com.example.filemanage.util.page.ChangePage;
import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public  class FileManageController  {
        private  static  final Logger log = LoggerFactory.getLogger(FileManageController.class);
        public static final int THREAD_COUNT = 3; // 开启的线程的个数
        public static int runningThread = 3;// 记录正在运行的下载文件的线程数

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

        @RequestMapping( value = "isLocal")
        public R isLocal(HttpServletRequest request){
            try{
                String id = getIpAddress(request);
                if("127.0.0.1".equals(id.toString())){
                    return R.ok();
                }else{
                    return R.error();
                }
            }catch (Exception e){
                log.info("获取失败");
                e.printStackTrace();
                return  R.error(e.getMessage());
            }
        }

        /**
         * 打开本地文件
         * @param map
         */
        @RequestMapping( value = "openLocalFile")
        @ResponseBody
        public R openLocalFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String , Object> map) throws IOException {
            String id = getIpAddress(request);
            File file = new File(map.get("filePath").toString());
            if("127.0.0.1".equals(id.toString())){

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
                //if(file.isDirectory()){
                    return  R.error().put("msg","该盘符或文件不在您的电脑上！");
                //}
                /*try {
                    ifileManage.download(response,request,map);
                }catch (Exception e){
                    e.printStackTrace();
                    return  R.error().put("msg","下载失败！");
                }*/
                /*//下载文件
                try{
                        // 服务器返回的数据的长度，实际就是文件的长度
                        int length = (int)file.length();

                        log.info("----文件总长度----" + length);
                        // 在客户端本地创建出来一个大小跟服务器端文件一样大小的临时文件
                        String fileName = map.get("filePath").toString().substring(map.get("filePath").toString().lastIndexOf("\\")+1);
                        RandomAccessFile raf = new RandomAccessFile(fileName, "rwd");
                        // 指定创建的这个文件的长度
                        raf.setLength(length);
                        // 关闭raf
                        raf.close();
                        // 假设是3个线程去下载资源
                        // 平均每一个线程下载的文件的大小
                        int blockSize = length / THREAD_COUNT;
                        for (int threadId = 1; threadId <= THREAD_COUNT; threadId++) {
                            // 第一个线程开始下载的位置
                            int startIndex = (threadId - 1) * blockSize;
                            int endIndex = threadId * blockSize - 1;
                            if (threadId == THREAD_COUNT) {
                                endIndex = length;
                            }
                            log.info("----threadId---" + "--startIndex--"
                                    + startIndex + "--endIndex--" + endIndex);
                            new DownloadThread(map.get("filePath").toString(), threadId, startIndex, endIndex,fileName)
                                    .start();
                        }

                }catch (Exception e){
                    e.printStackTrace();
                    return  R.error().put("msg","文件下载失败！");
                }*/
            }
            return null;

        }

       /* public void test1() {
            try {
                CloseableHttpClient client = null;
                CloseableHttpResponse response = null;
                try {
                    HttpGet httpGet = new HttpGet(uri + "/test1?code=001&name=测试");

                    client = HttpClients.createDefault();
                    response = client.execute(httpGet);
                    HttpEntity entity = response.getEntity();
                    String result = EntityUtils.toString(entity);
                    System.out.println(result);
                } finally {
                    if (response != null) {
                        response.close();
                    }
                    if (client != null) {
                        client.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    */
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
        public void download( HttpServletResponse response, HttpServletRequest request, String filePath){
            try {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("filePath", filePath);
                ifileManage.download(response,request,map);
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

