package com.izis.yzext.helper

import android.graphics.RectF
import android.os.SystemClock
import android.support.v4.util.SparseArrayCompat
import android.view.View
import com.izis.yzext.*
import com.izis.yzext.bean.GameStep
import com.izis.yzext.pl2303.LiveType
import com.izis.yzext.pl2303.LogToFile
import com.izis.yzext.pl2303.LogUtils
import com.izis.yzext.pl2303.Pl2303InterfaceUtilNew
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import lxf.widget.tileview.Board
import lxf.widget.tileview.PieceProcess
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

/**
 * 虚拟棋盘
 * Created by lxf on 18-5-29.
 */
class TileHelper(private var pl2303interface: Pl2303InterfaceUtilNew?,private val game:GameInfo) {
    /**
     * 储存死子列表
     */
    var mSparseArray = SparseArrayCompat<List<PieceProcess>>()
    /***
     * 电子棋盘相关
     */
    var commandhead = arrayOf("~STA", // 命令：主动请求全盘数据，返回~STAstasucceed# 和
            // ~SDA111……#）
            "~SDA", // 命令：收到下位机发送的 全盘棋子信息
            "~SIN", // 命令：收到下位机发送来的 单个棋子信息
            "~CMD", // 命令：
            "~PAB", // 命令：
            "~PAW", // 命令：
            "~REB", // 命令：
            "~REW", // 命令：
            "~RNB", // 命令：
            "~RNW", // 命令：
            "~GIB", // 命令：
            "~GIW", // 命令：
            "~ANA", // 命令：
            "~BKY",//黑方棋钟拍下
            "~WKY"//白方棋钟拍下
    )
    var defaultcommandhead = arrayOf("STA", "SDA", "SIN", "CMD", "PAB", "PAW", "REB", "REW", "RNB", "RNW", "GIB", "GIW", "ANA", "BKY", "WKY")
    // 数据头长度
    var startLength = 4
    // 丢失"~"后数据头长度
    var defaultstartLength = 3
    // 数据尾长度
    var endLength = 1
    /**
     * 旋转角度
     */
    var rotate: Int = 0


    val data: PlayChessModel = PlayChessModelImpl()
    var view: PlayChessView = PlayChessView(pl2303interface)

    fun readData(readdata: String) {
        val data: String
        var command = ""
        var cmdData = ""

        // readdata 必须是以"~"开头或者指令开头（可能存在丢失数据包的情况），正常情况必然是以“~”开头
        if (readdata.endsWith("#") && readdata.lastIndexOf("~") == 0)
        // 一条完整的指令，粘包情况先不考虑
        {
            data = readdata

            for (i in 0 until commandhead.size) {
                if (data.startsWith(commandhead[i])) {
                    command = data.substring(0, startLength)
                    cmdData = data.substring(startLength, data.length - endLength)
                    break
                } else if (data.startsWith(defaultcommandhead[i])) {
                    command = data.substring(0, defaultstartLength)
                    cmdData = data.substring(defaultstartLength, data.length - endLength)
                    break
                }
            }

            LogToFile.d("pl2303指令", command + cmdData)
            LogUtils.d("pl2303指令", command + cmdData)
            // 收到全盘信息
            when(command){
                "SDA","~SDA" ->{
                    // 收到完整的盘面
                    val rotate = if (ScreenUtil.isPortrait(pl2303interface?.mcontext)) 270 else 0
                    val liveType = pl2303interface?.handleReceiveDataRobot(view.board,
                            cmdData, false, rotate)

                    if (liveType != null)
                        receiveTileViewMessage(liveType,cmdData)
                }
                "BKY","WKY","~BKY","~WKY" ->{
                    pl2303interface?.WriteToUARTDevice("~STA#")
//                    pl2303interface?.WriteToUARTDevice("~RGC#")
                }
            }

        }
    }

