package cn.mina.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class MessageDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "database";
	private static final String MESSAGE_TABLE_NAME = "message";
	private static final int MESSAGE_DATABASE_VISION = 3;

	public MessageDbHelper(Context context) {
		super(context, DATABASE_NAME, null, MESSAGE_DATABASE_VISION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String create_table = "create table " + MESSAGE_TABLE_NAME + " ("
				+ BaseColumns._ID + " integer primary key autoincrement,"
				+ MessageColumns.SEND_PERSON + " text not null,"
				+ MessageColumns.SEND_CTN + " text not null,"
				+ MessageColumns.SEND_DATE + " text not null,"
				+ MessageColumns.RECORD_PATH + " text,"
				+ MessageColumns.IFYUYIN + " BOOLEAN,"
				+ MessageColumns.RECORDTIME + " NUMBER,"
				+ MessageColumns.SEND_USERID + " text,"
				+ MessageColumns.TOPIC + " text not null)";
		db.execSQL(create_table);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME);
		onCreate(db);
	}

	public static final class MessageColumns implements BaseColumns {

		public static final String MESSAGE_TABLE_NAME = "message";

		public static final String TOPIC = "topic";
		public static final String SEND_USERID = "send_userid";
		public static final String SEND_CTN = "send_ctn";
		public static final String SEND_DATE = "send_date";
		public static final String SEND_PERSON = "send_person";

		public static final String RECORD_PATH = "record_path"; // 录音路径
		public static final String IFYUYIN = "ifyuyin"; // 是否语音
		public static final String RECORDTIME = "recordTime";
		public static final String GROUP_ID = "groupId";
		public static final String TO_ID = "toId";
		public static final String SEND_ID = "send_userid";
	}

}
