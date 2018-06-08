package cn.mina.util;

import android.os.Handler;
import android.util.Xml;

import org.apache.mina.core.session.IoSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import cn.mina.constant.PublicClass;
import cn.mina.mina.entity.MsgPack;

public class PublicUtil {
    public static boolean isPause = false; // 是否按下home键

    public static boolean MianSend(int Method, int gameid, int otherUser,
                                   String msg, IoSession clientSession) {
        MsgPack msgPack = new MsgPack();
        try {
            msgPack.setMsgLength(msg.getBytes("UTF-8").length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        msgPack.setMsgMethod(Method); // 主动认输
        msgPack.setMsgGroupId(gameid); // 默认群聊
        msgPack.setMsgToID(otherUser); // 默认群聊
        msgPack.setMsgPack(msg);
        try {
            if (null != clientSession) {
                if (clientSession.isConnected()) {
                    clientSession.write(msgPack);
                    return true;
                }
            }
        } catch (Exception error) {
            System.out.println(error.getMessage());
            return false;
        }
        return false;
    }

    public static String HttpSend3(String type, String num, String msg,
                                   String userId) {
        String resStr = "";
        try {
            SoapObject request = new SoapObject(PublicClass.NAMESPACE, type);
            request.addProperty("arg0", num);
            request.addProperty("arg1", msg);
            request.addProperty("arg2", "0394cb6d-2575-4f73-92e9-6d402c39e20c");
            request.addProperty("arg3", userId);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.bodyOut = request;
            envelope.setOutputSoapObject(request); // 等价于envelope.bodyOut
            // envelope.encodingStyle = SoapSerializationEnvelope.ENV;
            (new MarshalBase64()).register(envelope);

            HttpTransportSE androidHttpTransport = new HttpTransportSE(PublicClass.URL,
                    10000);// 超过10000秒断开
            androidHttpTransport.debug = true;
            androidHttpTransport.call(PublicClass.SOAP_ACTION, envelope);

            Object result = envelope.getResponse();
            resStr = String.valueOf(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("erroe----=" + e.toString());
            return "ExceptionError";
        }
        return resStr;
    }

    public static String HttpSend(String type, String num, String msg,
                                  String userId) {

        String data = "";

        String requestData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://webservices.izis.cn/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<SOAP-ENV:Body>"
                + "<ns1:"+type+">"
                + "<arg0 xsi:type=\"xsd:string\">"
                + num
                + "</arg0>"
                + "<arg1 xsi:type=\"xsd:string\">"
                + msg
                + "</arg1>"
                + "<arg2 xsi:type=\"xsd:string\">0394cb6d-2575-4f73-92e9-6d402c39e20c</arg2>"
                + "<arg3 xsi:type=\"xsd:string\">" + userId + "</arg3>"
                + "</ns1:"+type+">"
                + "</SOAP-ENV:Body>"
                + "</SOAP-ENV:Envelope>";
        System.out.println(Thread.currentThread()+"public");
        try {
            URL url = new URL(PublicClass.URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(3000);
            byte[] bytes = requestData.getBytes("utf-8");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
            con.setRequestProperty("SOAPAction", PublicClass.SOAP_ACTION);
            con.setRequestProperty("Content-Length", "" + bytes.length);
            OutputStream outStream = con.getOutputStream();
            outStream.write(bytes);
            outStream.flush();
            outStream.close();
            InputStream inStream = con.getInputStream();

            data = parser(inStream);

            inStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            data = "IOException";
        }catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            data = "ExceptionError";
        }
        return data;
    }
    /**
     *
     * @param type 数据类型  PublicClass.METHOD_NAME(获取数据)   PublicClass.METHOD_NAME_UPDATE(更新数据)
     * @param num 访问方法编码
     * @param msg 需要提供的消息体
     * @param userId 访问者ID
     * @param myHandler Handler返回结果后处理的子线程
     * @param what 子线程处理编号
     */
    public static void HttpSend(String type, String num, String msg,String userId,Handler myHandler,int what){
        android.os.Message message = myHandler.obtainMessage();
        String data = "";
        String temporary ="";
        String requestData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://webservices.izis.cn/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<SOAP-ENV:Body>"
                + "<ns1:"+type+">"
                + "<arg0 xsi:type=\"xsd:string\">"
                + num
                + "</arg0>"
                + "<arg1 xsi:type=\"xsd:string\">"
                + msg
                + "</arg1>"
                + "<arg2 xsi:type=\"xsd:string\">0394cb6d-2575-4f73-92e9-6d402c39e20c</arg2>"
                + "<arg3 xsi:type=\"xsd:string\">" + userId + "</arg3>"
                + "</ns1:"+type+">"
                + "</SOAP-ENV:Body>"
                + "</SOAP-ENV:Envelope>";
        try {
            URL url = new URL(PublicClass.URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(10000);
            byte[] bytes = requestData.getBytes("utf-8");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
            con.setRequestProperty("SOAPAction", PublicClass.SOAP_ACTION);
            con.setRequestProperty("Content-Length", "" + bytes.length);
            OutputStream outStream = con.getOutputStream();
            outStream.write(bytes);
            outStream.flush();
            outStream.close();
            InputStream inStream = con.getInputStream();
            temporary = parser(inStream);
            JSONObject jsonresult = new JSONObject(temporary);
            JSONArray array = jsonresult.getJSONArray("root");
            JSONObject JsonObj = array.getJSONObject(0);
            if(JsonObj.has("successful")){
                message.what = what;
                message.obj = JsonObj.has("successful");
                myHandler.sendMessage(message);
            }else if(JsonObj.has("failure")){
                message.what = 99;
                message.obj = JsonObj.has("failure");
                myHandler.sendMessage(message);
            }else{
                message.what = what;
                message.obj = temporary;
                myHandler.sendMessage(message);
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            data="URL_Exception";
            message.what = 99;
            message.obj = data;
            myHandler.sendMessage(message);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            data="UnsupportedEncoding_Exception";
            message.what = 99;
            message.obj = data;
            myHandler.sendMessage(message);
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            data="Protocol_Exception";
            message.what = 99;
            message.obj = data;
            myHandler.sendMessage(message);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            data="TimeOut_Exception";
            message.what = 99;
            message.obj = data;
            myHandler.sendMessage(message);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            message.what = what;
            message.obj = temporary;
            myHandler.sendMessage(message);
        }

    }


    private static String parser(InputStream in) {
        XmlPullParser parser = Xml.newPullParser();
        String data = "";
        try {
            int flag = 0;
            parser.setInput(in, "utf-8");
            int evenType = parser.getEventType();
            while (evenType != XmlPullParser.END_DOCUMENT) {
                switch (evenType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        data = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                parser.next();
                evenType = parser.getEventType();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

}
