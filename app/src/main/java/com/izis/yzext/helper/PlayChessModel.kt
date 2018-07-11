package com.izis.yzext.helper

import com.izis.yzext.bean.GameRoom
import com.izis.yzext.bean.GameStep
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.json.JSONObject

interface PlayChessModel {
    /**
     * 对局id
     */
    var gameId: Int
    /**
     * 让子数
     */
    var letNum: Int
    /**
     * 黑方姓名
     */
    var blackName: String
    /**
     * 白方姓名
     */
    var whiteName: String
    /**
     * 最后一步
     */
    var lastStep: String
    /**
     * 所有棋步
     */
    var allSteps: String
    /**
     * 手顺
     */
    var playNum: Int
    /**
     * 缓存每一步棋，上传时使用
     */
    val stepList: MutableList<GameStep>
    /**
     * 是否上传成功
     */
    var upSuccess: Boolean
    var firstConnect: Boolean
//    /**
//     * 超级管理员正在操作
//     */
//    var isChanging:Boolean
    /**
     * 正在上传的步数
     */
    var currNum: Int
    /**
     * 正在上传的棋步
     */
    var currStep: String
    /**
     * 电子棋盘是否发声
     */
    var soundWarning: Boolean

    /**
     * 创建对局
     */
    fun createChess(blackName: String, whiteName: String, listener: Observer<String>)

    /**
     * 上传棋局
     */
    fun uploadChess(listener:Observer<String>)

    /**
     * 创建对局成功
     */
    fun createSuccess(gameId: Int, blackName: String, whiteName: String, letNum: Int)

    /**
     * 下载棋局成功
     */
    fun downChessSuccess(game: GameRoom)

    /**
     * 初始时下载棋局
     */
    fun downChess(id:Int,listener:Observer<GameRoom>)

    /**
     * 清理数据
     */
    fun clear()

    /**
     * 更新棋谱信息
     */
    fun updateInfo(lastStep: String, playNum: Int, allSteps: String)

//    /**
//     * 退出前保存数据
//     */
//    fun saveBeforeExit(time:String)
//
//    /**
//     * 清楚本地数据
//     */
//    fun clearLocalData()
//
//    /**
//     * 数子
//     * @param chessArray 棋谱的二维数组
//     */
//    fun count(chessArray: Array<IntArray>, boardSize: Int,listener:Observer<Score>)
//
//    /**
//     * 数子后的数据上传工作
//     */
//    fun countSuccess(score: Score)
//
//    /**
//     * 结束对局，上传分数
//     */
//    fun setScore(sscore:String, oscore:String, strResult:String,listener:Observer<String>)

    /**
     * 电子棋盘发生变化，记录并缓存，方便上传
     */
    fun tileViewHasChanged(step: GameStep)

    fun minaRegister(jsonObj: JSONObject): Disposable?
    fun minaNewChess(jsonObj: JSONObject)
    fun minaChangeRoomType(jsonObj: JSONObject)//更改房间类型
    //    fun minaStopUpload()
//    fun minaRestartUpload()
//    fun minaAgain()
    fun minaRegret()
}