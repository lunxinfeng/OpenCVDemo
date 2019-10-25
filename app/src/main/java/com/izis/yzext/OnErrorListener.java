package com.izis.yzext;

import com.izis.yzext.pl2303.ChessChange;

import java.util.List;

/**
 * 棋盘比对发生错误
 */
public interface OnErrorListener {
    void onError(ChessChange chessChange);
    void onErrorList(List<ChessChange> chessChangeList);
    void onSuccess();
}
