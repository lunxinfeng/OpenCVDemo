package lxf.widget.tileview;

import java.util.Arrays;
import java.util.List;

public class GeneralGrid {
	private int[][] a;

	public GeneralGrid() {
		a = new int[GeneralBoard.col][GeneralBoard.rom];
	}

	public int getValue(GeneralCoordinate c) {
		return a[c.x - 1][c.y - 1];
	}

	private void setValue(GeneralCoordinate c, int value) {
		a[c.x - 1][c.y - 1] = value;
	}

	/*
	 * 执行棋子过程 p：行棋记录 reverse：是否反悔行棋
	 */
	public void executeGeneralPieceProcess(GeneralPieceProcess p, boolean reverse) {

		if (p.c.x == 0 && p.c.y == 0) // 停一手时
		{
			return;
		}

		if (!reverse) {
			// 非悔棋，即正常行棋。落子后，对应位置标记为黑子或白子，同时，被迟掉的子置为NONE（空白）。
			setValue(p.c, p.bw);
			for (GeneralPieceProcess pp : p.removedList) {
				setValue(pp.c, Board.None);
			}
		} else {
			// 悔棋。当前子设置为NONE（空白），被踢掉的子恢复状态。
			for (GeneralPieceProcess pp : p.removedList) {
				setValue(pp.c, pp.bw);
			}
			setValue(p.c, Board.None);
		}
	}

	// 落子
	public boolean putPiece(GeneralPieceProcess piece) {

		// 先检查坐标是否有效，无效直接返回false
		if (!piece.c.isValid())
			return false;

		// 检查档期坐标点是否已经有子，如果有子，则落子无效，直接返回false;
		if (getValue(piece.c) != Board.None)
			return false;

		setValue(piece.c, piece.bw);
		startPick(piece.c, piece.bw, piece.removedList);

		// 若果落子后会自杀，则返回false
		if (isSuicide(piece.c, piece.bw)) {
			setValue(piece.c, Board.None); // 还原回来
			return false;
		}

		piece.resultBlackCount = getPieceCount(Board.Black);
		piece.resultWhiteCount = getPieceCount(Board.White);

		return true;
	}

	// 统计盘面棋子数量
	private int getPieceCount(int bw) {
		int c = 0;
		for (int i = 0; i < GeneralBoard.col; i++) {
			for (int j = 0; j < GeneralBoard.rom; j++) {
				if (a[i][j] == bw) {
					c++;
				}
			}
		}
		return c;
	}

	// 判断落子会不会直接杀死自己
	private boolean isSuicide(GeneralCoordinate c, int bw) {
		boolean[][] v = new boolean[GeneralBoard.col][GeneralBoard.rom];
		GeneralBlock GeneralBlock = new GeneralBlock(bw);
		pick(c, v, bw, GeneralBlock);

		return !GeneralBlock.isLive();

	}

	// ------------------------------------------------------------------提子

	private void startPick(GeneralCoordinate c, int bw, List<GeneralPieceProcess> removedList) {
		int reBw = Utils.getReBW(bw);
		pickOther(c, reBw, removedList);

		if (removedList.size() > 0) {
			Board.hasPickother = true;
			return;
		}

		// pickSelf(c, bw, removedList);

	}

	private void pickOther(GeneralCoordinate c, int bw, List<GeneralPieceProcess> removedList) {
		boolean[][] v = new boolean[GeneralBoard.col][GeneralBoard.rom];

		for (int i = 0; i < 4; i++) {
			GeneralCoordinate nc = c.getNear(i);
			GeneralBlock GeneralBlock = new GeneralBlock(bw);
			pick(nc, v, bw, GeneralBlock);

			if (!GeneralBlock.isLive()) {
				deleteGeneralBlock(GeneralBlock, removedList);
			}
		}
	}

	private void pickSelf(GeneralCoordinate c, int bw, List<GeneralPieceProcess> removedList) {
		boolean[][] v = new boolean[GeneralBoard.rom][GeneralBoard.col];
		GeneralBlock GeneralBlock = new GeneralBlock(bw);
		pick(c, v, bw, GeneralBlock);
		if (!GeneralBlock.isLive()) {
			deleteGeneralBlock(GeneralBlock, removedList);
		}
	}

	// 递归构造棋块
	private void pick(GeneralCoordinate c, boolean[][] v, int bw, GeneralBlock GeneralBlock) {
		if (c == null)
			return;
		if (v[c.x - 1][c.y - 1] == true)
			return;

		if (getValue(c) == Board.None) {
			GeneralBlock.addAir(1);
			return;
		} else if (getValue(c) != bw) {
			return;
		}

		v[c.x - 1][c.y - 1] = true;
		GeneralBlock.add(c);

		for (int i = 0; i < 4; i++) {
			GeneralCoordinate nc = c.getNear(i);
			pick(nc, v, bw, GeneralBlock);
		}
	}

	private void deleteGeneralBlock(final GeneralBlock GeneralBlock,
			final List<GeneralPieceProcess> removedList) {
		GeneralBlock.each(new Function() {
			@Override
			public Object apply(Object... obj) {
				GeneralCoordinate c = (GeneralCoordinate) obj[0];

				a[c.x - 1][c.y - 1] = Board.None;
				removedList.add(new GeneralPieceProcess(GeneralBlock.getBw(), c, null));

				return null;
			}
		});
	}

	// ------------------------------------------------------------------

	@Override
	public String toString() {
		String s = "";
		for (int j = 0; j < GeneralBoard.rom; j++) {
			for (int i = 0; i < GeneralBoard.col; i++) {
				if (a[i][j] == Board.None) {
					s += " +";
				} else if (a[i][j] == Board.White) {
					s += " o";
				} else if (a[i][j] == Board.Black) {
					s += " x";
				}
			}
			s += "\n";
		}
		return s;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(a);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeneralGrid other = (GeneralGrid) obj;
		if (!myEqualse(a, other.a, GeneralBoard.col,GeneralBoard.rom))
			return false;
		return true;
	}

	private boolean myEqualse(int[][] a, int b[][], int m,int n) {
		for (int j = 0; j < m; j++) {
			for (int i = 0; i < n; i++) {
				if (a[j][i] != b[j][i])
					return false;
			}
		}
		return true;
	}

	public int[][] getA() {
		return a;
	}
}
