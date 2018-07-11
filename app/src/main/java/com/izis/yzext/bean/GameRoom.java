package com.izis.yzext.bean;

import java.sql.Timestamp;

public class GameRoom {

	private int Id;
	private String f_gamename;
	private String f_roomname;
	private int f_roomnum;
	private String f_roomtype;
	private int f_num;
	private String f_position;
	private String f_allstep;
	private String f_starttime;
	private String f_lastplaytime;
	private int f_blackid;
	private int f_blackleft;
	private int f_whiteid;
	private int f_whiteleft;
	private String f_result;
	private String f_memo;
	private String f_ruler;
	private int f_state;
	private int f_order;
	private int f_matchid;
	private int f_round;
	private String f_revisiontime;
	private int f_degreeofopenness;
	private int f_gametype;
	private int referee;
	private int f_match_state;
	private int initial_time;
	private int set_stop_time;
	private int period;
	private int timing_system;
	private int first_pause;
	private int second_pause;
	private int institutional_time;
	private String blackname;
	private String whitename;
	private String bnickname;
	private String bdanwei;
	private String bgroup;
	private String wnickname;
	private String wdanwei;
	private String wgroup;
	private int isbusy;
	private String nowtime;
	private String status;
	private String bleader;
	private String wleader;
	private Timestamp f_add_time;
	private String f_add_user;
	private String f_add_userid;
	
	private String msglist;
	
