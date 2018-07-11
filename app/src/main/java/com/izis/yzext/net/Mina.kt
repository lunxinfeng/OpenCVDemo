package com.izis.yzext.net


/**
 * 更改房间类型
 */
const val mina_code_changeRoomType = 107
fun mina_info_changeRoomType(type: Int): String = "[{\"room_stype\":\"$type\"}]"

/**
 * 注册成功
 */
const val mina_code_register = 1
/**
 * 最新棋步
 */
const val mina_code_newChess = 3
/**
 * 认输
 */
const val mina_code_giveUp = 4
/**
 * 申请悔棋
 */
const val mina_code_askForRegret = 5
/**
 * 同意悔棋
 */
const val mina_code_agreeRegret = 6
/**
 * 不同意悔棋
 */
const val mina_code_disagreeRegret = 7
/**
 * 电子棋盘停止上传
 */
const val mina_code_stopUpload = 12
/**
 * 电子棋盘恢复上传
 */
const val mina_code_restartUpload = 13
/**
 * 电子棋盘重新下载数据
 */
const val mina_code_again = 14
/**
 * 申请数子
 */
const val mina_code_askForCount = 52
/**
 * 同意数子
 */
const val mina_code_agreeCount = 53
/**
 * 不同意数子
 */
const val mina_code_disagreeCount = 54
/**
 * 悔棋
 */
const val mina_code_regret = 65
/**
 * 进出房间
 */
const val mina_code_inOrOutRoom = 103


fun mina_info_newChess(gameId: Int, lastStep: String, playNum: Int, time_surplus: Long, X: Int, userId: Int): String {
    return "[{\"id\":\"$gameId\",\"f_position\":\"$lastStep"+"\",\"f_num\":\"" + playNum + "\",\"surplus_time\":\""+(time_surplus).toString() + "\",\"X\":\"" + X + "\",\"sendId\":\"" + userId + "\" }]"
}

fun mina_info_askForCount(userId: Int): String {
    return "[{\"yhnm\":\"$userId\"}]"
}

fun mina_info_agreeCount(userId: Int): String {
    return "[{\"yhnm\":\"$userId\"}]"
}

fun mina_info_disagreeCount(userId: Int): String {
    return "[{\"yhnm\":\"$userId\"}]"
}

fun mina_info_askForRegret(gameId: Int, userId: Int): String {
    return "[{\"id\":\"$gameId\",\"yhnm\":\"$userId\"}]"
}

fun mina_info_agreeRegret(gameId: Int, userId: Int): String {
    return "[{\"id\":\"$gameId\",\"yhnm\":\"$userId\"}]"
}

fun mina_info_disagreeRegret(gameId: Int, userId: Int): String {
    return "[{\"id\":\"$gameId\",\"yhnm\":\"$userId\"}]"
}

fun mina_info_giveUp(gameId: Int, userId: Int, winnerup: String, looserdown: String, roomtype: String, winnerEXP: String, looserEXP: String): String {
    return "[{\"id\":\"$gameId\",\"yhnm\":\"$userId\",\"winnerup\":\"$winnerup\",\"looserdown\":\"$looserdown\",\"gamestyle\":\"$roomtype\",\"winnerEXP\":\"$winnerEXP\",\"looserEXP\":\"$looserEXP\"}]"
}

fun mina_info_regret(gameId: Int, backNum: Int, userId: Int): String {
    return "[{\"id\":\"$gameId\",\"backNum\":\"$backNum\",\"sendId\":\"$userId\"}]"
}

fun mina_info_chageRoomType(type: Int): String {
    return "[{\"room_stype\":\"$type\"}]"
}

fun mina_info_inOrOutRoomInfo(gameId: Int,userId: Int, type: String, realName: String): String {
    return "[{\"id\":\"$gameId\",\"userid\":\"$userId\",\"stype\":\"$type\",\"realName\":\"$realName\"}]"
}