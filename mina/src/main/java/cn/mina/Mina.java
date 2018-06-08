package cn.mina;

public class Mina {
    /**
     * 栈顶的Activity
     */
    public static String TOP_ACTIVITY = "ComponentInfo{cn.izis.mygo.mplat/cn.izis.mygo.chat.ChatOneByOneActivity}";
    public static Class<?> CHAT_ONE_BY_ONE;

    /**
     * 初始化
     * @param topActivity 私聊界面的包名
     * @param chatOneByOne 私聊界面的class对象
     */
    public static void init(String topActivity,Class<?> chatOneByOne){
        TOP_ACTIVITY = topActivity;
        CHAT_ONE_BY_ONE = chatOneByOne;
    }
}
