package lxf.widget.tileview;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


public class GeneralBoard {

	public static int n = 19;
	public static int rom = 19;
	public static int col = 19;
	public static final int None = 0;
	public static final int Black = 1;
	public static final int White = 2;

	public static boolean hasPickother = false;

	// 行棋记录
	private List<GeneralPieceProcess> list = new ArrayList<GeneralPieceProcess>();

	public GeneralGrid currentGrid = new GeneralGrid();// 当前盘面

	private int expBw = Black;// 轮到哪一方下
	private int bwtag =0;//连续颜色0黑1白
	private Function listener;

	// ------------------------------------------------------------------
	public boolean continueput(int x, int y,int bwtag) {
		GeneralCoordinate c = new GeneralCoordinate(x, y);
//		GeneralPieceProcess p = new GeneralPieceProcess(expBw, c);
//		int bwtag =0;
		GeneralPieceProcess p = new GeneralPieceProcess(bwtag, c);

		if (x == 0 && y == 0) // 执行虚步，停一手。
		{
//			list.add(p);
//			finishedPu  t();
//			nochangefinishPut();
		
			
//			return true;
			return false;
		}

		if (currentGrid.putPiece(p)) {
			if (!check(p)) {
				currentGrid.executeGeneralPieceProcess(p, true); // 返回一步
				return false;
			}

			list.add(p);	
			nochangefinishPut();
			return true;
		}
		return false;
	}
	public void setCurBW(int bw) {
		expBw = bw;
	}
	public boolean put(int x, int y) {
		GeneralCoordinate c = new GeneralCoordinate(x, y);
//		GeneralPieceProcess p = new GeneralPieceProcess(expBw, c);
//		int bwtag =0;
		GeneralPieceProcess p = new GeneralPieceProcess(expBw, c);

		if (x == 0 && y == 0) // 执行虚步，停一手。
		{
			list.add(p);
			finishedPut();			
			return true;
//			return false;
		}

		if (currentGrid.putPiece(p)) {
			if (!check(p)) {
				currentGrid.executeGeneralPieceProcess(p, true); // 返回一步
				return false;
			}

			list.add(p);	
			finishedPut();
			return true;
		}
		return false;
	}
	
	// 预落子，检验是否合法
	public boolean prePut(int x, int y) {
		GeneralCoordinate c = new GeneralCoordinate(x, y);
		GeneralPieceProcess p = new GeneralPieceProcess(expBw, c);

		if (currentGrid.putPiece(p)) {
			if (!check(p)) {
				currentGrid.executeGeneralPieceProcess(p, true); // 返回一步
				return false;
			}

			currentGrid.executeGeneralPieceProcess(p, true); // 返回一步
			return true;
		}
		return false;
	}

	private void finishedPut() {
		expBw = Utils.getReBW(expBw);
		postEnvet();
	}
	private void nochangefinishPut(){
//		expBw = Utils.nogetReBW(expBw);
		postEnvet();
	}

	// 打劫检测
	private boolean check(GeneralPieceProcess p) {
//		int i = 0;
//		for (GeneralPieceProcess pp : list) {
//			if (pp.resultBlackCount == p.resultBlackCount
//					&& pp.resultWhiteCount == p.resultWhiteCount) {
//				if (isOverEqualse(i, p)) {
//					return false;
//				}
//			}
//			i++;
//		}
//		return true;
		
		if(list.size()<3) 
			return true;
		if(isOverEqualse(list.size()-2,p)) 
			return false;   // 盘面与上上一手比较，看是否同型
		
		
		return true;
		
	}

	private boolean isOverEqualse(int position, GeneralPieceProcess p) {
		GeneralSubBoard sb = getSubBoard(position + 1);
		return sb.currentGrid.equals(this.currentGrid);
	}

	// ------------------------------------------------------------------rebuilt

	public GeneralSubBoard getSubBoard(int index) {
		GeneralSubBoard board = new GeneralSubBoard(this);
		board.gotoIt(index);
		return board;
	}

	protected void cleanGrid() {
		this.currentGrid = new GeneralGrid();
	}

	protected void addGeneralPieceProcess(GeneralPieceProcess p) {
		currentGrid.executeGeneralPieceProcess(p, false);
		list.add(p);
		finishedPut();
	}

	protected void removeGeneralPieceProcess() {
		if (list.size() == 0)
			return;
		GeneralPieceProcess p = list.remove(getCount() - 1);
		currentGrid.executeGeneralPieceProcess(p, true);
		finishedPut();
	}

	// ------------------------------------------------------------------getter

	public int getValue(int x, int y) {
		return currentGrid.getValue(new GeneralCoordinate(x, y));
	}

	private void postEnvet() {
		if (listener == null)
			return;
		listener.apply(getCount(), expBw, bwtag);
	}

	public void setListener(Function listener) {
		this.listener = listener;
	}

	public GeneralCoordinate getLastPosition() {
		if (getCount() == 0)
			return null;
		return list.get(getCount() - 1).c;
	}

	public int getCount() {
		return list.size();
	}

	public GeneralPieceProcess getGeneralPieceProcess(int i) {
		if (i >= getCount())
			return null;
		return list.get(i);
	}

	public String getCurBW() {
		if (expBw == Black)
			return "+";
		return "-";

	}

	// ------------------------------------------------------------------status

	public Bundle saveState() {
		Bundle map = new Bundle();
		map.putInt("count", getCount());
		int i = 0;
		for (GeneralPieceProcess p : list) {
			map.putInt("x" + i, p.c.x);
			map.putInt("y" + i, p.c.y);
			i++;
		}
		return map;
	}

	public void restoreState(Bundle map) {
		int n = map.getInt("count");
		for (int i = 0; i < n; i++) {
			int x = map.getInt("x" + i);
			int y = map.getInt("y" + i);

			this.put(x, y);
		}
	}
}
