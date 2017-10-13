package com.timeandtidestudio.emergencybroadcast.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.timeandtidestudio.emergencybroadcast.Model.EmergencyContact;
import com.timeandtidestudio.emergencybroadcast.Model.SentMessage;

import java.util.ArrayList;

/**
 * Created by User on 10/10/2017.
 */

public class MessagesDAO {

    private ArrayList<SentMessage> loadMessages(Context ctx){
        ArrayList<SentMessage> messages = new ArrayList<>();
        SQLiteDatabase db = EBSQLiteHelper.getInstance(ctx).getReadableDatabase();
        Cursor resultCursor = db.query(EBSQLiteHelper.TABLE_MESSAGES, null, null, null, null, null, null);
        resultCursor.moveToFirst();
        messages = convertCursorIntoSentMessages(ctx,resultCursor);
        db.close();
        return messages;
    }

    public void saveSentMessage(Context ctx, SentMessage sentMessage){
        SQLiteDatabase db = EBSQLiteHelper.getInstance(ctx).getWritableDatabase();
        ContentValues cv = new ContentValues();
        if(sentMessage.id!=0){
            cv.put(EBSQLiteHelper.FIELD_MESSAGES_ID, sentMessage.id);
        }
        cv.put(EBSQLiteHelper.FIELD_MESSAGES_CONTENT, sentMessage.message);
        cv.put(EBSQLiteHelper.FIELD_MESSAGES_TIMESTAMP, sentMessage.timestamp);
        db.insertWithOnConflict(EBSQLiteHelper.TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void deleteSentMessage(Context ctx, SentMessage sentMessage) {
        SQLiteDatabase db = EBSQLiteHelper.getInstance(ctx).getWritableDatabase();
        String selection = EBSQLiteHelper.FIELD_MESSAGES_ID + "= ?";
        String[] selectionArgs = {""+sentMessage.id};
        db.delete(EBSQLiteHelper.TABLE_MESSAGES,selection, selectionArgs);
    }

    public ArrayList<SentMessage> convertCursorIntoSentMessages(Context ctx, Cursor resultCursor){
        ArrayList<SentMessage> contacts = new ArrayList<>();
        while (resultCursor.moveToNext()){
            contacts.add(convertCursorIntoSentMessage(ctx, resultCursor));
        }
        return contacts;
    }

    public SentMessage convertCursorIntoSentMessage(Context ctx, Cursor cursor){
        SentMessage sentMessage = null;
        int id = cursor.getInt(cursor.getColumnIndex(EBSQLiteHelper.FIELD_MESSAGES_ID));
        String timestamp = cursor.getString(cursor.getColumnIndex(EBSQLiteHelper.FIELD_MESSAGES_TIMESTAMP));
        String content = cursor.getString(cursor.getColumnIndex(EBSQLiteHelper.FIELD_MESSAGES_CONTENT));
        sentMessage = new SentMessage(id,timestamp,content);
        return sentMessage;
    }

}
