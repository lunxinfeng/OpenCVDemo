package cn.mina.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.mina.bean.User;
import cn.mina.constant.ContentFlag;
import cn.mina.constant.PublicClass;
import cn.mina.db.MessageDbHelper.MessageColumns;
import cn.mina.mina.entity.Message;
import cn.mina.util.IhandleMessge;
import cn.mina.util.PublicUtil;
import cn.mina.util.StreamTool;

public class MessageService {
	private Context context;
	private User user;
	private Socket socket;
	private DataOutputStream output;
	private DataInputStream input;
	private Map<Integer, Bitmap> imgMap = new HashMap<Integer, Bitmap>(); // 缓存在线用户头像数据

	private String filename;
	private String data;
	private static IhandleMessge handle;

	private MessageDbHelper messageDbHelpser;
	private SQLiteDatabase db;
	private String columns[] = { MessageColumns._ID, MessageColumns.SEND_CTN,
			MessageColumns.SEND_DATE, MessageColumns.SEND_PERSON,
			MessageColumns.SEND_USERID, MessageColumns.RECORD_PATH,
			MessageColumns.IFYUYIN, MessageColumns.RECORDTIME,
			MessageColumns.TOPIC };

	public MessageService(Context context) {
		this.context = context;
		messageDbHelpser = new MessageDbHelper(this.context);

	}

	/**
	 * 查询历史记录信息
	 */
	public List<Message> queryMessage(String strTopic, int pageSize,
									  int pageIndex) {
		db = messageDbHelpser.getWritableDatabase();

		String sqlStr = "select * from " + MessageColumns.MESSAGE_TABLE_NAME
				+ " where " + MessageColumns.TOPIC + "='" + strTopic + "'"
				+ " order by " + MessageColumns.SEND_DATE + " desc limit "
				+ String.valueOf(pageSize) + " offset "
				+ String.valueOf(pageSize * (pageIndex - 1));

		Cursor cursor = db.rawQuery(sqlStr, new String[] {});

		List<Message> list = new ArrayList<Message>();
		while (cursor.moveToNext()) {
			String send_ctn = cursor.getString(cursor
					.getColumnIndex(MessageColumns.SEND_CTN));
			String send_person = cursor.getString(cursor
					.getColumnIndex(MessageColumns.SEND_PERSON));
			String send_date = cursor.getString(cursor
					.getColumnIndex(MessageColumns.SEND_DATE));
			int send_userid = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(MessageColumns.SEND_USERID)));
			String record_path = cursor.getString(cursor
					.getColumnIndex(MessageColumns.RECORD_PATH));
			String ifyuyin = cursor.getString(cursor
					.getColumnIndex(MessageColumns.IFYUYIN));
			String recordTime = cursor.getString(cursor
					.getColumnIndex(MessageColumns.RECORDTIME));
			String topic = cursor.getString(cursor
					.getColumnIndex(MessageColumns.TOPIC));

			int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
			Message message = new Message(id, send_ctn, send_person, send_date);

