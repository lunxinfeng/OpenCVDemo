package com.izis.yzext.helper

import cn.mina.mina.MinaPushService
import com.izis.yzext.ShotApplication
import com.izis.yzext.ShotApplication.userId
import com.izis.yzext.bean.GameRoom
import com.izis.yzext.bean.GameStep
import com.izis.yzext.isEmpty
import com.izis.yzext.net.*
import com.izis.yzext.postSoap
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PlayChessModelImpl : PlayChessModel {
    override var gameId: Int = 0
    override var letNum: Int = 0
    override var blackName: String = ""
    override var whiteName: String = ""
    override var lastStep: String = ""
    override var allSteps: String = ""
    override var playNum: Int = 0
    override val stepList: MutableList<GameStep> = mutableListOf()
    override var upSuccess: Boolean = false
    override var firstConnect: Boolean = true
    //    override var isChanging: Boolean = false
    override var currNum: Int = 0
    override lateinit var currStep: String
    override var soundWarning: Boolean = false
    var ChessVersion: Int = 0

    override fun createChess(blackName: String, whiteName: String, listener: Observer<String>) {
        postSoap(NetWorkSoap.METHOD_NAME_UPDATE, net_code_createLiveRoom, net_info_createLiveRoom(blackName, whiteName, 0), ShotApplication.userId.toString(), String::class.java)
                .subscribe(listener)
    }

    override fun uploadChess(listener: Observer<String>) {
        ChessVersion++
        postSoap(NetWorkSoap.METHOD_NAME_UPDATE, net_code_uploadChess, net_info_uploadChess(gameId, lastStep, playNum.toString(), allSteps, letNum, ChessVersion), ShotApplication.userId.toString(), String::class.java)
                .subscribe(listener)
    }

    override fun downChess(id: Int, listener: Observer<GameRoom>) {
        postSoap(NetWorkSoap.METHOD_NAME, net_code_downChess, net_info_downChess(id), userId.toString(), GameRoom::class.java)
                .subscribe(listener)
    }

    override fun createSuccess(gameId: Int, blackName: String, whiteName: String, letNum: Int) {
        this.gameId = gameId
        this.letNum = letNum
        this.blackName = blackName
        this.whiteName = whiteName
    }

    override fun downChessSuccess(game: GameRoom) {
        gameId = game.id

        if (!isEmpty(game.f_roomtype)) {
            ChessVersion = Integer.parseInt(game.f_roomtype)
        } else {
            ChessVersion = 0
        }
    }

    override fun clear() {
        lastStep = ""
        allSteps = ""
        playNum = 0
        letNum = 0
        stepList.clear()
    }

    override fun updateInfo(lastStep: String, playNum: Int, allSteps: String) {
        this.playNum = playNum
        this.lastStep = lastStep
        this.allSteps = allSteps
    }


    override fun tileViewHasChanged(step: GameStep) {
        stepList.add(step)
    }

    @Throws(JSONException::class)
    override fun minaRegister(jsonObj: JSONObject): Disposable? {
        if (jsonObj.getString("id") == ShotApplication.userId.toString()) {// 自己连接成功
//            tileView.isConnecting = true
            upSuccess = true
            if (firstConnect) {
                firstConnect = false
                return upChess()
            }
        }
        return null
    }

    @Throws(JSONException::class)
    override fun minaNewChess(jsonObj: JSONObject) {
        val num = Integer.parseInt(jsonObj.getString("f_num"))
        val step = jsonObj.getString("f_position")
        if (num == currNum && step == currStep && stepList.size > 0 && stepList[0].type == 1)
            stepList.removeAt(0)
        // 自己收到自己发送的棋子信息了，说明发送成功
        upSuccess = true
    }

    @Throws(JSONException::class)
    override fun minaChangeRoomType(jsonObj: JSONObject) {
        upSuccess = true
    }

//    override fun minaStopUpload() {
//        isChanging = true
//    }
//
//    override fun minaRestartUpload() {
//        stepList.clear()
//        isChanging = false
//    }

//    fun minaAgain() {
//        tileView.board = Board()
//        board = tileView.board
//        board.setListener(boardListener)
////        downloadChessStep(Net.getGameInfo)
//    }

    override fun minaRegret() {
        if (stepList.size > 0 && stepList[0].type == 2)
            stepList.removeAt(0)
        upSuccess = true
    }

    private fun upChess(): Disposable {
        return Flowable.interval(200, TimeUnit.MILLISECONDS)
                .subscribe {
                    //                    if (stepList.size != 0 && upSuccess && !isChanging) {
                    if (stepList.size != 0 && upSuccess) {
                        upSuccess = false
                        val gameStep = stepList[0]

                        if (gameStep.type == 1) {//上传棋步
                            currNum = gameStep.playNum
                            currStep = gameStep.step

                            val bw = currStep.substring(0, 1)
                            val time_surplus: Long
                            val X: Int
                            if (bw == "+") {
                                time_surplus = 0L
                                X = 1
                            } else {
                                time_surplus = 0L
                                X = 2
                            }
                            // 通过长连接接口上传棋步。
                            MinaPushService.sendMsg(mina_code_newChess, gameId, 1, mina_info_newChess(gameId, currStep, currNum, time_surplus, X, ShotApplication.userId))
                        } else {//悔棋
                            val backNum = gameStep.backNum
                            MinaPushService.sendMsg(mina_code_regret, gameId, 1, mina_info_regret(gameId, backNum, ShotApplication.userId))
                        }
                    }
                }
    }
}