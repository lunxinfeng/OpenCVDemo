package cn.mina.mina.entity;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ChatMsg {

	

	
	public Message parseXMLtoMessage(String xml) {
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Element root = doc.getRootElement();
		Element client = root.element("info");
		Message message = new Message();

		message.setId(Integer.parseInt(client.elementText("id")));
		message.setSend_ctn(client.elementText("c"));		// content
		message.setSend_person(client.elementText("u"));    // username
		message.setSend_date(client.elementText("t"));		// send_time
	

		return message;
	}
	
	
	public static Message parseJsonToObject(String json) {
		try {
			JSONArray arrays = new JSONArray(json);
			JSONObject jsonObject = arrays.getJSONObject(0);
			// int userId = Integer.parseInt(jsonObject
			// .getString(MessageColumns.ID));
			String userid = jsonObject.getString("id"); 
			String send_person = jsonObject.getString("send_person"); 
			String send_ctn = jsonObject.getString("send_ctn"); 
			String send_date = jsonObject.getString("send_date"); 
			Message msg = new Message();
			msg.setId(Integer.parseInt(userid));
			msg.setSend_ctn(send_ctn);
			msg.setSend_person(send_person);
			msg.setSend_date(send_date);
			if (jsonObject.has("recordTime")) {
				String recordTime = jsonObject.getString("recordTime");
				msg.setRecordTime(Long.valueOf(recordTime));
				msg.setIfyuyin(true);
			}
			if (jsonObject.has("groupId")) {
				String groupId = jsonObject.getString("groupId");
				msg.setGroupId(groupId);
			}
			if (jsonObject.has("toId")) {
				String toId = jsonObject.getString("toId");
				msg.setToId(toId);
			}
			if (jsonObject.has("send_userid")) {
				 int toId = jsonObject.getInt("send_userid");
				msg.setSend_userid(toId);
			}else{
				msg.setSend_userid(0);
			}
			return msg;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	public String createDocument(String userid, String username,
			String content, String send_time) {
		String message = "";
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("Name");
		root.addAttribute("msgname", "Message");
		Element client = root.addElement("info");
		Element id = client.addElement("id");
		Element u = client.addElement("u");
		Element c = client.addElement("c");
		Element t = client.addElement("t");
		id.addText(userid);
		u.addText(username);
		c.addText(content);
		t.addText(send_time);
		message = document.asXML();

		return message;
	}
}
