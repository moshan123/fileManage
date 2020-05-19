package com.example.filemanage.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

public class FileUtil {
	/**
	 * 传入一个Calendar时间对象和文件名，返回一个新的时间文件名
	 * @param calendar时间对象
	 * @param fileName文件名
	 * @return
	 */
	public static String newFileName(Calendar calendar,String fileName){
		String newName = dateName(calendar)+randomTwoInt()+fileName.substring(fileName.lastIndexOf("."));
		return newName;
	}
	
	/**
	 * 传入一个Calendar时间对象，返回一个String时间名(格式yyyyMMddhhmmss) 
	 * @param calendar时间对象
	 * @return
	 */
	public static String dateName(Calendar calendar){
		String name = calendar.get(Calendar.YEAR)+twoString(calendar.get(Calendar.MONTH)+1)+calendar.get(Calendar.DAY_OF_MONTH)
				+twoString(calendar.get(Calendar.HOUR_OF_DAY))+twoString(calendar.get(Calendar.MINUTE))+twoString(calendar.get(Calendar.SECOND));
		return name;
	}
	
	/**
	 * 传入一个一至两位数，返回一个两位数的String(一位数前面补0返回) 
	 * @param i int数字
	 * @return
	 */
	public static String twoString(int i){
		return i<10?"0"+i:i+"";
	}
	
	/**
	 *  生成随机两位数的String类型
	 * @return 随机生成的两位数
	 */
	public static String randomTwoInt(){
		return String.format("%02d", new Random().nextInt(99));
	}
	
	/**
	 *  传入一个文件名，返回该文件的后缀
	 * @param fileName 文件名
	 * @return
	 */
	public static String suffix(String fileName){
		return fileName.substring(fileName.lastIndexOf(".")+1);
	}
	
	/**
	 * 返回一个UUID(不带"-")
	 * @return
	 */
	public static String newUUID(){
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	/**
	 * 给一个文件大小(b)，返回换算成kb的字符串
	 * @param num
	 * @return
	 */
	public static String size(long num){
		double c=(double)num/1024;
		return String.format("%.1f", c);
	}
	
	/**
	 * 将文件保存到本地磁盘
	 * 
	 * @param in 输入流
	 * @param path 路径
	 * @param name 文件名
	 */
	public static void copyInputStreamToFile(InputStream in, String path, String name) {
		OutputStream out = null;
		try {
			out = new FileOutputStream(new File(path + name));
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = in.read(b)) != -1) {
				out.write(b, 0, len);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//

	/**
	 * 删除文件夹或者文件（只适用于windows）
	 * @param  path 文件路径
	 * @return
	 */
	public static boolean delete(String path){
		File file = new File(path);
		if(!file.exists()){
			return false;
		}
		if(file.isFile()){
			return file.delete();
		}
		File[] files = file.listFiles();
		for (File f : files) {
			if(f.isFile()){
				if(!f.delete()){
					System.out.println(f.getAbsolutePath()+" delete error!");
					return false;
				}
			}else{
				if(!delete(f.getAbsolutePath())){
					return false;
				}
			}
		}
		return file.delete();
	}

	/**
	 * 创建文件夹
	 * @param path 文件夹地址
	 */
	public static void createFolder(String path){
		File file = new File(path);
		//如果路径不存在，新建
		if(!file.exists()&&!file.isDirectory()) {
			file.mkdirs();
		}

	}
}
