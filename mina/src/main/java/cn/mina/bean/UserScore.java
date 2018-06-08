package cn.mina.bean;
/**
 * 用户积分的实体
 * @author admin
 *
 */
public class UserScore {
	
	private int userid;
	private int gamelevel;
	private int nowexp;
	private int advexp;
	private int advmatch;
	private int wadv;
	private int cadv;
	private int mygostyle;
	private String mygostylename;
	private int win;
	private int lose;
	private int previous_EXP;
	private String Level;
	private String username;
	private Double Winrate;
	private String sex;
	private String chessname;

	
	public String getSex() {
		return sex;
	}


	public void setSex(String sex) {
		this.sex = sex;
	}


	public String getChessname() {
		return chessname;
	}


	public void setChessname(String chessname) {
		this.chessname = chessname;
	}


	public void setWinrate(Double winrate) {
		Winrate = winrate;
	}


	public String getLevel() {
		return Level;
	}


	public void setLevel(String level) {
		Level = level;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getMygostylename() {
		return mygostylename;
	}


	public void setMygostylename(String mygostylename) {
		this.mygostylename = mygostylename;
	}


	public UserScore() {
		super();
		// TODO 自动生成的构造函数存根
	}
	

	


	public UserScore(int userid, int gamelevel, int nowexp, int advexp,
			int advmatch, int wadv, int cadv, int mygostyle,
			String mygostylename, int win, int lose, int previous_EXP) {
		super();
		this.userid = userid;
		this.gamelevel = gamelevel;
		this.nowexp = nowexp;
		this.advexp = advexp;
		this.advmatch = advmatch;
		this.wadv = wadv;
		this.cadv = cadv;
		this.mygostyle = mygostyle;
		this.mygostylename = mygostylename;
		this.win = win;
		this.lose = lose;
		this.previous_EXP = previous_EXP;
	}


	public UserScore(int userid, int gamelevel, int nowexp, int advexp,
			int advmatch, int wadv, int cadv, int mygostyle,
			String mygostylename, int win, int lose, int previous_EXP,
			String level, String username) {
		super();
		this.userid = userid;
		this.gamelevel = gamelevel;
		this.nowexp = nowexp;
		this.advexp = advexp;
		this.advmatch = advmatch;
		this.wadv = wadv;
		this.cadv = cadv;
		this.mygostyle = mygostyle;
		this.mygostylename = mygostylename;
		this.win = win;
		this.lose = lose;
		this.previous_EXP = previous_EXP;
		Level = level;
		this.username = username;
	}


	public int getPrevious_EXP() {
		return previous_EXP;
	}


	public void setPrevious_EXP(int previous_EXP) {
		this.previous_EXP = previous_EXP;
	}


	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public int getGamelevel() {
		return gamelevel;
	}
	public void setGamelevel(int gamelevel) {
		this.gamelevel = gamelevel;
	}
	public int getNowexp() {
		return nowexp;
	}
	public void setNowexp(int nowexp) {
		this.nowexp = nowexp;
	}
	public int getAdvexp() {
		return advexp;
	}
	public void setAdvexp(int advexp) {
		this.advexp = advexp;
	}
	public int getAdvmatch() {
		return advmatch;
	}
	public void setAdvmatch(int advmatch) {
		this.advmatch = advmatch;
	}
	public int getWadv() {
		return wadv;
	}
	public void setWadv(int wadv) {
		this.wadv = wadv;
	}
	public int getCadv() {
		return cadv;
	}
	public void setCadv(int cadv) {
		this.cadv = cadv;
	}
	public int getMygostyle() {
		return mygostyle;
	}
	public void setMygostyle(int mygostyle) {
		this.mygostyle = mygostyle;
	}
	public int getWin() {
		return win;
	}
	public void setWin(int win) {
		this.win = win;
	}
	public int getLose() {
		return lose;
	}
	public void setLose(int lose) {
		this.lose = lose;
	}
	public int getWinrate() {
		if(win==0&&lose==0){
			return 0;
		}else{
			return ((win/(win+lose))*100);
		}
		
	}

	public String advMessage(){
		String advBoolean = "unknown";
		if(getWadv()>5){
			return "true";
		}
		if(getCadv()>=5&&getWadv()<1){
			return "false";
		}
		if(getCadv()>=6&&getWadv()<2){
			return "false";
		}
		if(getCadv()>=7&&getWadv()<3){
			return "false";
		}
		if(getCadv()>=8&&getWadv()<4){
			return "false";
		}
		if(getCadv()>=9&&getWadv()<5){
			return "false";
		}
		if(getCadv()==10&&getWadv()<6){
			return "false";
		}
		return advBoolean;
	}


	@Override
	public String toString() {
		return "UserScore [userid=" + userid + ", gamelevel=" + gamelevel
				+ ", nowexp=" + nowexp + ", advexp=" + advexp + ", advmatch="
				+ advmatch + ", wadv=" + wadv + ", cadv=" + cadv
				+ ", mygostyle=" + mygostyle + ", mygostylename="
				+ mygostylename + ", win=" + win + ", lose=" + lose
				+ ", previous_EXP=" + previous_EXP + ", Level=" + Level
				+ ", username=" + username + ", Winrate=" + getWinrate() + "]";
	}


}
