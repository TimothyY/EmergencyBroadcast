package com.timeandtidestudio.emergencybroadcast.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by User on 10/10/2017.
 */

public class DraftedMessageDAO {
    
    private String loadDraftedMessage(Context ctx){
        String draftedMessage = null;
        SQLiteDatabase db = EBSQLiteHelper.getInstance(ctx).getReadableDatabase();
        Cursor resultCursor = db.query(EBSQLiteHelper.TABLE_DRAFTEDMESSAGE, null, null, null, null, null, null);
        resultCursor.moveToFirst();
        draftedMessage = convertCursorIntoDraftedMessage(resultCursor);
        db.close();
        return draftedMessage;
    }

    public void saveDraftedMessage(Context ctx, String draftedMessage){
        SQLiteDatabase db = EBSQLiteHelper.getInstance(ctx).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(EBSQLiteHelper.FIELD_DRAFTEDMESSAGE_ID, 1);
        cv.put(EBSQLiteHelper.FIELD_DRAFTEDMESSAGE_CONTENT, draftedMessage);
        db.insertWithOnConflict(EBSQLiteHelper.TABLE_DRAFTEDMESSAGE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public String convertCursorIntoDraftedMessage(Cursor cursor){
        String draftedMessage = null;
        if(cursor.moveToFirst()) {
            draftedMessage = cursor.getString(cursor.getColumnIndex(EBSQLiteHelper.FIELD_DRAFTEDMESSAGE_CONTENT));
        }
        return draftedMessage;
    }

}
