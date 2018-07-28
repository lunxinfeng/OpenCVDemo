package com.izis.yzext.pl2303;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.izis.yzext.MyService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import lxf.widget.tileview.Board;
import lxf.widget.tileview.Coordinate;
import lxf.widget.tileview.PieceProcess;
import lxf.widget.tileview.SgfHelper;
import lxf.widget.util.SharedPrefsUtil;
import tw.com.prolific.pl2303multilib.PL2303MultiLib;


/**
 * 芯片PL2303工具类
 * Created by lxf on 2017/1/4.
 */
public class Pl2303InterfaceUtilNew {


    private static final String ACTION_USB_PERMISSION = "cn.izis.mygo.board.pl2303.Pl2303InterfaceUtilNew.USB_PERMISSION";
    private static final int DeviceIndex = 0;
    public static final int delayTime = 40;         // 50 尝试改成30试试，处理得太慢，缓冲区信息被挤掉？
    private PL2303MultiLib.BaudRate mBaudrate = PL2303MultiLib.BaudRate.B115200;
    private PL2303MultiLib.DataBits mDataBits = PL2303MultiLib.DataBits.D8;
    private PL2303MultiLib.Parity mParity = PL2303MultiLib.Parity.NONE;
    private PL2303MultiLib.StopBits mStopBits = PL2303MultiLib.StopBits.S1;
//    private PL2303MultiLib.FlowControl mFlowControl = PL2303MultiLib.FlowControl.OFF;
    private PL2303MultiLib.FlowControl mFlowControl = PL2303MultiLib.FlowControl.XONXOFF;  // 是否有效？
    private PL2303MultiLib mSerialMulti;
    public Context mcontext;
    private ActivityCallBridge mBridge; // 写的一个回调类
    private byte[] ReadByte = new byte[512]; // 4096
    private int ReadLen;
    private String ReadHub = ""; // 存储接受到的信息，全局变量
    private String curReadData = "";
    private int firstStartCharIndex = -1;
    private String tempRest = "";
    private String TotalCommands = "";
    private int iDeviceCount;
    private IPL2303ConnectSuccess ipl2303ConnectSuccess;

    public Pl2303InterfaceUtilNew() {

    }

    private Pl2303InterfaceUtilNew(Context context) {
        mcontext = context;
    }

    public void setIpl2303ConnectSuccess(IPL2303ConnectSuccess ipl2303ConnectSuccess) {
        this.ipl2303ConnectSuccess = ipl2303ConnectSuccess;
    }

    /**
     * 初始化Pl2303InterfaceUtil
     *
     * @param curBW 当前棋子颜色 传board.getCurBw();
     * @return 初始化Pl2303InterfaceUtil
     */
    public static Pl2303InterfaceUtilNew initInterface(Context context, String curBW, ActivityCallBridge.PL2303Interface pl2303Interface) {
        Pl2303InterfaceUtilNew pl2303interface = new Pl2303InterfaceUtilNew(context);
        pl2303interface.init();
        pl2303interface.mBridge.setOnMethodCallback(pl2303Interface);

        if (curBW.equals("+")) {
            pl2303interface.WriteOpenWhiteLamp("010");
        } else {
            pl2303interface.WriteOpenBlackLamp("010");
        }

        return pl2303interface;
    }

    private void toast(String info){
//        System.out.println(info);
        
        Toast.makeText(mcontext,info,Toast.LENGTH_SHORT).show();
    }

    public void callResume(View view) {
        iDeviceCount = mSerialMulti.PL2303Enumerate();
        if (iDeviceCount == 0) {
//            SnackUtil.ShortSnackbar(view, "没有找到相关设备", SnackUtil.Warning).show();
            toast("没有找到相关设备");
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(mSerialMulti.PLUART_MESSAGE);
        mcontext.registerReceiver(PLMultiLibReceiver, filter);
//        SnackUtil.ShortSnackbar(view, "共有" + iDeviceCount + "台设备连接", SnackUtil.Confirm).show();
        toast("共有" + iDeviceCount + "台设备连接");
    }

    public boolean OpenUARTDevice(final View view, final int boardSize) {
        if (!PL2303MultiLiblinkExist()) {
//            SnackUtil.IndefiniteSnackbar(view, "没有找到相关设备", Color.BLACK, Color.GREEN, Color.RED, Color.YELLOW, "重新连接", new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    callResume(view);
//                    OpenUARTDevice(view, boardSize);
//                }
//            }).show();
            toast("没有找到相关设备");
            return false;
        }
        boolean res = mSerialMulti.PL2303OpenDevByUARTSetting(DeviceIndex, mBaudrate,
                mDataBits, mStopBits, mParity, mFlowControl);
        if (!res) {
//            SnackUtil.IndefiniteSnackbar(view, "Open失败", Color.BLACK, Color.GREEN, Color.RED, Color.YELLOW, "重新连接", new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    callResume(view);
//                    OpenUARTDevice(view, boardSize);
//                }
//            }).show();
            toast("Open失败");
            return false;

        } else {
//            SnackUtil.ShortSnackbar(view, "打开" + mSerialMulti.PL2303getDevicePathByIndex(DeviceIndex) + "成功!", SnackUtil.Confirm).show();
            toast("打开" + mSerialMulti.PL2303getDevicePathByIndex(DeviceIndex) + "成功!");
            switch (boardSize) {
                case 9:
                    WriteToUARTDevice("~BOD09#");
                    break;
                case 13:
                    WriteToUARTDevice("~BOD13#");
                    break;
                case 19:
                    WriteToUARTDevice("~BOD19#");
                    break;
            }
            if (ipl2303ConnectSuccess != null)
                ipl2303ConnectSuccess.openSuccess();
            LogToFile.d("pl2303", "连接设备成功");

            requestData();
            return true;
        }
    }

    private void requestData() {
        Flowable.interval(delayTime, delayTime, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (mSerialMulti != null) {
                            try {
                                ReadLen = mSerialMulti.PL2303Read(DeviceIndex, ReadByte);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (ReadLen > 0) {

                                StringBuffer readdata = new StringBuffer();
                                for (int j = 0; j < ReadLen; j++) {
                                    readdata.append((char) (ReadByte[j] & 0x000000FF));
                                }

                                curReadData = readdata.toString(); // 将获取的棋盘数据转换成字符串

                                //LogUtils.d("===原始数据ReadHub", ReadHub);
                                LogToFile.d("===原始数据ReadHub", ReadHub);
                                LogToFile.d("===本次读取curReadData", curReadData);

                                ReadHub += curReadData; // 得到最完整的读取池数据

                                firstStartCharIndex = ReadHub.indexOf("~");

                                if (firstStartCharIndex > 0) // 总池子不是以“~”开头，有故障。将开头数据摒弃。（-1，0，>0）
                                {
                                    ReadHub = ReadHub.substring(firstStartCharIndex);
                                    LogToFile.d("===不是以~开头，丢弃的数据：",  ReadHub.substring(0,firstStartCharIndex));
                                }

                                //开头必定为~号
                                if (ReadHub.lastIndexOf("#") > 0) {//包含了#号（不包含则说明不包含一个完整的指令）
                                    if (ReadHub.lastIndexOf("#") < ReadHub.length() - 1) {//包含了部分下一条指令
                                        tempRest = ReadHub.substring(ReadHub
                                                .lastIndexOf("#") + 1); // 得到最后的#号之后的半截数据
                                    }

                                    TotalCommands = ReadHub.substring(0,
                                            ReadHub.lastIndexOf("#") + 1); // 完整的指令集合

                                } else // 不包含结束符号，说明指令尚不完整，继续等待下一帧。
                                {
                                    return; // 继续下一次循环
                                }

                                LogUtils.d("===初步处理数据ReadHub", ReadHub);

                                // ==================包含#号
                                // 解析数据，分解成一条条指令后交给前台调用者处理===================
                                if (ReadHub.endsWith("#")
                                        && ReadHub.lastIndexOf("~") == 0) // 一条完整的指令
                                {
                                    mBridge.invokeMethod(ReadHub); // 刚好一条完整指令，则
                                    // 直接通知前台去处理即可

                                } else {
                                    // 尝试获取第一条完整指令
                                    int rp = firstStartCharIndex == -1 ? 0 : ReadHub.lastIndexOf("~");
                                    for (int i = 0; i < TotalCommands.length(); i++) {
                                        if (TotalCommands.charAt(i) == '#') {
                                            if (rp < i + 1)
                                                mBridge.invokeMethod(TotalCommands.substring(rp, i + 1));
                                            rp = i + 1;
                                        }
                                    }
                                }

                                ReadHub = tempRest;
                            }
                        }
                    }
                });
    }

    public void callDestroy() {
        if (mSerialMulti != null) {
            if (iDeviceCount > 0)
                mcontext.unregisterReceiver(PLMultiLibReceiver);
            mSerialMulti.PL2303Release();
            mSerialMulti = null;
        }
    }


    private List<LiveType> liveTypeList=new ArrayList<LiveType>();