	private String bLevel;
	private String wLevel;
	private String bLevelName;
	private String wLevelName;
	
	
	public String getF_add_userid() {
		return f_add_userid;
	}
	public void setF_add_userid(String f_add_userid) {
		this.f_add_userid = f_add_userid;
	}
	public Timestamp getF_add_time() {
		return f_add_time;
	}
	public void setF_add_time(Timestamp f_add_time) {
		this.f_add_time = f_add_time;
	}
	public String getF_add_user() {
		return f_add_user;
	}
	public void setF_add_user(String f_add_user) {
		this.f_add_user = f_add_user;
	}
	public String getBleader() {
		return bleader;
	}
	public void setBleader(String bleader) {
		this.bleader = bleader;
	}
	public String getWleader() {
		return wleader;
	}
	public void setWleader(String wleader) {
		this.wleader = wleader;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public int getFirst_pause() {
		return first_pause;
	}
	public void setFirst_pause(int first_pause) {
		this.first_pause = first_pause;
	}
	public int getSecond_pause() {
		return second_pause;
	}
	public void setSecond_pause(int second_pause) {
		this.second_pause = second_pause;
	}
	public int getInstitutional_time() {
		return institutional_time;
	}
	public void setInstitutional_time(int institutional_time) {
		this.institutional_time = institutional_time;
	}

	
	public String getNowtime() {
		return nowtime;
	}
	public void setNowtime(String nowtime) {
		this.nowtime = nowtime;
	}
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
	public String getF_gamename() {
		return f_gamename;
	}
	public void setF_gamename(String f_gamename) {
		this.f_gamename = f_gamename;
	}
	public String getF_roomname() {
		return f_roomname;
	}
	public void setF_roomname(String f_roomname) {
		this.f_roomname = f_roomname;
	}
	public int getF_roomnum() {
		return f_roomnum;
	}
	public void setF_roomnum(int f_roomnum) {
		this.f_roomnum = f_roomnum;
	}
	public String getF_roomtype() {
		return f_roomtype;
	}
	public void setF_roomtype(String f_roomtype) {
		this.f_roomtype = f_roomtype;
	}
	public int getF_num() {
		return f_num;
	}
	public void setF_num(int f_num) {
		this.f_num = f_num;
	}
	public String getF_position() {
		return f_position;
	}
	public void setF_position(String f_position) {
		this.f_position = f_position;
	}
	public String getF_allstep() {
		return f_allstep;
	}
	public void setF_allstep(String f_allstep) {
		this.f_allstep = f_allstep;
	}
	public String getF_starttime() {
		return f_starttime;
	}
	public void setF_starttime(String f_starttime) {
		this.f_starttime = f_starttime;
	}
	public String getF_lastplaytime() {
		return f_lastplaytime;
	}
	public void setF_lastplaytime(String f_lastplaytime) {
		this.f_lastplaytime = f_lastplaytime;
	}
	public int getF_blackid() {
		return f_blackid;
	}
	public void setF_blackid(int f_blackid) {
		this.f_blackid = f_blackid;
	}
	public int getF_blackleft() {
		return f_blackleft;
	}
	public void setF_blackleft(int f_blackleft) {
		this.f_blackleft = f_blackleft;
	}
	public int getF_whiteid() {
		return f_whiteid;
	}
	public void setF_whiteid(int f_whiteid) {
		this.f_whiteid = f_whiteid;
	}
	public int getF_whiteleft() {
		return f_whiteleft;
	}
	public void setF_whiteleft(int f_whiteleft) {
		this.f_whiteleft = f_whiteleft;
	}
	public String getF_result() {
		return f_result;
	}
	public void setF_result(String f_result) {
		this.f_result = f_result;
	}
	public String getF_memo() {
		return f_memo;
	}
	public void setF_memo(String f_memo) {
		this.f_memo = f_memo;
	}
	public String getF_ruler() {
		return f_ruler;
	}
	public void setF_ruler(String f_ruler) {
		this.f_ruler = f_ruler;
	}
	public int getF_state() {
		return f_state;
	}
	public void setF_state(int f_state) {
		this.f_state = f_state;
	}
	public int getF_order() {
		return f_order;
	}
	public void setF_order(int f_order) {
		this.f_order = f_order;
	}
	public int getF_matchid() {
		return f_matchid;
	}
	public void setF_matchid(int f_matchid) {
		this.f_matchid = f_matchid;
	}
	public int getF_round() {
		return f_round;
	}
	public void setF_round(int f_round) {
		this.f_round = f_round;
	}
	public String getF_revisiontime() {
		return f_revisiontime;
	}
	public void setF_revisiontime(String f_revisiontime) {
		this.f_revisiontime = f_revisiontime;
	}
	public int getF_degreeofopenness() {
		return f_degreeofopenness;
	}
	public void setF_degreeofopenness(int f_degreeofopenness) {
		this.f_degreeofopenness = f_degreeofopenness;
	}
	public int getF_gametype() {
		return f_gametype;
	}
	public void setF_gametype(int f_gametype) {
		this.f_gametype = f_gametype;
	}
	public int getReferee() {
		return referee;
	}
	public void setReferee(int referee) {
		this.referee = referee;
	}
	public int getF_match_state() {
		return f_match_state;
	}
	public void setF_match_state(int f_match_state) {
		this.f_match_state = f_match_state;
	}
	public int getInitial_time() {
		return initial_time;
	}
	public void setInitial_time(int initial_time) {
		this.initial_time = initial_time;
	}
	public int getSet_stop_time() {
		return set_stop_time;
	}
	public void setSet_stop_time(int set_stop_time) {
		this.set_stop_time = set_stop_time;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	public int getTiming_system() {
		return timing_system;
	}
	public void setTiming_system(int timing_system) {
		this.timing_system = timing_system;
	}
	public String getBlackname() {
		return blackname;
	}
	public void setBlackname(String blackname) {
		this.blackname = blackname;
	}
	public String getWhitename() {
		return whitename;
	}
	public void setWhitename(String whitename) {
		this.whitename = whitename;
	}
	public String getBnickname() {
		return bnickname;
	}
	public void setBnickname(String bnickname) {
		this.bnickname = bnickname;
	}
	public String getBdanwei() {
		return bdanwei;
	}
	public void setBdanwei(String bdanwei) {
		this.bdanwei = bdanwei;
	}
	public String getBgroup() {
		return bgroup;
	}
	public void setBgroup(String bgroup) {
		this.bgroup = bgroup;
	}
	public String getWnickname() {
		return wnickname;
	}
	public void setWnickname(String wnickname) {
		this.wnickname = wnickname;
	}
	public String getWdanwei() {
		return wdanwei;
	}
	public void setWdanwei(String wdanwei) {
		this.wdanwei = wdanwei;
	}
	public String getWgroup() {
		return wgroup;
	}
	public void setWgroup(String wgroup) {
		this.wgroup = wgroup;
	}
	public int getIsbusy() {
		return isbusy;
	}
	public void setIsbusy(int isbusy) {
		this.isbusy = isbusy;
	}
	public String getMsglist() {
		return msglist;
	}
	public void setMsglist(String msglist) {
		this.msglist = msglist;
	}
	public String getbLevel() {
		return bLevel;
	}
	public void setbLevel(String bLevel) {
		this.bLevel = bLevel;
	}
	public String getwLevel() {
		return wLevel;
	}
	public void setwLevel(String wLevel) {
		this.wLevel = wLevel;
	}
	public String getbLevelName() {
		return bLevelName;
	}
	public void setbLevelName(String bLevelName) {
		this.bLevelName = bLevelName;
	}
	public String getwLevelName() {
		return wLevelName;
	}
	public void setwLevelName(String wLevelName) {
		this.wLevelName = wLevelName;
	}
	@Override
	public String toString() {
		return "GameRoom [Id=" + Id + ", f_gamename=" + f_gamename
				+ ", f_roomname=" + f_roomname + ", f_roomnum=" + f_roomnum
				+ ", f_roomtype=" + f_roomtype + ", f_num=" + f_num
				+ ", f_position=" + f_position + ", f_allstep=" + f_allstep
				+ ", f_starttime=" + f_starttime + ", f_lastplaytime="
				+ f_lastplaytime + ", f_blackid=" + f_blackid
				+ ", f_blackleft=" + f_blackleft + ", f_whiteid=" + f_whiteid
				+ ", f_whiteleft=" + f_whiteleft + ", f_result=" + f_result
				+ ", f_memo=" + f_memo + ", f_ruler=" + f_ruler + ", f_state="
				+ f_state + ", f_order=" + f_order + ", f_matchid=" + f_matchid
				+ ", f_round=" + f_round + ", f_revisiontime=" + f_revisiontime
				+ ", f_degreeofopenness=" + f_degreeofopenness
				+ ", f_gametype=" + f_gametype + ", referee=" + referee
				+ ", f_match_state=" + f_match_state + ", initial_time="
				+ initial_time + ", set_stop_time=" + set_stop_time
				+ ", period=" + period + ", timing_system=" + timing_system
				+ ", first_pause=" + first_pause + ", second_pause="
				+ second_pause + ", institutional_time=" + institutional_time
				+ ", blackname=" + blackname + ", whitename=" + whitename
				+ ", bnickname=" + bnickname + ", bdanwei=" + bdanwei
				+ ", bgroup=" + bgroup + ", wnickname=" + wnickname
				+ ", wdanwei=" + wdanwei + ", wgroup=" + wgroup + ", isbusy="
				+ isbusy + ", nowtime=" + nowtime + ", status=" + status
				+ ", bleader=" + bleader + ", wleader=" + wleader
				+ ", f_add_time=" + f_add_time + ", f_add_user=" + f_add_user
				+ ", f_add_userid=" + f_add_userid + ", msglist=" + msglist
				+ ", bLevel=" + bLevel + ", wLevel=" + wLevel + ", bLevelName="
				+ bLevelName + ", wLevelName=" + wLevelName + "]";
	}
	
}
