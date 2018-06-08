package cn.mina.constant;

import android.content.Context;
import android.content.res.Configuration;

public class PublicClass {

    /**
     * 方法调用标志符
     */
    public static final String flag = "0394cb6d-2575-4f73-92e9-6d402c39e20c";

    private static final String BASE = "app.izis.cn";
//	private static final String BASE = "192.168.1.115:8080";
//	private static final String BASE = "192.168.1.111:8080";
//    private static final String BASE = "192.168.1.100:8080";
//    private static final String BASE = "192.168.1.131:8080";
    /**
     * Web服务地址及命名空间 www.izis.cn:8080
     */
    public static final String NAMESPACE = "http://webservices.izis.cn/";
    public static final String NEWS_URL = "http://app.izis.cn/GoWebService/showNews?newsId=";
    public static String URL = "http://" + BASE + "/GoWebService/MyGoServicesPort";
    // public static final String URL =
    // "http://192.168.1.100:8080/GoWebService/MyGoServicesPort";

    /**
     * 验证码图片url
     */
    public static String IMG_URL = "http://" + BASE + "/GoWebService/AuthImage?deviceid=";
    public static final String SOFT_URL = "http://www.izis.cn:8080/GoWebService/";
    /**
     * 新的请求方式的URL地址
     */
//	public static String BASE_URL = "http://api.izis.cn/GoWebService";
    public static String BASE_URL = "http://" + BASE + "/GoWebService";
    public static String URL_GET = BASE_URL + "/getdataserver";
    public static String URL_POST = BASE_URL + "/postdataserver";

    public static String BASE_URL_MONEY = "http://" + BASE + "/mygomarket";
    public static String URL_GET_MONEY = BASE_URL_MONEY + "/getdataserver";
    public static String URL_POST_MONEY = BASE_URL_MONEY + "/postdataserver";

    // public static final String PAY_URL =
    // "http://app.izis.cn/GoWebService/servlet/PingppServlet";
    // public static final String PAY_URL =
    // "http://192.168.1.107:8080/GoWebService/servlet/PingppServlet";
    public static final String PAY_URL = "http://" + BASE + "/mygomarket/pingxxServlet";
    public static final String HEADURL = "http://app.izis.cn/GoWebService/UserHeadUploadServlet";
    public static final String USERCARDURL = "http://app.izis.cn/GoWebService/UserCardUploadServlet";
    public static final String HEADURLDWONLOAD = "http://app.izis.cn/GoWebService/UserHeadPhoto/";
    public static String ADDRESS = "app.izis.cn";// 192.168.150.16//本地  121.40.208.40
     //public static final String ADDRESS = "192.168.1.131"; //131
    //public static final String ADDRESS = "192.168.1.114"; //131

    public static final String USERIDCARDURL = "http://app.izis.cn/GoWebService/UserCardPhoto/";
    public static final int PORT = 8093;
    public static final String SOAP_ACTION = "http://webservices.izis.cn/Move_GetDate";
    public static final String METHOD_NAME = "Move_GetDate";

    public static final String SOAP_ACTION_BYTE = "http://tempuri.org/Move_GetByte";
    public static final String METHOD_NAME_BYTE = "Move_GetByte";

    public static final String SOAP_ACTION_UPDATE = "http://tempuri.org/Move_Update";
    public static final String METHOD_NAME_UPDATE = "Move_Update";
    public static int FIRST_PAUSE = 60;
    public static int SECOND_PAUSE = 141;

    public static int MinaGameid = 0;
    public static int MinaUserid = 0;

    public static String SYSTEM_TEST_LIBRARY = "001";

    public static boolean CONNECTION_STATE = true;

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
