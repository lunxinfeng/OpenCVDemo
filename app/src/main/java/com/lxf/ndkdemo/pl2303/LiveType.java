package com.lxf.ndkdemo.pl2303;

import java.util.List;


public class LiveType {
    /**
     * 从新展示棋谱，用于在线课堂
     */
    public static final String TILE_AGAIN = "tile_again";
    /**
     * 小智下棋
     */
    public static final String ROBOT_CHESS = "robot_chess";
    /**
     * 打劫
     */
    public static final String DA_JIE = "da_jie";
    /**
     * 提子完
     */
    public static final String FINISH_PICK = "finishpick";
    /**
     * 正常
     */
    public static final String DO_NOTHING = "donothing";
    /**
     * 最后一步消失，当作悔棋处理
     */
    public static final String LAST_BACK = "lastback";
    /**
     * 中间一颗子消失异常，需提示
     */
    public static final String LAST_ERROR = "lasterror";
    /**
     * 中间多颗消失异常，需提示
     */
    public static final String LAST_ERROR_MORE = "lasterrormore";
    /**
     * 中间多颗消失异常+新增一个，需提示
     */
    public static final String LAST_ERROR_MORE_ADD = "lasterrormoreadd";
    /**
     * 轮到白下，但是落的是黑子，可能是底层误判
     */
    public static final String LITTLE_ERROR = "littleerror";
    /**
     * 判断消失的次序是否合理。合理定义：末尾的棋步，可形成序列。回退N步
     */
    public static final String GO_BACK = "goback";
    /**
     * 新方法兼容
     */
    public static final String BACK_NEW = "backnew";
    public static final String NORMAL = "normal";
    /**
     * 3状态，落子
     */
    public static final String PUT_CHESS_3 = "putchess_3";
    /**
     * 3状态，提子
     */
    public static final String REMOVE_CHESS_3 = "removechess_3";
    /**
     * 3状态，移动棋子
     */
    public static final String BACK_NEW_3 = "backnew_3";
    /**
     * 黑方拍钟
     */
    public static final String TIME_BLACK_STOP = "time_black_stop";
    /**
     * 白方拍钟
     */
    public static final String TIME_WHITE_STOP = "time_white_stop";
    /**
     * 新增两颗子
     */
    public static final String NEW_CHESS_2 = "new_chess_2";


    private String type; //返回数据的问题类型
    private List<ChessChange> errorList; //多点异常集合
    private String backNew;//新棋步
    private ChessChange chessChange;//旗子状态
    private String allStep;
    private int backNum;
    private int index;//不同点的位置，1开始
//    private PieceProcess pieceProcess;

//    private int addPoints=0;    // 新增点个数
//    private int disPoints=0;    // 消失点个数
//    private int changePoints=0; // 变化点个数
//    private boolean used=false; // 该变化帧是否使用。正确帧自然使用，异常帧不使用。但通过校准后，异常帧也会改变状态。

//    public PieceProcess getPieceProcess() {
//        return pieceProcess;
//    }
//
//    public void setPieceProcess(PieceProcess pieceProcess) {
//        this.pieceProcess = pieceProcess;
//    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getBackNum() {
        return backNum;
    }

    public void setBackNum(int backNum) {
        this.backNum = backNum;
    }

    public void setAllStep(String allStep) {
        this.allStep = allStep;
    }

    public String getAllStep() {
        return allStep;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public ChessChange getChessChange() {
        return chessChange;
    }

    public void setChessChange(ChessChange chessChange) {
        this.chessChange = chessChange;
    }

    public List<ChessChange> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<ChessChange> errorList) {
        this.errorList = errorList;
    }

    public String getBackNew() {
        return backNew;
    }

    public void setBackNew(String backNew) {
        this.backNew = backNew;
    }

//    @Override
//    public String toString() {
//        return "LiveType [type=" + type + ", allStep=" + allStep + "]";
//    }

    @Override
    public String toString() {
        return "LiveType{" +
                "type='" + type + '\'' +
                ", backNew='" + backNew + '\'' +
                ", allStep='" + allStep + '\'' +
                ", backNum=" + backNum +
                '}';
    }


    @Override
    public boolean equals(Object obj) {


        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        LiveType liveType=(LiveType)obj;

        if(this.toString().equals(liveType.toString()))
        {

            if(this.getErrorList()==null && liveType.getErrorList()!=null) return false;
            if(this.getErrorList()!=null && liveType.getErrorList()==null) return false;

            if((this.getErrorList()==null && liveType.getErrorList()==null) || (this.getErrorList().containsAll(liveType.getErrorList()) && liveType.getErrorList().containsAll(this.getErrorList())))
            {
                if(this.getChessChange()!=null && this.getChessChange().equals(liveType.getChessChange()) )
                {
                    return  true;
                }

                if(this.getChessChange()==null && liveType.getChessChange()==null) return  true;

                return  false;

            }else
            {
                    return false;
            }


        }else
        {
            return false;
        }


    }
}
