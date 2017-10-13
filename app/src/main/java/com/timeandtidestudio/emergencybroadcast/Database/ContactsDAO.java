package com.timeandtidestudio.emergencybroadcast.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.timeandtidestudio.emergencybroadcast.Model.EmergencyContact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by User on 10/10/2017.
 */

public class ContactsDAO {

    public ArrayList<EmergencyContact> loadContacts(Context ctx){
        ArrayList<EmergencyContact> contacts = new ArrayList<>();
        SQLiteDatabase db = EBSQLiteHelper.getInstance(ctx).getReadableDatabase();
        Cursor resultCursor = db.query(EBSQLiteHelper.TABLE_CONTACTS, null, null, null, null, null, null);
        resultCursor.moveToFirst();
        contacts = convertCursorIntoContacts(ctx,resultCursor);
        Collections.sort(contacts);
        db.close();
        return contacts;
    }

    public void saveEmergencyContact(Context ctx, EmergencyContact contact){
        SQLiteDatabase db = EBSQLiteHelper.getInstance(ctx).getWritableDatabase();
        ContentValues cv = new ContentValues();
        if(contact.id!=0){
            cv.put(EBSQLiteHelper.FIELD_CONTACTS_ID, contact.id);
        }
        cv.put(EBSQLiteHelper.FIELD_CONTACTS_NAME, contact.name);
        cv.put(EBSQLiteHelper.FIELD_CONTACTS_PHONE, contact.phone);
        db.insertWithOnConflict(EBSQLiteHelper.TABLE_CONTACTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void deleteEmergencyContact(Context ctx, EmergencyContact contact) {
        SQLiteDatabase db = EBSQLiteHelper.getInstance(ctx).getWritableDatabase();
        String selection = EBSQLiteHelper.FIELD_CONTACTS_NAME + "LIKE ? AND " + EBSQLiteHelper.FIELD_CONTACTS_PHONE + "LIKE ?";
        String[] selectionArgs = {contact.name,contact.phone};
        db.delete(EBSQLiteHelper.TABLE_CONTACTS,selection, selectionArgs);
    }

    public ArrayList<EmergencyContact> convertCursorIntoContacts(Context ctx, Cursor resultCursor){
        ArrayList<EmergencyContact> contacts = new ArrayList<>();
        while (resultCursor.moveToNext()){
            contacts.add(convertCursorIntoEmergencyContact(ctx, resultCursor));
        }
        return contacts;
    }

    public EmergencyContact convertCursorIntoEmergencyContact(Context ctx, Cursor cursor){
        EmergencyContact emergencyContact = null;
        int id = cursor.getInt(cursor.getColumnIndex(EBSQLiteHelper.FIELD_CONTACTS_ID));
        String name = cursor.getString(cursor.getColumnIndex(EBSQLiteHelper.FIELD_CONTACTS_NAME));
        String number = cursor.getString(cursor.getColumnIndex(EBSQLiteHelper.FIELD_CONTACTS_PHONE));
        emergencyContact = new EmergencyContact(id,name,number);
        return emergencyContact;
    }

}
