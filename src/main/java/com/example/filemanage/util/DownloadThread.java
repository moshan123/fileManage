package com.example.filemanage.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载文件的子线程，每一个线程下载对应位置的文件
 *
 * @author loonggg
 *
 */
public  class DownloadThread extends Thread {
    private int threadId;
    private int startIndex;
    private int endIndex;
    private String path;
    private String fileName;
    public static int runningThread = 3;// 记录正在运行的下载文件的线程数
    public static final int THREAD_COUNT = 3;// 开启的线程的个数
    /**
     * @param path
     *            下载文件在服务器上的路径
     * @param threadId
     *            线程id
     * @param startIndex
     *            线程下载的开始位置
     * @param endIndex
     *            线程下载的结束位置
     */
    public DownloadThread(String path, int threadId, int startIndex,
                          int endIndex, String fileName) {
        this.path = path;
        this.threadId = threadId;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.fileName = fileName;
    }
    @Override
    public void run() {
        try {
            //***80行-90行*** 这段代码就是在下载线程中去判断每个线程的记录下载进度的文件是否存在，如果存在则读取里面的进度。并把startIndex的值设置到相应的数据。
            // 检查是否存在记录下载长度的文件，如果存在读取这个文件的数据
            File tempFile = new File(threadId + ".txt");
            if (tempFile.exists() && tempFile.length() > 0) {
                FileInputStream fis = new FileInputStream(tempFile);
                byte[] temp = new byte[1024 * 10];
                int leng = fis.read(temp);
                // 已经下载的长度
                String downloadLen = new String(temp, 0, leng);
                int downloadInt = Integer.parseInt(downloadLen);
                startIndex = downloadInt;
                fis.close();
            }
            File file = new File(path);
            FileInputStream is = new FileInputStream(file);
            //InputStream is = file;// 已经设置了请求的位置，返回的是当前位置对应的文件的输入流
            RandomAccessFile raf = new RandomAccessFile(fileName, "rwd");
            // 随机写文件的时候从哪个位置开始写
            raf.seek(startIndex);// 定位文件
            //***107行-118行*** 这个记录文件的生成就是在下载的文件写入到本地的过程中来标记生成的
            int len = 0;
            byte[] buffer = new byte[1024];
            int total = 0;// 记录已经下载的数据的长度
            while ((len = is.read(buffer)) != -1) {
                RandomAccessFile recordFile = new RandomAccessFile(threadId
                        + ".txt", "rwd");// 记录每个线程的下载进度，为断点续传做标记
                raf.write(buffer, 0, len);
                total += len;
                recordFile.write(String.valueOf(startIndex + total)
                        .getBytes());
                recordFile.close();
            }
            is.close();
            raf.close();
            System.out.println("线程：" + threadId + "下载完毕了！");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            runningThread--;
            if (runningThread == 0) {// 所有的线程已经执行完毕
                for (int i = 1; i <= THREAD_COUNT; i++) {
                    File file = new File(i + ".txt");
                    //删除标记文件的操作
                    file.delete();
                }
            }
        }
    }
}
