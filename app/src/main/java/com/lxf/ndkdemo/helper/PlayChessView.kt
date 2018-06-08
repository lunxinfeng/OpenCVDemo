package com.lxf.ndkdemo.helper

import android.content.Context
import android.content.Intent
import android.support.v4.util.SparseArrayCompat
import cn.mina.mina.MinaPushService
import com.lxf.ndkdemo.base.TileViewUtils
import com.lxf.ndkdemo.isEmpty
import com.lxf.ndkdemo.net.GoTypeUtil
import com.lxf.ndkdemo.net.mina_code_changeRoomType
import com.lxf.ndkdemo.net.mina_info_changeRoomType
import com.lxf.ndkdemo.pl2303.LogUtils
import com.lxf.ndkdemo.pl2303.Pl2303InterfaceUtilNew
import lxf.widget.tileview.Board
import lxf.widget.tileview.Function
import lxf.widget.tileview.PieceProcess

class PlayChessView(private var pl2303interface: Pl2303InterfaceUtilNew?){
    /**
     * 储存死子列表
     */
    var mSparseArray = SparseArrayCompat<List<PieceProcess>>()

    lateinit var board:Board
    /**
     * 棋盘的listener
     */
    var boardListener: Function = Function { obj ->
        val expBw = obj[1] as Int
        updateInfo(expBw)
    }

    init {
        initTileView()
    }

    fun initTileView() {
        board = Board()
        board.setListener(boardListener)
    }

    /**
     * 更新棋盘状态
     */
    fun updateInfo(expBw: Int) {
        if (board.hasPickStone) {
            if (expBw == Board.Black) {
                pl2303interface?.WriteOpenWhiteLamp("")
            } else if (expBw == Board.White) {
                pl2303interface?.WriteOpenBlackLamp("")
            }
        } else {
            if (expBw == Board.Black) {
                pl2303interface?.WriteOpenBlackLamp("")
            } else if (expBw == Board.White) {
                pl2303interface?.WriteOpenWhiteLamp("")
            }
        }
    }

    fun tileViewError(coor: String) {
        warning()
//        SnackUtil.LongSnackbar(tileView, coor, SnackUtil.Warning).show()
    }

    fun tileViewFinshPick() {
        try {//清理棋盘时有可能发生空指针异常
            if (board.getPieceProcess(board.count - 1).removedList != null && (board.getPieceProcess(board.count - 1).removedList.size != 0)) {
                val list = ArrayList<PieceProcess>()
                list.addAll(board.getPieceProcess(board.count - 1).removedList)
                mSparseArray.put(board.count - 1, list)
                //   board.getPieceProcess(board.count - 1).removedList.clear()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (board.curBW == "+") {
            pl2303interface?.WriteOpenBlackLamp("")
        } else {
            pl2303interface?.WriteOpenWhiteLamp("")
        }
    }

    fun tileViewNormal(theStep: String) {
        if (!isEmpty(theStep))
            TileViewUtils.shownetsgf(null, board, theStep)
    }

    fun tileViewGoBack(backNum: Int) {
        backNStep(backNum)

        if (board.curBW == "+") {
            pl2303interface?.WriteOpenBlackLamp("")
        } else {
            pl2303interface?.WriteOpenWhiteLamp("")
        }
    }

    fun tileViewBackNew(goBackNum: Int, newStep: String) {
        backNStep(goBackNum)
        TileViewUtils.shownetsgf(null, board, newStep)
    }

    fun tileViewLastBack() {
        backNStep(1)

        if (board.curBW == "+") {
            pl2303interface?.WriteOpenBlackLamp("")
        } else {
            pl2303interface?.WriteOpenWhiteLamp("")
        }
    }

    fun warning() {
        pl2303interface?.WriteWarning()
    }

    fun connectServer(gameId: Int,context: Context) {
        if (MinaPushService.isConnected()) {

            MinaPushService.sendMsg(mina_code_changeRoomType, gameId, 0, mina_info_changeRoomType(GoTypeUtil.liveroom))
            // 更合理的做法是收到反馈的指令后再加载其他信息。
        } else {
            // 启动MINA比赛端服务
            val myIntent = Intent(context, MinaPushService::class.java)
            myIntent.putExtra("type", GoTypeUtil.liveroom)
            myIntent.putExtra("gameid", gameId)
            context.startService(myIntent)
        }
    }

    /**
     * 盘面回退N步

     * @param num 回退的步数
     */
    private fun backNStep(num: Int) {
        if (board.count < num) {
            return
        }
        var key: Int
        var value: List<PieceProcess>
        for (i in 0..mSparseArray.size() - 1) {
            key = mSparseArray.keyAt(i)

            if (board.getPieceProcess(key) == null)
                return

            if (board.count - num == key) {//说明悔掉了存在吃子的那颗子
                LogUtils.d("====悔棋====：移除key", key)
                mSparseArray.remove(key)
                board.getPieceProcess(key).removedList.clear()
            } else {
                value = mSparseArray.valueAt(i)
                LogUtils.d("====悔棋====：赋值key", key)
                board.getPieceProcess(key).removedList = value
            }
        }

        val b = board.getSubBoard(board.count - num)
        board = b
        board.setListener(boardListener)
    }

}