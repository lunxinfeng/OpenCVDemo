package lxf.widget.tileview;

import java.util.ArrayList;
import java.util.List;


/**
 * 棋块
 *
 */
public class GeneralBlock {
	private List<GeneralCoordinate> block=new ArrayList<GeneralCoordinate>();
	private int airCount=0;//气数
	private int bw;//颜色
	
	public GeneralBlock(int bw){
		this.bw=bw;
	}
	
	public int getBw(){
		return bw;
	}
	
	public void add(GeneralCoordinate c){
		block.add(c);
	}
	
	public void addAir(int air){
		airCount+=air;
	}
	
	public boolean isLive(){
		if(airCount>0 && block.size()>0)return true;
		return false;
	}
	
	public void each(Function f){
		for(GeneralCoordinate c:block){
			f.apply(c);
		}
	}
}
