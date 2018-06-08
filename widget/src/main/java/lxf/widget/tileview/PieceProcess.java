/**
 * 
 */
package lxf.widget.tileview;

import java.util.ArrayList;
import java.util.List;

/**
 * 每一步棋的记录
 *
 */
public class PieceProcess {
	public int bw;
	public Coordinate c;
	public List<PieceProcess> removedList;
	
	public int resultBlackCount;
	public int resultWhiteCount;

	public PieceProcess(int bw, Coordinate c,List<PieceProcess> removedList) {
		this.bw = bw;
		this.c = c;
		this.removedList=removedList;
	}
	
	public PieceProcess(int bw, Coordinate c ) {
		this.bw = bw;
		this.c = c;
		this.removedList=new ArrayList<PieceProcess>();
	}
}