			if (ifyuyin.equals("1")) {
				ifyuyin = "true";
			} else {
				ifyuyin = "false";
			}
			message.setIfyuyin(Boolean.parseBoolean(ifyuyin));
			message.setId(send_userid);
			message.setRecord_path(record_path);
			message.setRecordTime(Long.parseLong(recordTime));
			message.setSend_userid(send_userid);
			list.add(message);
		}
		cursor.close();
		return list;
	}

	public void deleteAllMessage() {
		db.beginTransaction();
		try {
			db = messageDbHelpser.getWritableDatabase();
			db.delete(MessageColumns.MESSAGE_TABLE_NAME, null, null);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	public void deleteMessage(String send_ctn,String send_data ) {

		try {
			db = messageDbHelpser.getWritableDatabase();
			db.beginTransaction();
			db.delete(MessageColumns.MESSAGE_TABLE_NAME,
					MessageColumns.SEND_CTN +"=? and " + MessageColumns.SEND_DATE+"=?",
					new String[] {send_ctn,send_data});
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * 插入消息记录
	 * 
	 * @throws FileNotFoundException
	 */
	public long insertMessage(Message message) {
		db = messageDbHelpser.getWritableDatabase();

		// 判断类似记录是否存在
		String sqlStr = "select * from " + MessageColumns.MESSAGE_TABLE_NAME
				+ " where " + MessageColumns.SEND_USERID + "="
				+ message.getId() + " and " + MessageColumns.SEND_DATE + "='"
				+ message.getSend_date() + "'";

		Cursor cursor = db.rawQuery(sqlStr, new String[] {});
		if (cursor.getCount() > 0) {
			return -1;
		}

		ContentValues values = new ContentValues();
		// values.put(MessageColumns._ID, message.getId());
		values.put(MessageColumns.SEND_CTN, message.getSend_ctn());
		values.put(MessageColumns.SEND_PERSON, message.getSend_person());
		values.put(MessageColumns.SEND_DATE, message.getSend_date());
		values.put(MessageColumns.SEND_USERID, message.getId());
		values.put(MessageColumns.RECORD_PATH, message.getRecord_path());
		values.put(MessageColumns.IFYUYIN, message.isIfyuyin());
		values.put(MessageColumns.RECORDTIME, message.getRecordTime());
		values.put(MessageColumns.TOPIC, message.getTopic());
		values.put(MessageColumns.SEND_USERID, message.getSend_userid());
		db.beginTransaction();
		long rowId = 0;
		try {
			// db.execSQL("update "+MessageColumns.MESSAGE_TABLE_NAME+" set "+UserColumns.FALG+"='0'");
			rowId = db.insert(MessageColumns.MESSAGE_TABLE_NAME, null, values);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return rowId;
	}

	/**
	 * 建立连接
	 * 
	 * @return
	 */
	public void startConnect(User user, IhandleMessge handle)
			throws IOException {
		this.user = user;
		String ip = user.getIp();
		String port = user.getPort();
		long id = user.getId();
		try {
			SocketAddress socAddress = new InetSocketAddress(
					InetAddress.getByName(ip), Integer.parseInt(port));
			socket = new Socket();
			socket.connect(socAddress, 5 * 1000);
			output = new DataOutputStream(socket.getOutputStream());
			input = new DataInputStream(socket.getInputStream());
			// 处理用户登录
			String str = ContentFlag.ONLINE_FLAG + id;
			output.writeUTF(str);
			// 缓存其它登录者的头像数据
			int fileNums = input.readInt(); // 图片文件的数量
			for (int i = 0; i < fileNums; i++) {
				int tempId = Integer.parseInt(input.readUTF());
				byte[] datas = StreamTool.readStream(input);
				Bitmap tempImg = BitmapFactory.decodeByteArray(datas, 0,
						datas.length);
				imgMap.put(tempId, tempImg);
			}
			// 接收消息
			receiveMsg(handle);
		} catch (IOException e) {
			throw new IOException("fail connect to the server");
		}
	}

	/**
	 * 应用退出
	 */
	public void quitApp() {
		String sendStr = "";
		if (null != user) {
			sendStr = ContentFlag.OFFLINE_FLAG + this.user.getId();
		}
		if (!socket.isClosed()) {
			if (socket.isConnected()) {
				if (!socket.isOutputShutdown()) {
					try {
						output.writeUTF(sendStr);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							if (null != input)
								input.close();
							if (null != output)
								output.close();
							if (null != socket)
								socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * 接收消息
	 * 
	 * @return
	 * @throws IOException
	 */
	public void receiveMsg(IhandleMessge handle) throws IOException {
		try {
			while (true) {
				String msgCtn = input.readUTF();
				if (msgCtn.startsWith(ContentFlag.ONLINE_FLAG)) { // 处理登录消息
					String json = input.readUTF();
					Message msg = parseJsonToObject(json);
					byte[] datas = StreamTool.readStream(input);
					Bitmap bitmap = BitmapFactory.decodeByteArray(datas, 0,
							datas.length);
					msg.setBitmap(bitmap);
					handle.handleMsg(msg);
					imgMap.put(msg.getId(), bitmap);
				} else if (msgCtn.startsWith(ContentFlag.OFFLINE_FLAG)) { // 处理退出消息
					String json = input.readUTF();
					Message msg = parseJsonToObject(json);
					msg.setBitmap(imgMap.get(msg.getId()));
					handle.handleMsg(msg);
					imgMap.remove(msg.getId());
				} else if (msgCtn.startsWith(ContentFlag.RECORD_FLAG)) { // 处理语音消息
					String filename = msgCtn.substring(ContentFlag.RECORD_FLAG
							.length());
					File dir = new File(
							Environment.getExternalStorageDirectory()
									+ "/recordMsg/");
					if (!dir.exists())
						dir.mkdirs();
					File file = new File(dir, filename);
					String json = input.readUTF();
					Message msg = parseJsonToObject(json);
					msg.setRecord_path(file.getAbsolutePath());
					msg.setBitmap(imgMap.get(msg.getId()));
					msg.setIfyuyin(true);
					handle.handleMsg(msg);
					saveRecordFile(file);
				} else { // 处理普通消息
					Message msg = parseJsonToObject(msgCtn);
					msg.setBitmap(imgMap.get(msg.getId()));
					handle.handleMsg(msg);
				}
			}
		} catch (Exception e) {
			if (!socket.isClosed()) {
				throw new IOException("fail connect to the server");
			}
		}
	}

	/**
	 * 保存录音文件
	 * 
	 * @param
	 * @param
	 * @throws IOException
	 */
	private void saveRecordFile(File file) throws IOException {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			byte[] datas = StreamTool.readStream(input);
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(datas);
		}
	}

	/**
	 * 解析json字符串
	 */
	public Message parseJsonToObject(String json) {
		try {
			JSONArray arrays = new JSONArray(json);
			JSONObject jsonObject = arrays.getJSONObject(0);
			int userId = Integer.parseInt(jsonObject
					.getString(MessageColumns.SEND_USERID)); // 用户的ID
			String send_person = jsonObject
					.getString(MessageColumns.SEND_PERSON); // 发送者
			String send_ctn = jsonObject.getString(MessageColumns.SEND_CTN); // 发送内容
			String send_date = jsonObject.getString(MessageColumns.SEND_DATE); // 发送时间
			Message msg = new Message();
			msg.setId(userId);
			msg.setSend_ctn(send_ctn);
			msg.setSend_person(send_person);
			msg.setSend_date(send_date);
			if (jsonObject.has("recordTime")) {
				String recordTime = jsonObject.getString("recordTime");
				msg.setRecordTime(Long.valueOf(recordTime));
			}
			return msg;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 发送消息
	 * 
	 * @param ctn
	 * @throws IOException
	 */
	public void sendMsg(String ctn) throws Exception {
		output.writeUTF(ctn);
	}

	/**
	 * 发送语音消息
	 * 
	 * @param file
	 * @throws Exception
	 */
	public void sendRecordMsg(File file, long recordTime) throws Exception {
		FileInputStream inputStream = null;
		try {

			inputStream = new FileInputStream(file);
			byte[] buffer = new byte[(int) file.length() + 100];
			int length = inputStream.read(buffer);
			data = Base64.encodeToString(buffer, 0, length, Base64.DEFAULT);
			filename = file.getName();

			// 上传信息到服务器【暂存于数据库】
			uploadFile();

		} catch (Exception e) {
			throw new Exception();
		} finally {
			try {
				if (file != null)
					file.delete();
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 上传语音信息到服务器
	private void uploadFile() {

		// 再次启动新线程获取文档按钮状态
		new Thread() {
			@Override
			public void run() {
				try {
					String msg = "{\"root\":[{\"filename\":\"" + filename
							+ "\",\"bytestr\":\"" + data + "\"}]}";
					String retObj = PublicUtil.HttpSend(
							PublicClass.METHOD_NAME_UPDATE, "0002", msg, "-");
					try {
						String retStr = String.valueOf(retObj);

						if (retStr.equals("true")) {

						}

					} catch (Exception e) {
						// myHandler.sendEmptyMessage(99);
						e.printStackTrace();
					}

				} catch (Exception e) {

				}
			}
		}.start();
	}

}
