package com.timeandtidestudio.emergencybroadcast.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.timeandtidestudio.emergencybroadcast.Model.EmergencyContact;

import java.util.ArrayList;

/**
 * Created by User on 10/10/2017.
 */

public class ContactsDAO {

    private String loadContacts(Context ctx){
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

    public void deletePromotion(Context ctx, PromotionModel promotion) {
        SQLiteDatabase db = TravelPlanSQLiteHelper.getInstance(ctx).getWritableDatabase();
        String selection = TravelPlanSQLiteHelper.FIELD_PROMOTION_ID + "=?";
        String[] selectionArgs = {promotion.identifier};
        db.delete(TravelPlanSQLiteHelper.TABLE_PROMOTIONS,selection, selectionArgs);
    }

    public ArrayList<PromotionModel> convertCursorIntoPOIPromotions(Context ctx, Cursor resultCursor){
        ArrayList<PromotionModel> listPromotions = new ArrayList<>();
        while (resultCursor.moveToNext()){
            listPromotions.add(convertCursorIntoPOIPromotion(ctx, resultCursor));
        }
        return listPromotions;
    }

    public EmergencyContact convertCursorIntoPOIPromotion(Context ctx, Cursor cursor){
        EmergencyContact emergencyContact = null;
        String name =
        promotion.identifier = cursor.getString(cursor.getColumnIndex(TravelPlanSQLiteHelper.FIELD_PROMOTION_ID));
        promotion.name = cursor.getString(cursor.getColumnIndex(TravelPlanSQLiteHelper.FIELD_PROMOTION_NAME));

        POIModel poi = new POIModel();
        poi.identifier = cursor.getString(cursor.getColumnIndex(TravelPlanSQLiteHelper.FIELD_PROMOTION_POIID));
        poi.name = cursor.getString(cursor.getColumnIndex(TravelPlanSQLiteHelper.FIELD_PROMOTION_POINAME));
        promotion.poi = poi;

        /*
        String poiID = cursor.getString(cursor.getColumnIndex(TravelPlanSQLiteHelper.FIELD_PROMOTION_POIID));
        String poiSelection = TravelPlanSQLiteHelper.FIELD_DOWNLOADED_POI_ID + "=?";
        String[] poiSelectionArgs = {poiID};
        POIModel poi = new DownloadedPOIsDAO().getPOIs(ctx, poiSelection, poiSelectionArgs).get(0);
        promotion.poi = poi;
        */

        String categoryID = cursor.getString(cursor.getColumnIndex(TravelPlanSQLiteHelper.FIELD_PROMOTION_CATEGORY));
        String categorySelection = TravelPlanSQLiteHelper.FIELD_PROMOTIONCATEGORY_ID + "=?";
        String[] categorySelectionArgs = {categoryID};
        PromotionCategoryModel category = new PromotionCategoriesDAO().getPromotionCategories(ctx, categorySelection, categorySelectionArgs).get(0);
        promotion.category = category;

        String cityID = cursor.getString(cursor.getColumnIndex(TravelPlanSQLiteHelper.FIELD_PROMOTION_CITY));
        String citySelection = TravelPlanSQLiteHelper.FIELD_CITIES_ID + "=?";
        String[] citySelectionArgs = {cityID};
        CityModel city = new CitiesDAO().getCities(ctx, citySelection, citySelectionArgs).get(0);
        promotion.city = city;

        promotion.description = cursor.getString(cursor.getColumnIndex(TravelPlanSQLiteHelper.FIELD_PROMOTION_DESCRIPTION));
        promotion.startDate = DateTimeConverter.convertToDate(cursor.getString(cursor.getColumnIndex(TravelPlanSQLiteHelper.FIELD_PROMOTION_STARTDATE)),DateTimeConverter.DATE_FORMAT);
        promotion.endDate = DateTimeConverter.convertToDate(cursor.getString(cursor.getColumnIndex(TravelPlanSQLiteHelper.FIELD_PROMOTION_ENDDATE)),DateTimeConverter.DATE_FORMAT);
        promotion.picture = cursor.getString(cursor.getColumnIndex(TravelPlanSQLiteHelper.FIELD_PROMOTION_PICTURE));
        promotion.imageIdentifier = cursor.getString(cursor.getColumnIndex(TravelPlanSQLiteHelper.FIELD_PROMOTION_IMAGEIDENTIFIER));
        return promotion;
    }

}
