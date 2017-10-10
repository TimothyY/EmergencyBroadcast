package com.timeandtidestudio.emergencybroadcast.Database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by W8 on 12/14/2015.
 */
public class EBSQLiteHelper extends SQLiteOpenHelper{

    private static EBSQLiteHelper sInstance;

    public static final String DB_NAME = "emergency_broadcast_db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_CONTACTS = "CONTACTS";
    public static final String FIELD_CONTACTS_ID = "contactID";
    public static final String FIELD_CONTACTS_NAME = "contactName";
    public static final String FIELD_CONTACTS_PHONE = "contactPhone";

    public static final String TABLE_MESSAGES = "MESSAGES";
    public static final String FIELD_MESSAGES_ID = "messageID";
    public static final String FIELD_MESSAGES_CONTENT = "messageContent";
    public static final String FIELD_MESSAGES_TIMESTAMP = "messageTimestamp";

    public static final String TABLE_DRAFTEDMESSAGE = "DRAFTEDMESSAGE";
    public static final String FIELD_DRAFTEDMESSAGE_ID = "messageID";
    public static final String FIELD_DRAFTEDMESSAGE_CONTENT = "messageContent";

    public static synchronized EBSQLiteHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new EBSQLiteHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private EBSQLiteHelper(Context context) {
        super(context,DB_NAME, null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createContactsQStr="CREATE TABLE IF NOT EXISTS '"+TABLE_CONTACTS+"' (\n" +
                "\t'"+FIELD_CONTACTS_ID +"'\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t'"+FIELD_CONTACTS_NAME+"'\tTEXT,\n" +
                "\t'"+FIELD_CONTACTS_PHONE+"'\tTEXT\n" +
                ");";
        db.execSQL(createContactsQStr);

        String createMessagesQStr="CREATE TABLE IF NOT EXISTS '"+TABLE_MESSAGES+"' (\n" +
                "\t'"+FIELD_MESSAGES_ID +"'\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t'"+FIELD_MESSAGES_CONTENT+"'\tTEXT,\n" +
                "\t'"+FIELD_MESSAGES_TIMESTAMP+"'\tTEXT\n" +
                ");";
        db.execSQL(createMessagesQStr);

        String createDraftedMessageQStr="CREATE TABLE IF NOT EXISTS '"+TABLE_DRAFTEDMESSAGE+"' (\n" +
                "\t'"+FIELD_MESSAGES_ID +"'\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t'"+FIELD_MESSAGES_CONTENT+"'\tTEXT,\n" +
                "\t'"+FIELD_MESSAGES_TIMESTAMP+"'\tTEXT\n" +
                ");";
        db.execSQL(createDraftedMessageQStr);

        ContentValues cvDraftedMessage = new ContentValues();
        cvDraftedMessage.put(EBSQLiteHelper.FIELD_DRAFTEDMESSAGE_ID, 1);
        cvDraftedMessage.put(EBSQLiteHelper.FIELD_DRAFTEDMESSAGE_CONTENT, "");
        db.insertWithOnConflict(EBSQLiteHelper.TABLE_DRAFTEDMESSAGE, null, cvDraftedMessage, SQLiteDatabase.CONFLICT_REPLACE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String deleteDownloadedTourPackageQStr = "DROP TABLE IF EXISTS "+DB_NAME+"."+TABLE_DRAFTEDMESSAGE;
        db.execSQL(deleteDownloadedTourPackageQStr);
        String deleteMessagesQStr = "DROP TABLE IF EXISTS "+DB_NAME+"."+TABLE_MESSAGES;
        db.execSQL(deleteMessagesQStr);
        String deleteContactsQStr = "DROP TABLE IF EXISTS "+DB_NAME+"."+TABLE_CONTACTS;
        db.execSQL(deleteContactsQStr);
        onCreate(db);
    }
}
