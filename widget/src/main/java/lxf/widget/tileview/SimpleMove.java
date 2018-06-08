package lxf.widget.tileview;

public class SimpleMove {
	
	private String MoveStr;	// 节点坐标（5位，+0101格式）
	private String Tips;	// 节点注释。
	private String LBs;		// 标签标记。
	
	public String getMoveStr() {
		return MoveStr;
	}
	public void setMoveStr(String moveStr) {
		MoveStr = moveStr;
	}
	public String getTips() {
		return Tips;
	}
	public void setTips(String tips) {
		Tips = tips;
	}
	
	public String getLBs() {
		return LBs;
	}
	public void setLBs(String lBs) {
		LBs = lBs;
	}
	
	public SimpleMove(String moveStr, String tips, String lBs) {
		super();
		MoveStr = moveStr;
		Tips = tips;
		LBs = lBs;
	}
	
	
//	public SimpleMove(String moveStr, String tips) {
//		super();
//		MoveStr = moveStr;
//		Tips = tips;
//	}
//	
	

}
