package com.izis.yzext.net


/**
 * 连接失败时间
 */
const val CONNECT_TIME_OUT: Long = 8000
/**
 * 服务器基本URL
 */
const val BASE_URL = "http://app.izis.cn/GoWebService/"
const val BASE_URL_SOAP = "http://app.izis.cn/GoWebService/"
//const val BASE_URL_SOAP = "http://192.168.150.191:8080/GoWebService/"
const val DOWNLOAD_URL_5 = "http://app.izis.cn/GoWebService/yzext5_%1\$d.apk"
const val DOWNLOAD_URL_7 = "http://app.izis.cn/GoWebService/yzext7_%1\$d.apk"

/**
 * 获取服务器版本号
 */
const val net_code_getServerCode = "060101"

fun net_info_getServerCode(type: String = "5"): String = "{\"root\":[{\"type\":\"$type\"}]}"

/**
 * 创建直播房间
 */
val net_code_createLiveRoom = "2302_3"

fun net_info_createLiveRoom(blackName: String, whiteName: String, userId: Int): String {
    return "{\"root\":[{\"matchid\":\"0\",\"round\":\"0\",\"blackname\":\"$blackName\",\"whitename\":\"$whiteName\",\"f_gamename\":\"\",\"f_add_user\":\"$userId\"}]}"
}

/**
 * 直播上传棋谱
 */
val net_code_uploadChess = "0504"

fun net_info_uploadChess(id: Int, laststep: String, playnum: String, allstep: String, letNum: Int, ChessVersion: Int) = "{\"root\":[{\"id\":\"$id\",\"f_position\":\"$laststep\",\"f_num\":\"$playnum\",\"f_allstep\":\"$allstep\",\"f_ruler\":\"$letNum\",\"version\":\"$ChessVersion\"}]}"

/**
 * 直播页面下载棋局
 */
val net_code_downChess = "0506"

fun net_info_downChess(id: Int) = "{\"root\":[{\"Id\":\"$id\"}]}"

/**
 * 直播页面结束对局，上传分数
 */
val net_code_liveSetScore = "2305"

fun net_info_liveSetScore(gameId: Int, sscore: String, oscore: String, strResult: String) = "{\"root\":[{\"id\":\"$gameId\",\"sscore\":\"$sscore\",\"oscore\":\"$oscore\",\"result\":\"$strResult\"}]}"

/**
 * 人机对弈，提交对局结果
 */
val robot_result = "0101_1"

fun robotResult(allStep: String, playNum: Int, boardSize: Int, gameName: String, blackName: String, whiteName: String, level: String, result: String, memo: String, stype: Int) = ("{\"root\":[{\"allstep\":\"" + allStep + "\",\"playnum\":\"" + playNum
        + "\",\"f_sz\":\"" + boardSize + "\",\"gamename\":\"" + gameName + "\",\"blackname\":\"" + blackName
        + "\",\"whitename\":\"" + whiteName + "\",\"level\":\"" + level + "\",\"result\":\""
        + result + "\"" + ",\"memo\":\"" + memo + "\",\"stype\":\"" + stype + " \"}]}")