    fun receiveTileViewMessage(value: LiveType,cmdData:String) {
        if (LiveType.LAST_ERROR != value.type && LiveType.LITTLE_ERROR != value.type) {
            //isSound = !SharedPrefsUtil.getValue(context, "sound", false)
            data.soundWarning = false // 在1秒延迟内，设置成false后，发声子线程将不发声。
        }
        LogToFile.d("pl2303 LiveType", value.toString())
        LogUtils.d("pl2303 LiveType", value.toString())
        when (value.type) {
            LiveType.DA_JIE -> {
                view.warning()

                val rotate = if (ScreenUtil.isPortrait(pl2303interface?.mcontext)) 270 else 0
                LogToFile.w("DA_JIE","打劫:${view.board.toShortString(rotate)}\t:\t${pl2303interface?.reverStr(cmdData)}")
            }
            LiveType.LAST_BACK -> {
//                data.tileViewHasChanged(GameStep(1))
//                view.tileViewLastBack()
            }
            LiveType.LAST_ERROR -> {
                val change = value.chessChange
                view.tileViewError("位置（" + (change.x + 64).toChar() + "，" + change.y + "）发生异常")

                val rotate = if (ScreenUtil.isPortrait(pl2303interface?.mcontext)) 270 else 0
                LogToFile.w("LAST_ERROR","错误$change:${view.board.toShortString(rotate)}\t:\t${pl2303interface?.reverStr(cmdData)}")
            }
            LiveType.LITTLE_ERROR -> {
                view.warning()

                val rotate = if (ScreenUtil.isPortrait(pl2303interface?.mcontext)) 270 else 0
                LogToFile.w("LITTLE_ERROR","错误:${view.board.toShortString(rotate)}\t:\t${pl2303interface?.reverStr(cmdData)}")
            }
            LiveType.LAST_ERROR_MORE, LiveType.LAST_ERROR_MORE_ADD -> {
                view.warning()
                val sb = StringBuilder()
                for (c in value.errorList) {
                    sb.append("(").append((c.x + 64).toChar()).append(",").append(c.y).append(")")
                }
                view.tileViewError("位置" + sb + "发生异常")

                val rotate = if (ScreenUtil.isPortrait(pl2303interface?.mcontext)) 270 else 0
                LogToFile.w("MORE_ERROR","错误$sb:${view.board.toShortString(rotate)}\t:\t${pl2303interface?.reverStr(cmdData)}")
            }
            LiveType.FINISH_PICK -> {
//                view.tileViewFinshPick()
            }
            LiveType.DO_NOTHING -> {
            }
            LiveType.NORMAL -> {
//                putChess(value.allStep)
                if ((game.bw == 1 && value.allStep.startsWith("-"))
                        || (game.bw == 2 && value.allStep.startsWith("+")))
                    return
                //点击屏幕落子
                val index = value.index//返回的数据棋盘白方左手边为1，白方右手边为19，即棋盘的左下角为1，横向右下角为19
                val x: Int
                val y: Int
                if (ScreenUtil.isPortrait(pl2303interface?.mcontext)) {//竖屏
                    //转换为棋盘右下角为1，横向左下角为19
                    x = Board.n - index / Board.n - if (index % Board.n == 0) 0 else 1//0-18
                    y = if (index % Board.n == 0) Board.n - 1 else index % Board.n - 1 //0-18
                } else {//横屏
                    //转换为棋盘右上角为1，竖向右下角为19
                    x = if (index % Board.n == 0) Board.n - 1 else index % Board.n - 1 //0-18
                    y = index / Board.n - if (index % Board.n == 0) 1 else 0 //0-18
                }


                val size = rectF.width() / Board.n

                val xLocation = rectF.left + size * (y + 0.5f)
                val yLocation = rectF.top + size * (x + 0.5f)
                println("点击屏幕落子:index" + index + ";x" + (x + 1) + "/" + xLocation + ";y" + (y + 1) + "/" + yLocation)
                click(xLocation, yLocation)
                if (ServiceActivity.PLATFORM == PLATFORM_XB){//新博需要双击
                    Single.timer(double_click_time,TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .repeat(4)
                            .subscribe { _ -> click(xLocation, yLocation) }

                }
                if (ServiceActivity.PLATFORM == PLATFORM_YK){
                    Single.timer(500,TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { _ -> click(240f, 735f) }
                }

                if (ServiceActivity.PLATFORM == PLATFORM_YC){
                    Single.timer(500,TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { _ -> click(240f, 669f) }
                }
//                if (ServiceActivity.PLATFORM == PLATFORM_JJ){
//                    SystemClock.sleep(500)
//                    click(480f, 367f)
//                }
            }
            LiveType.GO_BACK -> {
//                val backNum = value.backNum
//                data.tileViewHasChanged(GameStep(backNum))
//                view.tileViewGoBack(backNum)
            }
            LiveType.BACK_NEW -> {
//                val goBackNum = value.backNum
//                val newStep = value.backNew
//                data.tileViewHasChanged(GameStep(goBackNum))
//                view.tileViewBackNew(goBackNum, newStep)
            }
            LiveType.NEW_CHESS_2 -> {
//                putChess(value.allStep)
            }
        }
    }

    fun putChess(step: String) {
        view.tileViewNormal(step)
    }

    fun lamb( singleGoCoodinate:String,
              isbremove:Boolean,  rotate:Int){
        pl2303interface?.WritesingleGoCoodinate(singleGoCoodinate,isbremove,rotate)
    }

    private val processBuilder = ProcessBuilder()
    private fun click(x: Float, y: Float) {
        println("触发click")
        val order = arrayOf("input", "tap", "" + x, "" + y)
        try {
            processBuilder.command(*order).start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    var rectF = RectF()
    private var disposable: Disposable? = null

    fun connect(view: View?) {
        disposable = io.reactivex.Observable.timer(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { pl2303interface?.callResume(view) }
                .observeOn(Schedulers.io())
                .flatMap { io.reactivex.Observable.timer(300, TimeUnit.MILLISECONDS) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { pl2303interface?.OpenUARTDevice(view, Board.n) }
    }

    fun updateBoard(a: Array<IntArray>) {
        view.board.currentGrid.a = a
    }

    fun updateCurBW(bw:Int){
        view.board.setCurBW(if (bw == 1) 1 else 2)
    }

    fun isConnected() = pl2303interface?.PL2303MultiLiblinkExist()?:false

    fun disConnect(){
        pl2303interface?.pl2303DisConnect()
    }

    fun onDestroy() {
        pl2303interface?.WriteToUARTDevice("~CAL#")
        pl2303interface?.WriteToUARTDevice("~CTS1#")
        pl2303interface?.WriteToUARTDevice("~BOD19#")
        pl2303interface?.callDestroy()

        disposable?.dispose()
    }
}