    /**
     * 处理收到的全盘棋谱
     *
     * @param board             当前网络盘面
     * @param receiveValidData  当前收到的全盘棋谱
     * @param robot             是否是在人机对弈，而不是记谱直播
     * @param rotate            数据旋转角度，真实棋盘与程序呈现的棋盘存在角度问题。
     * @return 最新棋步或者其他提示信息
     */
    public LiveType handleReceiveDataRobot(Board board, String receiveValidData,
                                           boolean robot, int rotate) {

        LiveType liveType = new LiveType();

        liveType=handleReceiveOneFrameDataRobot(board,receiveValidData,robot,rotate);

        // 尝试再次校对数据。方法：保存当前数据帧，等待下一帧时处理。13888413513
        if(liveType.getType().equals(LiveType.LAST_ERROR_MORE_ADD))
        {

            LogToFile.d("尝试进行二次比对，当前帧信息：", liveType.toString());
            // 看是否有前一帧数据。有则与前一帧数据比对，看能否找出规律，得到有效的两步棋。
            LiveType liveType_deepin = getMayValidStep(board,liveType);

            if(liveType_deepin!=null) return liveType_deepin;


            // 如果错误太多，忽略次序，呈现棋谱，让记谱继续进行。【有问题的次序，将来考虑交给用户自行校准】
            liveType_deepin=getMayValidStep2(board,liveType);

            if(liveType_deepin!=null) return liveType_deepin;

        }

        liveTypeList.add(liveType);

        return liveType;

    }


    /**
     * 处理收到的全盘棋谱
     *
     * @param board            当前网络盘面
     * @param receiveValidData 当前收到的全盘棋谱
     * @return 最新棋步或者其他提示信息
     */
    public LiveType handleReceiveOneFrameDataRobot(Board board, String receiveValidData,
                                           boolean robot, int rotate) {
        /*
         * 第一步：准备工作。 （1）得到当前实际要提的死子列表。大部分情况下没有。 （2）如果receiveValidData 和
		 * preReceiveData一致，则直接返回 donothing
		 *
		 * 第二步：比对 (1) receiveValidData 和 preReceiveData
		 * 比对。找出不同点集合ChangeList.，集合中的条目包含
		 * （i,x,y,Num,preColor,NowColor,====》||isDead
		 * ,isValid,isLastStep,isRegret)
		 *
		 *
		 * (2) 如果ChangeList的长度=1，则判断 1）该点当前颜色为0，判断当前点是否是死子
		 * ，如果是，则设置isDead=true，isValid=true，isLastStep=false,isRegret=false；
		 * 如果不是
		 * ，非死子突然消失，则判断该点是否是最后位置点。如果是则表示是悔棋（isDead=false，isValid=true，isLastStep
		 * =true，isRegret=true）
		 * 如果不是最后位置，则表示中间的子无故消失，悔棋一步是不可取的，只能给出提示（重新检验），记录下此异常。
		 *
		 * 2）该点颜色为1或者2.判断当前轮到黑还是白下棋，如果对应，继续判断 如果死子尚未提完，则新的落子不予处理，提示要先提完盘面的死子， 否则
		 * 设置值isDead=false，isValid=true，isLastStep=false,isRegret=false，返回该点。
		 * 如果黑白不匹配，则返回异常。记录下此异常。
		 *
		 *
		 * （2） 如果是两个变化。
		 *
		 *    2.1  两个子一黑一白，当做有效的落子。
		 *    2.2  一个增加，一个消失，判断是否是落子提子 或者 提子落子。
		 *    2.3  两个黑或两个白，直接报错，当做异常。丢弃。【】
		 *
		 * （3）如果ChangeList的长度大于2，则判断 1）是否有死子，如果有则继续判断，如果没有则返回 异常 不处理 2）
		 * 如果所有异动点当前颜色都是0，且在死子集合中，则认为是正常的提子动作，不予太多处理。否则提示有异常。
		 *
		 *
		 *
		 * 第三步：返回结果，显示结果。
		 */

        // 第一步：准备工作
        // 1.1
        // 准确的网络盘面

        LiveType liveType = new LiveType();
        receiveValidData = reverStr(receiveValidData);

        String preReceiveData = board.toShortString(rotate);

        LogToFile.d("pl2303当前收到信息", receiveValidData);
        LogToFile.d("pl2303盘面展示信息", preReceiveData);

        // 1.1
        List<PieceProcess> deadList = getLastDeadList(board);

        // 1.2
        if (receiveValidData.equals(preReceiveData)) {
            if (deadList.size() == 0 && robot) {//如果轮到小智下，且无死子，则返回ROBOT_CHESS
                liveType.setType(LiveType.ROBOT_CHESS);
                return liveType;
            } else if (isFinishPickStone(deadList, receiveValidData, rotate)) {  // 此判断似乎必然。
                liveType.setType(LiveType.FINISH_PICK);
                return liveType;
            } else {
                liveType.setType(LiveType.DO_NOTHING);
                return liveType; // 可提示继续提子吧
            }
        }


        // 第二步：比对
        // 2.1 找出差异点。
        List<ChessChange> changeList = getDifferent(receiveValidData, preReceiveData, rotate);


        if (changeList == null) {//数据发生异常，已重新请求数据，前台不做任何操作
            liveType.setType(LiveType.DO_NOTHING);
            return liveType;
        }

        LogUtils.d("===========不同点个数===", changeList.size());

        int key = -1;
        List<PieceProcess> value = null;
        if (mcontext instanceof MyService) {
            MyService robotActivity = (MyService) mcontext;
            int size = robotActivity.getTileHelper().getMSparseArray().size();
            if (size > 0) {
                key = robotActivity.getTileHelper().getMSparseArray().keyAt(size - 1);
                value = robotActivity.getTileHelper().getMSparseArray().valueAt(size - 1);
            }
        }

        //处理状态3（abc标记）
        boolean isThree = false;
        for (ChessChange chessChange : changeList) {
            if ((chessChange.getPreColor() == 3 && chessChange.getNowColor() == 0)
                    || (chessChange.getPreColor() == 0 && chessChange.getNowColor() == 3)) {
                isThree = true;
                break;
            }
        }

        if (isThree) {
            if (changeList.size() == 1) {//落子或提子

                ChessChange change = changeList.get(0);
                if (change.getNowColor() == 0) {// 当前子消失。
                    liveType.setChessChange(change);
                    liveType.setType(LiveType.REMOVE_CHESS_3);
                } else {
                    liveType.setChessChange(change);
                    liveType.setType(LiveType.PUT_CHESS_3);
                }

            } else {//移动特殊棋子

                liveType.setType(LiveType.BACK_NEW_3);
            }

            return liveType;
        }

        LogToFile.d("pl2303不同点", Arrays.toString(changeList.toArray()));
        LogToFile.d("pl2303死子列表", Arrays.toString(deadList.toArray()));

        //非3状态的处理
//        if (changeList.size() == 1 && (deadList.size() <= 1 || key == board.getCount() - 1)) // 提多个子后落的第一颗子
        if (changeList.size() == 1) // 提多个子后落的第一颗子
        {
            LogToFile.d("pl2303", "进入第一层判断：只有一个点发生变化");
            ChessChange change = changeList.get(0);

            if (change.getNowColor() == 0) // 当前点子消失。
            {
                if (isDeadPiece(change, deadList)) // 死子被提掉，正常的举动。
                {
                    // deadStonesNum--;
                    // 【完善】根据是否完全提完子来触发切换灯，如果有提子，则不立刻切换灯，让灯变色，表示提子中。所有子提完，再切换灯
                    // change.setDead(true);
                    if (isFinishPickStone(deadList, receiveValidData, rotate)) {
                        liveType.setType(LiveType.FINISH_PICK);
                        return liveType;
                    } else {
                        liveType.setType(LiveType.DO_NOTHING);
                        return liveType; // 可提示继续提子吧
                    }
                } else // 非死子被提掉了。
                {
                    if (isLastPiece(change, board)) // 是最后一步棋消失，当做悔棋处理
                    {
                        liveType.setType(LiveType.LAST_BACK);
                        return liveType; // 可提示继续提子吧

                    } else // 中间的子消失一颗，异常情况，需稍后给出提示。
                    {
                        liveType.setType(LiveType.LAST_ERROR);
                        liveType.setChessChange(change);
                        return liveType;
                    }
                }

            } else { // 黑子或白子
                //打劫检测
                LogToFile.d("pl2303", "进入第一层判断：新增1颗子");
                if (key == board.getCount() - 1
                        && value != null && value.size() == 1
                        && value.get(0).c.x == change.getX()
                        && value.get(0).c.y == change.getY()) {
//                    if (!board.check(value.get(0))) {   // 此检测方法似乎有问题

                    LogToFile.d("pl2303", "打劫检测：新增点坐标:"+change.getX()+",y:"+change.getY());


//                    if (mcontext instanceof BasePl2303Activity) {
//                        BasePl2303Activity robotActivity = (BasePl2303Activity) mcontext;
//                        int size = robotActivity.getMSparseArray().size();
//
//                        int keys;
//                        List<PieceProcess> values;
//
//                        for(int i=0;i<size;i++)
//                        {
//                            keys=robotActivity.getMSparseArray().keyAt(i);
//                            values=robotActivity.getMSparseArray().valueAt(i);
//                            board.getPieceProcess(keys).removedList = values;
//
//                        }
//                    }

                    if (!board.prePut(change.getX(),change.getY())) {
                        LogToFile.d("pl2303", "打劫检测结果：禁入点");
                        liveType.setType(LiveType.DA_JIE);
                        return liveType;
                    }
                }

                String userBW = board.getCurBW(); // 当前轮到黑或者白下。

                if (userBW.equals("+")) { // 轮到黑下
                    if (change.getNowColor() == 1 && change.getPreColor() == 0) { // 新增的是黑子

                        if (isFinishPickStone(change, deadList,
                                receiveValidData, rotate)) {
                            liveType.setIndex(change.getI()+1);
                            liveType.setType(LiveType.NORMAL);
                            liveType.setAllStep("+" + change.getStep());
                            return liveType; // 返回该棋步
                        } else {
                            liveType.setType(LiveType.DO_NOTHING);
                            return liveType; // 可提示继续提子吧
                        }

                    } else { // 轮到黑下，但是落的是白子，可能是底层误判。
                        liveType.setType(LiveType.LITTLE_ERROR);
                        liveType.setChessChange(change);
                        return liveType;
                    }
                } else {
                    if (change.getNowColor() == 2 && change.getPreColor() == 0) {
                        if (isFinishPickStone(change, deadList,
                                receiveValidData, rotate)) {
                            liveType.setIndex(change.getI()+1);
                            liveType.setType(LiveType.NORMAL);
                            liveType.setAllStep("-" + change.getStep());
                            return liveType; // 返回该棋步
                        } else {
                            liveType.setType(LiveType.DO_NOTHING);
                            return liveType; // 可提示继续提子吧
                        }
                    } else { // 轮到白下，但是落的是黑子，可能是底层误判。
                        liveType.setType(LiveType.LITTLE_ERROR);
                        liveType.setChessChange(change);
                        return liveType;
                    }
                }

            }

        } else if (changeList.size() == 2 && isAllAdd(changeList)) {//新增两颗子
            LogToFile.d("pl2303", "进入第二层判断：新增两颗子");
            ChessChange change1 = changeList.get(0);
            ChessChange change2 = changeList.get(1);
            if (change1.getNowColor() == change2.getNowColor()) { // 同色子，还有一种可能，是死子。

                if(isAllDead(changeList,deadList)){
                    liveType.setType(LiveType.DO_NOTHING);   // 可能是在提死子
                    return liveType;
                }else
                {
                    liveType.setErrorList(changeList);
                    liveType.setType(LiveType.LAST_ERROR_MORE_ADD);
                    return liveType;
                }



            }
            String allStep;
            String userBW = board.getCurBW(); // 当前轮到黑或者白下。
            if (userBW.equals("+")) {// 轮到黑下
                if (change1.getNowColor() == 1) {
                    allStep = "+" + change1.getStep() + "-" + change2.getStep();
                } else {
                    allStep = "+" + change2.getStep() + "-" + change1.getStep();
                }
            } else {
                if (change1.getNowColor() == 1) {
                    allStep = "-" + change2.getStep() + "+" + change1.getStep();
                } else {
                    allStep = "-" + change1.getStep() + "+" + change2.getStep();
                }
            }
            liveType.setAllStep(allStep);
            liveType.setType(LiveType.NEW_CHESS_2);
            return liveType;

        } else if (changeList.size() == 2 && isPickAndChess(changeList, board, deadList)) {//消失一子，新增一子， 并且消失的是死子
            LogToFile.d("pl2303", "进入第三层判断：//消失一子，新增一子， 并且消失的是死子");
            ChessChange change1 = changeList.get(0);
            ChessChange change2 = changeList.get(1);
            ChessChange dismiss;//消失的子
            ChessChange add;//新增的子
            if (change1.getNowColor() == 0) {
                dismiss = change1;
                add = change2;
            } else {
                dismiss = change2;
                add = change1;
            }
            liveType.setIndex(add.getI()+1);
            liveType.setType(LiveType.NORMAL);
            liveType.setAllStep(board.getCurBW() + add.getStep());
            return liveType; // 返回该棋步

        } else { // 有多处变化（包含了两处变化，非上面考虑的问题之外的情况）。
            LogToFile.d("pl2303", "进入第四层判断：多处变化");
            if (isAllDisappearRobot(changeList)) // 是否是批量消失
            {
                // 如果消失的都是该提的死子，则不予处理，正在提着死子。
                if (isAllDead(changeList, deadList)) {
                    LogToFile.d("pl2303", "进入第四层判断：批量消失，提死子");
                    // 【完善】：死子提完后，需切换灯，切换计时器（如果有）。
                    if (isFinishPickStone(deadList, receiveValidData, rotate)) {
                        liveType.setType(LiveType.FINISH_PICK);
                        return liveType;
                    } else {
                        liveType.setType(LiveType.DO_NOTHING);
                        return liveType; // 可提示继续提子吧
                    }
                } else {
                    // 判断消失的次序是否合理。合理定义：末尾的棋步，可形成序列。
                    if (isValidDisappear(board, changeList)) {
                        LogToFile.d("pl2303", "进入第四层判断：批量消失，末尾棋步");
                        liveType.setType(LiveType.GO_BACK);
                        liveType.setBackNum(changeList.size());
                        return liveType;

                    } else {

                        LogToFile.d("pl2303", "进入第四层判断：无法处理的多处消失？");
                        liveType.setErrorList(changeList);
                        liveType.setType(LiveType.LAST_ERROR_MORE);
                        return liveType;
                    }

                }

            } else {

                // 有死子的情况下，未提子就悔棋 （最后一步消失，同时前面多出N步，且这N步是最后一步的死子）。
                // 提子逻辑一旦执行，标准盘面变成了最后落子+多个被提子位置为空白位置。此时悔棋，比对出来的就是最后
                // 一个子消失，多出N个子，这N个子是最后这一步的死子。
                if (isBackWhenPick(changeList, board)) {
                    liveType.setType(LiveType.LAST_BACK);
                    return liveType;
                }

                // 两种情况：（1）新增一个子，多个死子消失。提子速度超快情况下 （2）新增2子，消失1子。消失的是最后1步棋，相当于拖子了。悔棋+落子+落子。
                LiveType templiveType = isPickMoreAndChess(changeList,board,deadList);
                if(templiveType!=null) return templiveType;


                // 一个子新增或改变，多个子消失，消失的子为最后面的棋步。可能是悔棋多步落子【应该要兼容，悔1步，落1步拖子情况】。
                String[] aStr = doWithLoseAndAlter(board, changeList,deadList);
                if (aStr[0].equals("")) {
                    liveType.setErrorList(changeList);
                    liveType.setType(LiveType.LAST_ERROR_MORE_ADD);
                    return liveType;
                }else if(aStr[0].equalsIgnoreCase("wait-pick")) {
                    liveType.setType(LiveType.DO_NOTHING);
                    return liveType;
                }
                    else
                {
                    liveType.setType(LiveType.BACK_NEW);
                    liveType.setBackNum(Integer.parseInt(aStr[0]));
                    liveType.setBackNew(aStr[1]);
                    return liveType;
                }

            }
        }
    }

