package com.example.filemanage.util.page;




import java.util.*;


/**
 * limit ? ? 转换起始位置 和页面大小
 * 
 * @author zhangyanchang
 * 
 */
public  class ChangePage {
	private static final long serialVersionUID = 1L;

	// 当前页码
	private static int page;
	// 每页条数
	private static int rows;

	public static Map<String, Object> change(Map<String, Object> params) {
		page=1;
		if (null != params.get("pageIndex")
				&& !isBlank(params.get("pageIndex").toString())
				&& Integer.parseInt(params.get("pageIndex").toString())!=0) {
			page = Integer.parseInt(params.get("pageIndex").toString());
		}
		if(page==0){
			
		}
		// 每页条数 默认10条
		rows = 10;
		if (null != params.get("pageSize")
				&& !isBlank(params.get("pageSize").toString())) {
			rows = Integer.parseInt(params.get("pageSize").toString());
		}
		params.put("offset", (page - 1) * rows);
		params.put("pageIndex", page);
		params.put("pageSize", rows);
		return params;
	}
	public static boolean isBlank(String str)
	{
		int strLen;
		if ((str == null) || ((strLen = str.length()) == 0)) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}




}
