package com.izis.yzext.pl2303;

public class ChessChange {
	/**
	 * 一维序列中所在位置
	 */
	private int i;	
	/**
	 * 二维坐标X
	 */
	private int x;
	/**
	 * 二维坐标Y
	 */
	private int y;
	/**
	 * 之前的颜色
	 */
	private int preColor;
	/**
	 * 当前颜色
	 */
	private int NowColor;
	/**
	 * 是否死子位置
	 */
	private boolean isDead;
	/**
	 * 是否有效
	 */
	private boolean isValid;
	/**
	 * 是否最后位置
	 */
	private boolean isLastStep;
	/**
	 * 是否悔棋
	 */
	private boolean isRegret;
	
	private String Step;
	
	public String getStep() {
		return Step;
	}
	public void setStep(String step) {
		Step = step;
	}
	public int getI() {
		return i;
	}
	public void setI(int i) {
		this.i = i;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getPreColor() {
		return preColor;
	}
	public void setPreColor(int preColor) {
		this.preColor = preColor;
	}
	public int getNowColor() {
		return NowColor;
	}
	public void setNowColor(int nowColor) {
		NowColor = nowColor;
	}
	public boolean isDead() {
		return isDead;
	}
	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}
	public boolean isValid() {
		return isValid;
	}
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	public boolean isLastStep() {
		return isLastStep;
	}
	public void setLastStep(boolean isLastStep) {
		this.isLastStep = isLastStep;
	}
	public boolean isRegret() {
		return isRegret;
	}
	public void setRegret(boolean isRegret) {
		this.isRegret = isRegret;
	}
	public ChessChange(int i, int x, int y, int preColor, int nowColor,
			boolean isDead, boolean isValid, boolean isLastStep,
			boolean isRegret) {
		super();
		this.i = i;
		this.x = x;
		this.y = y;
		this.preColor = preColor;
		NowColor = nowColor;
		this.isDead = isDead;
		this.isValid = isValid;
		this.isLastStep = isLastStep;
		this.isRegret = isRegret;
	}
	
	public ChessChange() {
		super();
	}
	
	@Override
	public String toString() {
		return "ChessChange [i=" + i + ", x=" + x + ", y=" + y + ", preColor="
				+ preColor + ", NowColor=" + NowColor + ", isDead=" + isDead
				+ ", isValid=" + isValid + ", isLastStep=" + isLastStep
				+ ", isRegret=" + isRegret + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		ChessChange chessChange=(ChessChange)obj;
		if(this.toString().equals(chessChange.toString())) return true;

		return false;
	}
			
			
}