    /**
     * 是否全部是增加子
     */
    private boolean isAllAdd(List<ChessChange> changeList) {
        for (ChessChange change : changeList) {
            if (change.getNowColor() == 0 || change.getPreColor() != 0)
                return false;
        }
        return true;
    }

    /**
     * 提子时操作过快，一帧俩数据,消失一子新增一子,且消失的是死子
     * 1.死子只有一颗，已经是死子，和后一颗子的数据同时发出
     * 2.死子只有一颗，下完该子后才是死子，和前一颗子的数据同时发出
     * <p>
     * todo
     * 3.死子有多颗，已经是死子，提子(没提完)的同时下了一颗子（也许是误判），
     * 4.死子有多颗，下完该子后才是死子，下子的同时提了一颗子,此时死子没提完，
     */
    private boolean isPickAndChess(List<ChessChange> changeList, Board board, List<PieceProcess> deadList) {
        if (changeList.size() != 2)
            return false;
        LogToFile.d("pl2303", "正在进行是否是提子落子过快的判断");
        ChessChange change1 = changeList.get(0);
        ChessChange change2 = changeList.get(1);
        if ((change1.getNowColor() == 0 && change2.getNowColor() != 0 && change2.getPreColor() == 0)
                || (change2.getNowColor() == 0 && change1.getNowColor() != 0 && change1.getPreColor() == 0)) {//消失一子新增一子
            ChessChange dismiss;//消失的子
            ChessChange add;//新增的子

            if (change1.getNowColor() == 0) {
                dismiss = change1;
                add = change2;
            } else {
                dismiss = change2;
                add = change1;
            }
            LogToFile.d("pl2303", "dismiss:" + dismiss);
            LogToFile.d("pl2303", "add:" + add);
            return isPreDeadPiece(dismiss, add, board) || isDeadPiece(dismiss, deadList);
        } else {
            return false;
        }
    }


