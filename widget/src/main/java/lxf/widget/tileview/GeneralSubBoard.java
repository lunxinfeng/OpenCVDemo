package lxf.widget.tileview;



public class GeneralSubBoard extends GeneralBoard {
	private GeneralBoard parent;
	private int position = -1;

	public GeneralSubBoard(GeneralBoard parent) {
		super();
		this.parent = parent;
	}

	public void forward() {
		if (position + 1 < parent.getCount()) {
			position++;
			GeneralPieceProcess p = parent.getGeneralPieceProcess(position);
			this.addGeneralPieceProcess(p);
		}
	}

	public void back() {
		if (position < 0)return; 

		this.removeGeneralPieceProcess();
		position--;
	}

	public void gotoIt(int n) {
		if (n > parent.getCount() || n < 0){
			return;
		}
		this.cleanGrid();
		for (int i = 0; i < n; i++) {
			forward();
		}
	}
	@Override
	public boolean put(int x,int y){
		boolean r=super.put(x, y);
		if(r==true){
			position++;
		}
		return r;
	}
}
