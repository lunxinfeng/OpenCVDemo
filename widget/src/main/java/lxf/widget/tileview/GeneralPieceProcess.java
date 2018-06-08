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
public class GeneralPieceProcess {
	public int bw;
	public GeneralCoordinate c;
	public List<GeneralPieceProcess> removedList;
	
	public int resultBlackCount;
	public int resultWhiteCount;

	public GeneralPieceProcess(int bw, GeneralCoordinate c,List<GeneralPieceProcess> removedList) {
		this.bw = bw;
		this.c = c;
		this.removedList=removedList;
	}
	
	public GeneralPieceProcess(int bw, GeneralCoordinate c ) {
		this.bw = bw;
		this.c = c;
		this.removedList=new ArrayList<GeneralPieceProcess>();
	}
}