    /**
     * 多个变化，有新增也有消失，如果消失的是全是死子，则认为有效
     */
    private LiveType isPickMoreAndChess(List<ChessChange> changeList, Board board, List<PieceProcess> deadList) {


        LogToFile.d("pl2303", "第五层判断：多个增加+多个消失的情况");

        LiveType liveType=new LiveType();

        List<ChessChange> addList=new ArrayList<ChessChange>();
        List<ChessChange> dismissList=new ArrayList<ChessChange>();


        for(int i=0;i<changeList.size();i++)
        {
            if(changeList.get(i).getNowColor()!=0 && changeList.get(i).getPreColor()==0)
            {
                addList.add(changeList.get(i));
            }else  if(changeList.get(i).getPreColor()!=0 && changeList.get(i).getNowColor()==0)
            {
                dismissList.add(changeList.get(i));
            }
        }

        if(addList.size()>2) return  null;      // 如果同时多个新增,子还没提完就去下子，不大应该。但不排除。

        if(addList.size()==1)       // 第一种情况：新增一个子，提掉N个子
        {
            ChessChange  addChess=addList.get(0);

            //新增的颜色与当前需要的不匹配，
            if((addChess.getNowColor()==2 && board.getCurBW().equals("+")) || (addChess.getNowColor()==1 && board.getCurBW().equals("-")))
            {
                return  null;
            }

            if(dismissList.size()==0) return  null;  // 前面判断的漏网之鱼

            if(isAllDead(dismissList,deadList) || isPreDeadPieceList(dismissList,addChess,board))
            {
                liveType.setIndex(addChess.getI()+1);
                liveType.setType(LiveType.NORMAL);
                liveType.setAllStep(board.getCurBW() + addChess.getStep());
                return liveType; // 返回该棋步

            }

            // 其他情况，无法处理（尾部棋谱消失+1子新增的在另外的案例中处理了）。



        }else if(addList.size()==2){  // 第2中情况：新增两个子，提掉N个子

            // 新增的两个子必须一黑一白，才有可能有效。
            if(addList.get(0).getNowColor()==addList.get(1).getNowColor())
            {
                return  null;
            }

            ChessChange bChange=new ChessChange();
            ChessChange wChange=new ChessChange();
            ChessChange firstChange=new ChessChange();
            ChessChange secondChange=new ChessChange();

            if(addList.get(0).getNowColor()==1)
            {
                bChange=addList.get(0);
                wChange=addList.get(1);
            }else
            {
                bChange=addList.get(1);
                wChange=addList.get(0);
            }

            String userBW=board.getCurBW();
            if(userBW.equals("+"))
            {
                firstChange=bChange;
                secondChange=wChange;

            }else {
                firstChange=wChange;
                secondChange=bChange;
            }

            String allStep;

            // (1)消失的子是原棋谱最后一步的死子。（2）消失的子是将落下第1颗子的死子。
            // 2018-2-27 新增 还有两种情况，（3）消失的子是第2颗新增子的死子。（4） 消失的子是第1颗新增子的死子和第3颗新增子的死子，死子颜色不一样。
            if(isAllDead(dismissList,deadList)
                    || isPreDeadPieceList(dismissList,firstChange,board)
                    || isPreDeadPieceList2(dismissList,firstChange,secondChange,board)
                    || isPreTwoStepDeadPieceList(dismissList,firstChange,secondChange,board))
            {

                if (userBW.equals("+")) {// 轮到黑下
                    allStep = "+" + bChange.getStep() + "-" + wChange.getStep();
                } else {
                    allStep = "-" + wChange.getStep() + "+" + bChange.getStep();
                }
                liveType.setAllStep(allStep);
                liveType.setType(LiveType.NEW_CHESS_2);
                return liveType;    // 返回该棋步，两步棋。

            }

            // 还有一种情况：最后1步消失，新增两步。【场景：最后1步是误判（拖子），纠正确认了位置的同时，对手也落子】
            // 这样就造成两个新增，一个消失的假象。

            if(dismissList.size()==1 &&  isLastPiece(dismissList.get(0), board)) // 是最后一步棋消失
            {
                if(userBW.equals("-"))
                {
                    allStep = "+" + bChange.getStep() + "-" + wChange.getStep();

                }else
                {
                    allStep = "-" + wChange.getStep() + "+" + bChange.getStep();
                }

                LogToFile.d("pl2303", "LiveType.BACK_NEW====回退1步+allstep："+allStep);

                liveType.setType(LiveType.BACK_NEW);
                liveType.setBackNum(1);         // 回退1步
                liveType.setBackNew(allStep);   // 新增2步
                return liveType;

            }


        }


        LogToFile.d("pl2303", "一种特殊情况的处理。1个或多个新增，1个或多个消失的情况");

        return null;

    }


    /**
     * 有死子的时候未提子，就悔棋
     */
    private boolean isBackWhenPick(List<ChessChange> changeList, Board board) {
        LogToFile.d("pl2303", "开始进行第四层判断：有死子的时候未提子，就悔棋");
        List<ChessChange> changes = new ArrayList<>();
        changes.addAll(changeList);
        int num = 0;
        for (ChessChange change : changeList) {
            if (change.getNowColor() == 0 && change.getPreColor() != 0) {
                num++;
                changes.remove(change);
            }
        }

        if (num != 1)//只有一颗子消失
            return false;


        List<PieceProcess> deadList = getLastDeadList(board);

        if (deadList.size() != changeList.size() - num)
            return false;

        for (ChessChange change : changes) {
            if (!isDeadPiece(change, deadList))
                return false;
        }

        return true;
    }

