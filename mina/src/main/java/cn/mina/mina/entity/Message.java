package cn.mina.mina.entity;

import android.graphics.Bitmap;

/**
 * 消息pojo
 * 
 * @author Administrator
 * 
 */
public class Message {
	private int id;
	private String topic;
	private String send_ctn;
	private String send_person;
	private String send_date;
	private String groupId = "";
	private String toId = "";
	private String record_path;
	private boolean ifyuyin = false; // 是否是语音消息
	private long recordTime; // 语音消息持续的时间
	private Bitmap bitmap;

	public int getSend_userid() {
		return send_userid;
	}

	public void setSend_userid(int send_userid) {
		this.send_userid = send_userid;
	}

	private int send_userid;

	
	
	@Override
	public String toString() {
		return "Message [id=" + id + ", topic=" + topic + ", send_ctn="
				+ send_ctn + ", send_person=" + send_person + ", send_date="
				+ send_date + ", groupId=" + groupId + ", toId=" + toId
				+ ", record_path=" + record_path + ", ifyuyin=" + ifyuyin
				+ ", recordTime=" + recordTime + ", bitmap=" + bitmap
				+ ", send_userid=" + send_userid + "]";
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String _topic) {
		this.topic = _topic;
	}

	public String getSend_ctn() {
		return send_ctn;
	}

	public void setSend_ctn(String send_ctn) {
		this.send_ctn = send_ctn;
	}

	public String getSend_person() {
		return send_person;
	}

	public void setSend_person(String send_person) {
		this.send_person = send_person;
	}

	public String getSend_date() {
		return send_date;
	}

	public void setSend_date(String send_date) {
		this.send_date = send_date;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Message(String send_ctn, String send_person, String send_date,
			Bitmap bitmap) {
		super();
		this.send_ctn = send_ctn;
		this.send_person = send_person;
		this.send_date = send_date;
		this.bitmap = bitmap;
	}

	public Message(int _id, String send_ctn, String send_person,
			String send_date) {
		super();
		this.id = _id;
		this.send_ctn = send_ctn;
		this.send_person = send_person;
		this.send_date = send_date;
	}

	public Message() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRecord_path() {
		return record_path;
	}

	public void setRecord_path(String record_path) {
		this.record_path = record_path;
	}

	public boolean isIfyuyin() {
		return ifyuyin;
	}

	public void setIfyuyin(boolean ifyuyin) {
		this.ifyuyin = ifyuyin;
	}

	public long getRecordTime() {
		return recordTime;
	}

	public void setRecordTime(long recordTime) {
		this.recordTime = recordTime;
	}

}
