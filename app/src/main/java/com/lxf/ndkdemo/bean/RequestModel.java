package com.lxf.ndkdemo.bean;

/**
 * 请求实体
 */
public class RequestModel {

	private String code;	//方法名
	private String info;	//请求内容主题
	private String snum;	//序列号或动态口令
	private int userid;	//请求中ID
	
	public RequestModel() {

	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getSnum() {
		return snum;
	}

	public void setSnum(String snum) {
		this.snum = snum;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public RequestModel(String code, String info, String snum, int userid) {
		super();
		this.code = code;
		this.info = info;
		this.snum = snum;
		this.userid = userid;
	}

	@Override
	public String toString() {
		return "RequestModel [code=" + code + ", info=" + info + ", snum="
				+ snum + ", userid=" + userid + "]";
	}

	
}
