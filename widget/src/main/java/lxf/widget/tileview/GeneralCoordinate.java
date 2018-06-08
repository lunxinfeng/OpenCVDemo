package lxf.widget.tileview;


public class GeneralCoordinate {
	
	public final int x;
	public final int y;
	public int bw;

	private GeneralCoordinate[] near;
	public static final int up = 0;
	public static final int down = 1;
	public static final int right = 2;
	public static final int left = 3;

	public GeneralCoordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public GeneralCoordinate(int x, int y, int bw) {
		this.x = x;
		this.y = y;
		this.bw =bw;
	}

	private GeneralCoordinate up() {
		GeneralCoordinate c = new GeneralCoordinate(x, y - 1);
		return c.isValid() ? c : null;
	}

	private GeneralCoordinate down() {
		GeneralCoordinate c = new GeneralCoordinate(x, y + 1);
		return c.isValid() ? c : null;
	}

	private GeneralCoordinate right() {
		GeneralCoordinate c = new GeneralCoordinate(x + 1, y);
		return c.isValid() ? c : null;
	}

	private GeneralCoordinate left() {
		GeneralCoordinate c = new GeneralCoordinate(x - 1, y);
		return c.isValid() ? c : null;
	}

	private void initNear() {
		if (near == null) {
			near = new GeneralCoordinate[4];
			near[up] = up();
			near[down] = down();
			near[right] = right();
			near[left] = left();
		}
	}

	public GeneralCoordinate getNear(int direction) {
		initNear();
		return near[direction];
	}

	//------------------------------------------------------------------------
	
	public boolean isValid() {		
		if (x == 0 && y == 0)
			return true;
		
		if (x < 1)
			return false;
		if (y < 1)
			return false;
		if (x > GeneralBoard.col)
			return false;
		if (y > GeneralBoard.rom)
			return false;
		return true;
	}

	public boolean isValid_old() {
		if (x < 0)
			return false;
		if (y < 0)
			return false;
		if (x > GeneralBoard.rom - 1)
			return false;
		if (y > GeneralBoard.col - 1)
			return false;
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeneralCoordinate other = (GeneralCoordinate) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
}