    // ===========辅助函数：单个消失的子，是否是在新增子落下后变成死子
    public boolean isPreDeadPiece(ChessChange dismiss, ChessChange add, Board board) {
        String allStep = SgfHelper.getBoardsgfStr(board);
        Board tempBoard = new Board();
        try {
            List<Coordinate> cs = SgfHelper.getCoordListformStr(allStep);
            for (Coordinate c : cs) {
                tempBoard.put(c.x, c.y);
            }

            // 判断即将落下去的点是否是打劫点。
//            if(tempBoard.prePut(add.getX(),add.getY()))
//            {
//                tempBoard.put(add.getX(), add.getY());
//            }else
//            {
//                return false;    // 落点不合法
//            }

            if(!tempBoard.put(add.getX(), add.getY())) return false;

            List<PieceProcess> deadList = getLastDeadList(tempBoard);

            return isDeadPiece(dismiss, deadList);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ===================辅助函数：消失的子是即将落下的子的死子=========================
    public boolean isPreDeadPieceList(List<ChessChange> changeList, ChessChange add, Board board) {
        String allStep = SgfHelper.getBoardsgfStr(board);
        Board tempBoard = new Board();
        try {
            List<Coordinate> cs = SgfHelper.getCoordListformStr(allStep);
            for (Coordinate c : cs) {
                tempBoard.put(c.x, c.y);
            }

            if(!tempBoard.put(add.getX(), add.getY()))return false;

            List<PieceProcess> deadList = getLastDeadList(tempBoard);

            return isAllDead(changeList, deadList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    // ===================辅助函数：消失的子是即将落下的第2颗子的死子  2018-2-27 新增=========================
    public boolean isPreDeadPieceList2(List<ChessChange> dismissList, ChessChange add1,ChessChange add2, Board board) {
        String allStep = SgfHelper.getBoardsgfStr(board);
        Board tempBoard = new Board();
        try {

            // 先将第1步落子追加到原棋谱。
            if(add1.getNowColor()==1)
            {
                allStep += "+" + add1.getStep();
            }else
            {
                allStep += "-" + add1.getStep();
            }

            List<Coordinate> cs = SgfHelper.getCoordListformStr(allStep);
            for (Coordinate c : cs) {
                tempBoard.put(c.x, c.y);
            }

            tempBoard.put(add1.getX(), add1.getY());

            List<PieceProcess> deadList = getLastDeadList(tempBoard);

            return isAllDead(dismissList, deadList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ===================辅助函数：消失的是否是两步棋各自的死子   2018-2-27 新增=========================
    public boolean  isPreTwoStepDeadPieceList(List<ChessChange> changeList, ChessChange add1,ChessChange add2, Board board) {
        String allStep = SgfHelper.getBoardsgfStr(board);
        Board tempBoard = new Board();
        try {

            List<Coordinate> cs = SgfHelper.getCoordListformStr(allStep);
            for (Coordinate c : cs) {
                tempBoard.put(c.x, c.y);
            }

            tempBoard.put(add1.getX(), add1.getY());
            List<PieceProcess> deadList = getLastDeadList(tempBoard);  //第1步产生的死子列表。


            tempBoard.put(add2.getX(), add2.getY());
            List<PieceProcess> deadList2 = getLastDeadList(tempBoard);  //第2步产生的死子列表。


            deadList.addAll(deadList2);   // 合并两个列表。两者理论上不会有交集。

            return isAllDead(changeList, deadList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * 获取最后一步的死子列表
     */
    private List<PieceProcess> getLastDeadList(Board board) {
        List<PieceProcess> deadList = new ArrayList<PieceProcess>();
        if (board.getCount() > 0) {
            PieceProcess lastPieceProcess = board.getPieceProcess(board
                    .getCount() - 1); // 最后棋步
//            if(lastPieceProcess.removedList!=null && lastPieceProcess.removedList.size()>0)
//                deadList.addAll(lastPieceProcess.removedList);
              deadList = lastPieceProcess.removedList;
        }
        return deadList;
    }

    /**
     * 获取某一步的死子列表
     */
    private List<PieceProcess> getDeadList(Board board, int index) {
        List<PieceProcess> deadList = new ArrayList<>();
        if (board.getCount() > 0) {
            PieceProcess lastPieceProcess = board.getPieceProcess(index - 1); // 最后棋步
            deadList = lastPieceProcess.removedList;
        }
        return deadList;
    }


    private List<ChessChange> getDifferent(String receiveValidData, String preReceiveData, int rotate) {
        List<ChessChange> changeList = new ArrayList<ChessChange>();

        if (receiveValidData.length() != Board.n * Board.n) {//数据发生异常，重新请求数据
            LogToFile.d("========数据异常，接收到的数据长度不为361========= ", "receiveValidData:" + receiveValidData);
            Writestartoverall();
            return null;
        }

        char curChar;
        char preChar;

        int xx;
        int yy;
        for (int i = 1; i <= receiveValidData.length(); i++) {
            curChar = receiveValidData.charAt(i - 1);
            preChar = preReceiveData.charAt(i - 1);

            if (!isNum(String.valueOf(curChar))
                    || !isNum(String.valueOf(preChar))) {
              //  System.out.println("========错误指令（盘面包含非数字）========= " + receiveValidData);
                LogToFile.d("========错误指令（盘面包含非数字）========= ", "receiveValidData:" + receiveValidData);
                Writestartoverall();
                return null;
            }

            if (curChar != preChar) {
                System.out.println("不同点：" + i + "====" + preChar + curChar);
                String[] b = CoordinateTransition(i, rotate);
                xx = Integer.parseInt(b[0]);
                yy = Integer.parseInt(b[1]);

                ChessChange change = new ChessChange();

                change.setI(i - 1);
                change.setX(xx);
                change.setY(yy);
                // change.setPreColor(board.getValue(xx, yy));
                // 或者
                change.setPreColor(Integer.parseInt(String.valueOf(preChar)));
                change.setNowColor(Integer.parseInt(String.valueOf(curChar)));
                change.setStep(b[2]);
                changeList.add(change);

            }
        }
        return changeList;
    }

    // ================辅助函数：比较当前帧与前一帧变化的异同点，找到固定不变的点信息===============
    // 此方法存在漏洞，没有考虑消失的点是否属于正常消失。
    private LiveType getMayValidStep(Board board,LiveType liveType)
    {

        int listSize=liveTypeList.size();

        if(listSize==0) return  liveType;

        LiveType preLiveType=liveTypeList.get(listSize-1);

        List<ChessChange>  nowChangelist= liveType.getErrorList();
        List<ChessChange>  preChangelist= preLiveType.getErrorList();

        if(nowChangelist==null || preChangelist==null) return null;

        List<ChessChange> staticChages=new ArrayList<ChessChange>();    // 固定不变的变化点，很有可能是棋子，不是干扰

        // 查找以上这两个变化列表，看是否具有固定不变的新增点。

        for(int i=0;i<preChangelist.size();i++)
        {
            for(int j=0;j<nowChangelist.size();j++)
            {
                if(preChangelist.get(i).equals(nowChangelist.get(j)) && (preChangelist.get(i).getPreColor()==0 && preChangelist.get(i).getNowColor()!=0))
                {
                    staticChages.add(preChangelist.get(i));
                }
            }
        }

        // 如果新增固定不变的点个数始终不止一个，无法处理识别此情况。
        if(staticChages.size()!=1){
            return  null;
        }

        // 第1帧的数据得到的可能有效落子颜色
        int preNowColor=staticChages.get(0).getNowColor();


        String userBW = board.getCurBW(); // 当前轮到黑或者白下。

        // 如果得到的第1步颜色不对头，也不能处理。
        if((userBW.equals("+") && preNowColor==2) || (userBW.equals("-")&& preNowColor==1))
        {
            return null;
        }


        int nowColor=getReverColor(preNowColor);


        List<ChessChange> nowChages=new ArrayList<ChessChange>();
        // 遍历当前变化，找到异色的新增点（消失的、同色继续新增等都不予考虑）
        for(int i=0;i<nowChangelist.size();i++)
        {
            if(nowChangelist.get(i).getPreColor()==0 && nowChangelist.get(i).getNowColor()==nowColor)
            {
                nowChages.add(nowChangelist.get(i));
            }
        }

        // 如果新增固定不变的点个数始终不止一个，无法处理识别此情况。
        if(nowChages.size()!=1){
            return  null;
        }


        // 进一步看看当前变化nowChangelist 中消失的子是否是合理的消失。 新增 2018-2-27
        List<ChessChange>  dismissList= new ArrayList<ChessChange>();
        for(int i=0;i<nowChangelist.size();i++)
        {
            if(nowChangelist.get(i).getNowColor()==0)
            {
                dismissList.add(nowChangelist.get(i));
            }
        }

        if(dismissList.size()>0)
        {
            if(isAllDead(dismissList,getLastDeadList(board)) || isValidDisappear(board,dismissList))
            {

            }else   // 消失的子异常
            {
                return  null;
            }
        }


        String allStep;
        if (userBW.equals("+")) {         // 轮到黑下
                allStep = "+" + staticChages.get(0).getStep() + "-" + nowChages.get(0).getStep();
        } else {
                allStep = "-" + staticChages.get(0).getStep() + "+" + nowChages.get(0).getStep();
        }

        LiveType resLivetype=new LiveType();

        resLivetype.setAllStep(allStep);
        resLivetype.setType(LiveType.NEW_CHESS_2);
        return  resLivetype;
    }



    // ================辅助函数：比较当前帧与前一帧、前前一帧变化的异同点，找到固定不变的点信息===============
    // 存在风险：随机产生的棋步，有可能会有提子。！！
    private LiveType getMayValidStep2(Board board,LiveType liveType)
    {

        int listSize=liveTypeList.size();

        if(listSize<2) return  liveType;

        LiveType preLiveType=liveTypeList.get(listSize-1);
        LiveType pre2LiveType=liveTypeList.get(listSize-2);

        // 3帧数据，能够顺利得到两步棋，则一般表面后续在正规交替下棋了。

        if(!preLiveType.getType().equalsIgnoreCase(LiveType.LAST_ERROR_MORE_ADD) || !pre2LiveType.getType().equalsIgnoreCase(LiveType.LAST_ERROR_MORE_ADD)) return null;


        List<ChessChange>  nowChangelist= liveType.getErrorList();
        List<ChessChange>  preChangelist= preLiveType.getErrorList();
        List<ChessChange>  pre2Changelist= pre2LiveType.getErrorList();

        if(nowChangelist==null || preChangelist==null || pre2Changelist==null) return null;

        if(!(nowChangelist.size()==preChangelist.size()+1 && preChangelist.size()==pre2Changelist.size()+1))
        {
            return  null;
        }

        List<ChessChange> nowChangelistBackup=new ArrayList<ChessChange>(); // 备用，用于后面求交集

        List<ChessChange> nowChangelistTemp=new ArrayList<ChessChange>();
        List<ChessChange> preChangelistTemp=new ArrayList<ChessChange>();
        List<ChessChange> pre2ChangelistTemp=new ArrayList<ChessChange>();


        nowChangelistBackup.addAll(nowChangelist);

        nowChangelistTemp.addAll(nowChangelist);
        preChangelistTemp.addAll(preChangelist);
        pre2ChangelistTemp.addAll(pre2Changelist);


        if(nowChangelistTemp.containsAll(preChangelistTemp) && preChangelistTemp.containsAll(pre2ChangelistTemp))
        {
            nowChangelistTemp.removeAll(preChangelistTemp);
            preChangelistTemp.removeAll(pre2ChangelistTemp);


            if(nowChangelistTemp.size()==1 && preChangelistTemp.size()==1)
            {

                ChessChange preStep=preChangelistTemp.get(0);
                ChessChange nowStep=nowChangelistTemp.get(0);

                // 三帧数据，得到两步棋。
               if((nowStep.getNowColor()==1 && preStep.getNowColor()==2) || (nowStep.getNowColor()==2 && preStep.getNowColor()==1))
                {

                    nowChangelistBackup.retainAll(pre2Changelist);  // 得到交集，前面的落子。多步。由于未知原因未能记录上。

                    String userBW = board.getCurBW(); // 当前轮到黑或者白下。

                    String preStepStr="";

                    List<String> blackSteps=new ArrayList<String>();
                    List<String> whiteSteps=new ArrayList<String>();

                    for(int i=0;i<nowChangelistBackup.size();i++)
                    {
                        if(nowChangelistBackup.get(i).getNowColor()==0) return  null;
                        if(nowChangelistBackup.get(i).getPreColor()!=0) return  null;

                        if(nowChangelistBackup.get(i).getPreColor()==0 && nowChangelistBackup.get(i).getNowColor()==1)
                        {
                            blackSteps.add("+"+nowChangelistBackup.get(i).getStep());
                        }else if(nowChangelistBackup.get(i).getPreColor()==0 && nowChangelistBackup.get(i).getNowColor()==2)
                        {
                            whiteSteps.add("-"+nowChangelistBackup.get(i).getStep());
                        }

                    }

                    if(Math.abs(blackSteps.size()-whiteSteps.size())>1) return null; // 不能构成+-+ -+- +-+- -+-+

                    if(blackSteps.size()+whiteSteps.size()!=nowChangelistBackup.size()) return null;


                    // 由于黑白先后次序信息可能缺失，不做考虑了，直接随机拼接
                    if(userBW.equalsIgnoreCase("+"))
                    {

                        if(blackSteps.size()-whiteSteps.size()==0)   // 一样的步数
                        {
                            for(int k=0;k<blackSteps.size();k++)
                            {
                                preStepStr+=blackSteps.get(k);
                                preStepStr+=whiteSteps.get(k);
                            }

                        }else if(blackSteps.size()-whiteSteps.size()==1)  // 白少1步，最后一步依然是黑
                        {
                            for(int k=0;k<whiteSteps.size();k++)
                            {
                                preStepStr+=blackSteps.get(k);
                                preStepStr+=whiteSteps.get(k);
                            }

                            preStepStr+=blackSteps.get(whiteSteps.size());

                        }else
                        {
                            return null;
                        }

                    }else   // 轮到白下
                    {

                        if(whiteSteps.size()-blackSteps.size()==0)   // 一样的步数
                        {
                            for(int k=0;k<blackSteps.size();k++)
                            {
                                preStepStr+=whiteSteps.get(k);
                                preStepStr+=blackSteps.get(k);
                            }

                        }else if(whiteSteps.size()-blackSteps.size()==1)  // 白少1步，最后一步依然是黑
                        {
                            for(int k=0;k<blackSteps.size();k++)
                            {
                                preStepStr+=whiteSteps.get(k);
                                preStepStr+=blackSteps.get(k);
                            }

                            preStepStr+=whiteSteps.get(blackSteps.size());

                        }else
                        {
                            return null;
                        }

                    }


                    // 前面的次序忽略后，已得到完整的棋步。下面开始获取最新的很有可能正确的两步棋。然后进行拼接。

                    // 偶数步
                    if(nowChangelistBackup.size() % 2==0)
                    {
                        // 偶数步之后落子黑白与之前的一致
                        if(userBW.equalsIgnoreCase("+") && preStep.getNowColor()==1)
                        {
                            preStepStr+="+"+preStep.getStep()+"-"+nowStep.getStep();
                        }else if(userBW.equalsIgnoreCase("-") && preStep.getNowColor()==2){
                            preStepStr+="-"+preStep.getStep()+"+"+nowStep.getStep();
                        }else
                        {
                            return  null;
                        }

                    }else
                    {

                        // 偶数步之后落子黑白与之前的一致
                        if(userBW.equalsIgnoreCase("+") && preStep.getNowColor()==2)
                        {
                            preStepStr+="-"+preStep.getStep()+"+"+nowStep.getStep();
                        }else if(userBW.equalsIgnoreCase("-") && preStep.getNowColor()==1){
                            preStepStr+="+"+preStep.getStep()+"-"+nowStep.getStep();
                        }else
                        {
                            return  null;
                        }

                    }

                    LiveType resLivetype=new LiveType();
                    resLivetype.setAllStep(preStepStr);
                    resLivetype.setType(LiveType.NEW_CHESS_2);   // 新增多步棋
                    return  resLivetype;

                }

            }

        }

        return null;

    }



    // ================辅助函数：判断是否是死子位置===============
    private boolean isDeadPiece(ChessChange change, List<PieceProcess> deadList) {
        PieceProcess piece;
        for (int i = 0; i < deadList.size(); i++) {
            piece = deadList.get(i);
            if (change.getX() == piece.c.x && change.getY() == piece.c.y) {
                return true;
            }

        }
        return false;
    }

    // ================辅助函数：判断变化点是否是最后一步===============
    private boolean isLastPiece(ChessChange change, Board board) {

        Coordinate lastC = board.getLastPosition();

        if (lastC.x == change.getX() && lastC.y == change.getY()) {
            return true;
        }

        return false;
    }

    // ================辅助函数：判断是否是批量消失[2018-3-6 再次修改]=================
    private boolean isAllDisappearRobot(List<ChessChange> changeList) {
        for (int i = 0; i < changeList.size(); i++) {
            if (!(changeList.get(i).getPreColor() > 0 && changeList.get(i).getNowColor() == 0)) {
                return false;
            }
        }
        return true;

//        for (int i = 0; i < changeList.size(); i++) {
//            if (changeList.get(i).getNowColor() != 0 || changeList.get(i).getPreColor() == 0) {
//                return false;
//            }
//        }
//        return true;
    }

    // ===================辅助函数：判断变化的是否全是死子位置======================
    private boolean isAllDead(List<ChessChange> changeList,
                              List<PieceProcess> deadList) {
        if (deadList.size() == 0)
            return false;

        boolean allhas = false;

        int k = 0;

        for (int i = 0; i < changeList.size(); i++) {

            boolean has = false;

            ChessChange change = changeList.get(i);
            System.out.println("=====提子change=====" + change.getX() + change.getY());
            for (int j = 0; j < deadList.size(); j++) {
                System.out.println("=====提子dead=====" + deadList.get(j).c.x + deadList.get(j).c.y);
                if (change.getX() == deadList.get(j).c.x
                        && change.getY() == deadList.get(j).c.y) {
                    has = true;
                    break;
                }
            }

            if (has) {
                k++;
            }

        }

        if (changeList.size() == k) {
            return true;
        }
        return false;
    }

    // ====================辅助函数：判断是否是合理消失【有提子的情况不考虑，有提子情况不适用本方法】====================
    private boolean isValidDisappear(Board board, List<ChessChange> changeList) {

        int totalNum = board.getCount();    // 总的棋步数，可能比361都大的（因为有提子）。
        int changeNum = changeList.size();  // 不可能超过361

        if (totalNum == 0)
            return false;

        if (changeNum > totalNum)
            return false;

        ChessChange change = new ChessChange();
        PieceProcess piece = null;

        int num = 0;

        for (int i = 0; i < changeNum; i++) {

            change = changeList.get(i);
            boolean has = false;

            for (int j = 0; j < changeNum; j++) {
                piece = board.getPieceProcess(totalNum - j - 1);
                if (piece.c.x == change.getX() && piece.c.y == change.getY()) {
                    has = true;
                    break;
                }
            }

            if (has) {
                num++;
            }
        }

        if (num == changeNum)
            return true;

        return false;
    }


    // ================辅助函数：判断所有死子是否已被提空===============
    private boolean isFinishPickStone(List<PieceProcess> deadList,
                                      String receiveValidData, int rotate) {

        if (deadList.size() == 0)
            return true;
        int x;
        int y;
        int num = 0;

        for (int i = 0; i < deadList.size(); i++) {
            x = deadList.get(i).c.x;
            y = deadList.get(i).c.y;
            switch (rotate) {
                case 0:
                    num = Board.n * (y - 1) + x; // (2,1)==>2
                    break;
                case 90:
                    //num = Board.n * (Board.n - 1) + (Board.n - y + 1);
                    num=Board.n*(x-1)+(Board.n-y+1);
                    break;
                case 180:
                    num = Board.n * (Board.n - y) + (Board.n - x + 1);
                    break;
                case 270:
                    num = Board.n * (Board.n - x) + y;
                    break;
            }
            // ===【坐标转化】===
            // num=361-num+1;
            if (receiveValidData.charAt(num - 1) != '0') // 非空
            {
                return false;
            }
        }
        return true;
    }

    private boolean isFinishPickStone(ChessChange change,
                                      List<PieceProcess> deadList, String receiveValidData, int rotate) {

        if (deadList.size() == 0)
            return true;
        int x = 0;
        int y = 0;
        int num = 0;

        for (int i = 0; i < deadList.size(); i++) {
            x = deadList.get(i).c.x;
            y = deadList.get(i).c.y;

            if (change.getX() == x && change.getY() == y)
                continue;

            switch (rotate) {
                case 0:
                    num = Board.n * (y - 1) + x; // (2,1)==>2
                    break;
                case 90:
                    num=Board.n*(x-1)+(Board.n-y+1);
                    //num = Board.n * (Board.n - 1) + (Board.n - y + 1);
                    break;
                case 180:
                    num = Board.n * (Board.n - y) + (Board.n - x + 1);
                    break;
                case 270:
                    num = Board.n * (Board.n - x) + y;
                    break;
            }

            // ===【坐标转化】===
            // num=361-num+1;

            if (receiveValidData.charAt(num - 1) != '0') // 非空
            {
                return false;
            }

        }

        return true;
    }

    // ====================辅助函数：是否是新子批量消失+更改一个====================
    private String[] doWithLoseAndAlter(Board board, List<ChessChange> changeList,List<PieceProcess> deadList) {

        String[] a = {"", ""};

        int totalNum = board.getCount();    // 正常网络棋谱总步数
        int changeNum = changeList.size();  // 总变化数

        if (totalNum == 0) // 一般不会出现
            return a;

        if(changeNum==1)  // 只有一个变化，应该也是单个点变化判断之外漏网之鱼。
        {
           if( isDeadPiece(changeList.get(0),deadList))
           {
               a[0]="wait-pick";
               return a;
           }

        }

        // 2018-1-10，第一步悔棋BUG
//        if (changeNum > totalNum)
//            return a;

        ChessChange change = new ChessChange();
        PieceProcess piece = null;

        int num = 0;
        int addIndex = -1;

        // 看最后的changeNum-1步是否消失的是否
        for (int i = 0; i < changeNum; i++) {

            change = changeList.get(i);
            boolean has = false;

            for (int j = 0; j < changeNum; j++) {

                if(totalNum-j-1<0)
                {
                    break;  // 棋谱遍历都到头了，还没有匹配到。则该变化点为非最后棋步点。
                }

                piece = board.getPieceProcess(totalNum - j - 1);
                if (piece.c.x == change.getX() && piece.c.y == change.getY()
                        && change.getNowColor() == 0) {
                    has = true;
                    break;
                }
            }

            if (has) {
                num++;
            } else {
                addIndex = i; // 这个变化不在原来的棋谱倒数changeNum - 1 之列
            }
        }

        if (num == changeNum - 1) // 最后changeNum-1消失，再检测最后一个变化的位置是否是合理的改变
        {
            change = changeList.get(addIndex); // 必不是空位置。颜色为黑或者白。

            // 颜色匹配上后，再判断该点是否是新增点。判断标准，棋谱上原来该点是否有子。【新增一个棋子的情况】
            if(change.getPreColor()==0 && change.getNowColor()!=0){
                // if (board.getValue(change.getX(), change.getY()) == 0) {

                String curBW = board.getCurBW();

                if (num % 2 != 0) // 偶数步之前，依然为curBW,否则，进行修正。
                {
                    if (curBW.equals("+")) {
                        curBW = "-";
                    } else {
                        curBW = "+";
                    }
                }

                if (curBW.equals("+")) {
                    if (change.getNowColor() == 1) {

                    } else {
                        return a;
                    }
                } else {
                    if (change.getNowColor() == 2) {

                    } else {
                        return a;
                    }
                }

                // 计算得到了个新增子，看是否是提子完成后，立刻落子（提子和落子在同一帧，落子位置在死子区域之外）提子完成状态修正。
                if (num==0&&board.getPieceProcess(board.getCount() - 1).removedList != null && (board.getPieceProcess(board.getCount() - 1).removedList.size() != 0)) {
                    List<PieceProcess> list =new ArrayList<PieceProcess>();
                    list.addAll(board.getPieceProcess(board.getCount()  - 1).removedList);
                    if (mcontext instanceof MyService) {
                        MyService robotActivity = (MyService) mcontext;
                        robotActivity.getTileHelper().getMSparseArray().put(board.getCount()-1,list);
                    }

                }

                a[0] = String.valueOf(changeNum - 1); // 回退步数
                a[1] = curBW + change.getStep();

            } else {

                // ======================【修改一个棋子的情况，似乎不太靠谱，没理由前面某一步颜色改变，除非悔棋多步落子在曾经的点（悔掉的）上】
                String curBW = board.getCurBW();

                if (changeNum % 2 != 0) // 偶数步之前，依然为curBW,否则，进行修正。
                {
                    if (curBW.equals("+")) {
                        curBW = "-";
                    } else {
                        curBW = "+";
                    }
                }

                if (curBW.equals("+")) {
                    if (change.getNowColor() == 1) {

                    } else {
                        return a;
                    }
                } else {
                    if (change.getNowColor() == 2) {

                    } else {
                        return a;
                    }
                }

                if (isInLastStepList(change, board, changeNum)) {

                    a[0] = String.valueOf(changeNum); // 回退步数
                    a[1] = curBW + change.getStep();

                } else {

                    return a;
                }
            }

        }

        if( isAllDead(changeList,deadList))  // 变化点都是死子位置。【颜色要求暂时未提及】
        {
            a[0]="wait-pick";
        }

        return a;

    }

    // ====================辅助函数：判断棋子是是最后下的N步（如倒数第一步，第二步等）====================
    private boolean isInLastStepList(ChessChange change, Board board, int step) {
        int totalNum = board.getCount();

        if (totalNum < step)
            return false;

        PieceProcess piece = null;
        boolean has = false;

        for (int i = 0; i < step; i++) {
            piece = board.getPieceProcess(totalNum - i - 1);
            if (piece.c.x == change.getX() && piece.c.y == change.getY()) {
                has = true;
                break;
            }

        }
        return has;
    }

    // ================辅助函数：判断变化点是否是倒数第step步===============
    private boolean isLastPiece(ChessChange change, Board board, int step) {
        int totalNum = board.getCount();

        if (totalNum < step)
            return false;

        PieceProcess piece = board.getPieceProcess(totalNum  - step);

        Coordinate lastC = piece.c;

        if (lastC.x == change.getX() && lastC.y == change.getY()) {
            return true;
        }

        return false;
    }


    public int getReverColor(int _color)
    {
        if(_color==1)
        {
            return 2;
        }else if(_color==2)
        {
            return 1;
        }

        return 0;
    }

    public void Writestartoverall() {
        String writeoverall = "";
        writeoverall = "~STA#";
        WriteToUARTDevice(writeoverall);
    }

    private void init() {
        mSerialMulti = new PL2303MultiLib(
                (UsbManager) mcontext.getSystemService(Context.USB_SERVICE),
                mcontext, ACTION_USB_PERMISSION);
        mBridge = ActivityCallBridge.getInstance();
    }

    public boolean PL2303MultiLiblinkExist() {
        return mSerialMulti == null || mSerialMulti.PL2303IsDeviceConnectedByIndex(DeviceIndex);
    }

    public void pl2303DisConnect(){
        if (mSerialMulti!=null){
            mSerialMulti.PL2303Release();
            mSerialMulti = null;
        }
    }

    // 写入数据
    public void WriteToUARTDevice(final String strWrite) {
        if (!PL2303MultiLiblinkExist() || TextUtils.isEmpty(strWrite)) {
            return;
        }

        try {
            Thread.currentThread().sleep(delayTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mSerialMulti != null)
            mSerialMulti.PL2303Write(DeviceIndex, strWrite.getBytes());
    }

    public void WriteFileToUARTDevice(String filePath) {
//        if (!PL2303MultiLiblinkExist()) {
//            return;
//        }

        try {
            Thread.currentThread().sleep(delayTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mSerialMulti != null) {
            byte[] data = getBytes(filePath);
            int max = data.length/1024 + 1;
            for (int i=0;i<max;i++){
                int length = (data.length - i*1024)>1024?1024:(data.length - i*1024);
                byte[] msg = new byte[length];
                System.arraycopy(data,i*1024,msg,0,length);
                mSerialMulti.PL2303Write(DeviceIndex, msg);
            }
        }
    }

    /**
     * 获得指定文件的byte数组
     */
    private byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            Log.d("文件大小", file.length() + "\t\\\t" + fis.available());

            ByteArrayOutputStream bos = new ByteArrayOutputStream(fis.available());
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }


    //坐标转换成字符
    public String GoCoordinateTransition(String GoCoodinate, int rotate) {
        int CoordinateXY = 0;
        String CoordinateX = GoCoodinate.substring(3, 5);
        String CoordinateY = GoCoodinate.substring(1, 3);
        int x = Integer.valueOf(CoordinateX);
        int y = Integer.valueOf(CoordinateY);

        switch (rotate) {
            case 0:
                CoordinateXY = (Board.n - y) * Board.n + (Board.n - x + 1);
                break;
            case 90:
                CoordinateXY = (Board.n - x) * Board.n + y;
                break;
            case 180:
                CoordinateXY = (y - 1) * Board.n + x;
                break;
            case 270:
                CoordinateXY = (x - 1) * Board.n + (Board.n - y + 1);
                break;
        }

        String singleGoCoordinate = String.valueOf(CoordinateXY);
        if (CoordinateXY < 10) {
            singleGoCoordinate = "00" + singleGoCoordinate;
        } else if (CoordinateXY >= 10 && CoordinateXY < 100) {
            singleGoCoordinate = "0" + singleGoCoordinate;
        }
        return singleGoCoordinate;
    }


    /**
     * 新版协议：
     * ~LEDxx#   行棋指示灯控制命令
     * 第一个x如果是1 代表的是黑方亮灯，如果是2代表是白方亮灯。如果是其他数字，则返回错误。
     * 第二个x如果是1，代表打开绿色led，如果是0，代表是关闭绿色led。
     */
    /**
     * 控制黑方亮灯
     *
     * @param Color RGB颜色（100代表红色，010代表绿色，001，代表红色）
     */
    public void WriteOpenBlackLamp(String Color) {
//        String writeremain = "~LED1" + Color + "#";
        String writeremain = "~LED1" + 1 + "#";
        WriteToUARTDevice(writeremain);
    }

    /**
     * 控制白方亮灯
     *
     * @param Color RGB颜色（100代表红色，010代表绿色，001，代表红色）
     */
    public void WriteOpenWhiteLamp(String Color) {
//        String writeremain = "~LED2" + Color + "#";
        String writeremain = "~LED2" + 1 + "#";
        WriteToUARTDevice(writeremain);
    }

    /**
     * singleGoCoodinate 对方最新落子坐标 isbremove 是否要移除这个点的LED灯
     */
    public void WritesingleGoCoodinate(String singleGoCoodinate,
                                       boolean isbremove, int rotate) {
        if (singleGoCoodinate == null)
            return;
        String curBW;
        String GoCoodinate;
        String WritesingleGoCoordinate = "";
        if (singleGoCoodinate.length() == 5) {
            curBW = singleGoCoodinate.substring(0, 1);
            GoCoodinate = GoCoordinateTransition(singleGoCoodinate, rotate);
            String colorWitle = "r000g000b000";
            if ("+".equals(curBW)) {
                String colorRead = "r255g000b000";
                WritesingleGoCoordinate = "~SHP" + GoCoodinate + ","
                        + (isbremove ? colorWitle : colorRead) + ",1#";
            } else if ("-".equals(curBW)) {
                String colorBlue = "r000g000b255";
                WritesingleGoCoordinate = "~SHP" + GoCoodinate + ","
                        + (isbremove ? colorWitle : colorBlue) + ",1#";
            }
        }
        WriteToUARTDevice(WritesingleGoCoordinate);

    }

    private boolean isSound;//报警声音是否开启

    // 发出警告音
    public void WriteWarning() {
        isSound = !SharedPrefsUtil.getValue(mcontext, "sound", false);
        if(isSound){
            String writeremain = "";
            writeremain = "~AWO#";
            WriteToUARTDevice(writeremain);
        }
    }

    public String reverStr(String receiveStr) {

        char[] array = receiveStr.toCharArray();
        String reverse = "";
        for (int i = array.length - 1; i >= 0; i--)
            reverse += array[i];

        return reverse;
    }

    public String getAllStep(String receiveData, int rotate) {
        StringBuffer sb = new StringBuffer();
        char curChar;

        for (int i = 1; i <= receiveData.length(); i++) {
            curChar = receiveData.charAt(i - 1);
            if (curChar != '0') {
                String[] b = CoordinateTransition(i, rotate);
                String bw;
                if (curChar == '1') {
                    bw = "+";
                } else {
                    bw = "-";
                }
                sb.append(bw).append(b[2]);
            }
        }

        return sb.toString();
    }

    // 一维坐标转化为二维坐标， 例如： i=1 对应 （1,1）,i=2 对于（2,1）
    private String[] CoordinateTransition(int i, int rotate) {
        String[] arr = {"", "", ""};

        int xx = 0;
        int yy = 0;

        // ===【坐标转化】===
        // i=361-i+1;

        // i=1 对应 （1,1）,i=2 对于（2,1）
        switch (rotate) {
            case 0:
                if (i % Board.n == 0) {
                    yy = (i / Board.n);
                    xx = i - Board.n * (yy - 1);
                } else {
                    xx = i % Board.n;
                    yy = (i - xx) / Board.n + 1;
                }
                break;
            case 90:
                if (i % Board.n == 0) {
                    yy = 1;
                    xx = i / Board.n;
                } else {
                    xx = i / Board.n + 1;
                    yy = Board.n - i % Board.n + 1;
                }
                break;
            case 180:
                if (i % Board.n == 0) {
                    yy = Board.n - (i / Board.n) + 1;
                    xx = 1;
                } else {
                    xx = Board.n - i % Board.n + 1;
                    yy = Board.n - i / Board.n;
                }
                break;
            case 270:
                if (i % Board.n == 0) {
                    xx = Board.n - i / Board.n + 1;
                    yy = Board.n;
                } else {
                    xx = Board.n - i / Board.n;
                    yy = i % Board.n;
                }
                break;

        }


        String Y = String.valueOf(yy);
        String X = String.valueOf(xx);

        String theCoordinateXY = "";

        if (xx < 10 && yy < 10) {
            theCoordinateXY = "0" + Y + "0" + X;
        } else if (xx >= 10 && yy < 10) {
            theCoordinateXY = "0" + Y + X;
        } else if (xx < 10 && yy >= 10) {
            theCoordinateXY = Y + "0" + X;
        } else if (xx >= 10 && yy >= 10) {
            theCoordinateXY = Y + X;
        }

        arr[0] = String.valueOf(xx);
        arr[1] = String.valueOf(yy);
        arr[2] = theCoordinateXY;

        return arr;
    }

    public BroadcastReceiver PLMultiLibReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mSerialMulti.PLUART_MESSAGE)) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    String str = (String) extras.get(mSerialMulti.PLUART_DETACHED);
                    int index = Integer.valueOf(str);
                    if (DeviceIndex == index) {

                    }
                }
            }
        }
    };

    private boolean isNum(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    //=========================================================
    private static final int DeviceIndex2 = 1;
    private IReadDataFromRobot iReadDataFromRobot;
    private byte[] ReadByte2 = new byte[4096];
    private int ReadLen2;
    private String ReadHub2 = ""; // 存储接受到的信息，全局变量
    private String curReadData2 = "";
    private int firstStartCharIndex2 = -1;
    private String tempRest2 = "";
    private String TotalCommands2 = "";

    public void setiReadDataFromRobot(IReadDataFromRobot iReadDataFromRobot) {
        this.iReadDataFromRobot = iReadDataFromRobot;
    }

    public boolean OpenUARTDevice2(final View view) {
        if (!PL2303MultiLiblinkExist2()) {
            SnackUtil.IndefiniteSnackbar(view, "没有找到相关设备", Color.BLACK, Color.GREEN, Color.RED, Color.YELLOW, "重新连接", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callResume(view);
                    OpenUARTDevice2(view);
                }
            }).show();
            return false;
        }
        boolean res = mSerialMulti.PL2303OpenDevByUARTSetting(DeviceIndex2, mBaudrate,
                mDataBits, mStopBits, mParity, mFlowControl);
        if (!res) {
            SnackUtil.IndefiniteSnackbar(view, "Open失败", Color.BLACK, Color.GREEN, Color.RED, Color.YELLOW, "重新连接", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callResume(view);
                    OpenUARTDevice2(view);
                }
            }).show();
            return false;

        } else {
            SnackUtil.ShortSnackbar(view, "打开" + mSerialMulti.PL2303getDevicePathByIndex(DeviceIndex2) + "成功!", SnackUtil.Confirm).show();

            requestData2();
            return true;
        }
    }

    private void requestData2() {
        Flowable.interval(delayTime, delayTime, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (mSerialMulti != null && iReadDataFromRobot != null) {
                            try {
                                ReadLen2 = mSerialMulti.PL2303Read(DeviceIndex2, ReadByte2);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (ReadLen2 > 0) {

                                StringBuffer readdata = new StringBuffer();
                                for (int j = 0; j < ReadLen2; j++) {
                                    readdata.append((char) (ReadByte2[j] & 0x000000FF));
                                }

                                curReadData2 = readdata.toString(); // 将获取的棋盘数据转换成字符串

                                LogToFile.d("===原始数据ReadHub2", ReadHub2);
                                LogToFile.d("===本次读取curReadData2", curReadData2);

                                ReadHub2 += curReadData2; // 得到最完整的读取池数据

                                firstStartCharIndex2 = ReadHub2.indexOf("~");

                                if (firstStartCharIndex2 > 0) // 总池子不是以“~”开头，有故障。将开头数据摒弃。（-1，0，>0）
                                {
                                    LogToFile.d("===不是以~开头，丢弃的数据：",  ReadHub2.substring(0,firstStartCharIndex2));
                                    ReadHub2 = ReadHub2.substring(firstStartCharIndex2);
                                }

                                //开头必定为~号
                                if (ReadHub2.lastIndexOf("#") > 0) {//包含了#号（不包含则说明不包含一个完整的指令，直接跳出，等待下一次循环）
                                    if (ReadHub2.lastIndexOf("#") < ReadHub2.length() - 1) {//包含了部分下一条指令
                                        tempRest2 = ReadHub2.substring(ReadHub2
                                                .lastIndexOf("#") + 1); // 得到最后的#号之后的半截数据，暂时存储在临时变量tempRest2中
                                    }

                                    TotalCommands2 = ReadHub2.substring(0,
                                            ReadHub2.lastIndexOf("#") + 1); // 完整的指令集合

                                } else // 不包含结束符号，说明指令尚不完整，继续等待下一帧。
                                {
                                    return; // 继续下一次循环
                                }

                                //LogUtils.d("===初步处理数据ReadHub2", ReadHub2);

                                // ==================包含#号
                                // 解析数据，分解成一条条指令后交给前台调用者处理===================
                                if (ReadHub2.endsWith("#")
                                        && ReadHub2.lastIndexOf("~") == 0) // 一条完整的指令
                                {
                                    iReadDataFromRobot.readDataFromRobot(ReadHub2); // 刚好一条完整指令，则
                                    // 直接通知前台去处理即可

                                } else {
                                    // 尝试获取第一条完整指令
                                    int rp = firstStartCharIndex2 == -1 ? 0 : ReadHub2.lastIndexOf("~");
                                    for (int i = 0; i < TotalCommands2.length(); i++) {
                                        if (TotalCommands2.charAt(i) == '#') {
                                            if (rp < i + 1)
                                                iReadDataFromRobot.readDataFromRobot(TotalCommands2.substring(rp, i + 1));
                                            rp = i + 1;
                                        }
                                    }
                                }

                                ReadHub2 = tempRest2;
                            }
                        }
                    }
                });
    }

    public boolean PL2303MultiLiblinkExist2() {
        return mSerialMulti == null || mSerialMulti.PL2303IsDeviceConnectedByIndex(DeviceIndex2);
    }

    // 写入数据
    public void WriteToUARTDevice2(String strWrite) {
        if (!PL2303MultiLiblinkExist2() || TextUtils.isEmpty(strWrite)) {
            return;
        }

        try {
            Thread.currentThread().sleep(delayTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mSerialMulti != null)
            mSerialMulti.PL2303Write(DeviceIndex2, strWrite.getBytes());
    }

}
