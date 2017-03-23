package com.bombbomb.bombsight;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by cos-mbp-don on 3/22/17.
 */

public class BombsightDbHelper extends SQLiteOpenHelper {


    private static BombsightDbHelper instance;
    private Context appContext;
    private String createLocationTable;

    public static final String DATABASE_NAME = "bombsiteDb";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_LOCATION = "LOCATION";
    public static final String LOCATION_ID = "ID";
    public static final String LOCATION_LAT = "LAT";
    public static final String LOCATION_LON = "LON";
    public static final String LOCATION_TYPE = "TYPE";
    public static final String LOCATION_FORMATTED_ADDRESS = "FORMATTED_ADDRESS";
    public static final String LOCATION_STREET_NUMBER = "STREET_NUMBER";
    public static final String LOCATION_ROUTE = "ROUTE";
    public static final String LOCATION_NEIGHBORHOOD = "NEIGHBORHOOD";
    public static final String LOCATION_POSTAL_CODE= "POSTAL_CODE";
    public static final String LOCATION_POSTAL_CODE_SUFFIX = "POSTAL_CODE_SUFFIX";
    public static final String LOCATION_CITY = "CITY";
    public static final String LOCATION_COUNTY = "COUNTY";
    public static final String LOCATION_STATE = "STATE";
    public static final String LOCATION_COUNTRY = "COUNTRY";


    public static synchronized BombsightDbHelper getInstance(Context context){

        if (instance == null){
            instance = new BombsightDbHelper(context.getApplicationContext());
        }
        return instance;

    }

    private BombsightDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.appContext = context;
        this.createLocationTable = context.getString(R.string.location_table_create_script);


    }



    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(this.createLocationTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(BombsightDbHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);

        onCreate(database);
    }



    public BombsightLocation addOrUpdateLocation(BombsightLocation location){

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try{
            ContentValues values = new ContentValues();

            if (location.latitude != null)
                values.put(LOCATION_LAT, location.latitude);

            if (location.longitude != null)
                values.put(LOCATION_LON, location.longitude);

            if (location.type != null)
                values.put(LOCATION_TYPE, location.type);

            if (location.formattedAddress != null)
                values.put(LOCATION_FORMATTED_ADDRESS, location.formattedAddress);

            if (location.streetNumber != null)
                values.put(LOCATION_STREET_NUMBER, location.streetNumber);

            if (location.route != null)
                values.put(LOCATION_ROUTE, location.route);

            if (location.neighborhood != null)
                values.put(LOCATION_NEIGHBORHOOD, location.neighborhood);

            if (location.postCode != null)
                values.put(LOCATION_POSTAL_CODE, location.postCode);

            if (location.postCodeSuffix != null)
                values.put(LOCATION_POSTAL_CODE_SUFFIX, location.postCodeSuffix);

            if (location.city != null)
                values.put(LOCATION_CITY, location.city);

            if (location.county != null)
                values.put(LOCATION_COUNTY, location.county);

            if (location.state != null)
                values.put(LOCATION_STATE, location.state);

            if (location.country != null)
                values.put(LOCATION_COUNTRY, location.country);

            Integer rows = null;
            if (location.id != null)
                rows = db.update(TABLE_LOCATION, values, LOCATION_ID + "= ?", new String[]{location.id.toString()});
            else
                rows = -1;

            if (rows != 1){
                Long rowid = db.insertOrThrow(TABLE_LOCATION, null, values);
                location.id = rowid.intValue();
                db.setTransactionSuccessful();
            }

        } catch (Exception ex){
            String message = ex.getMessage();

        } finally {
            db.endTransaction();
        }


        return location;
    }

    public Map<Integer, BombsightLocation> getAllLocations(){
        Map<Integer, BombsightLocation> locations = new TreeMap<>();

        String selectVideoSql = String.format("Select * From %s", TABLE_LOCATION);

        SQLiteDatabase db = getReadableDatabase();
        String path = db.getPath();

        Cursor cursor = db.rawQuery(selectVideoSql, null);
        try {
            if (cursor.moveToFirst()) {
                do {

                    BombsightLocation location = new BombsightLocation();
                    location.id = cursor.getInt(cursor.getColumnIndex(LOCATION_ID));

                    location.latitude = cursor.getDouble(cursor.getColumnIndex(LOCATION_LAT));
                    location.longitude = cursor.getDouble(cursor.getColumnIndex(LOCATION_LON));
                    location.type = cursor.getInt(cursor.getColumnIndex(LOCATION_TYPE));
                    location.formattedAddress = cursor.getString(cursor.getColumnIndex(LOCATION_FORMATTED_ADDRESS));
                    location.streetNumber = cursor.getString(cursor.getColumnIndex(LOCATION_STREET_NUMBER));
                    location.route = cursor.getString(cursor.getColumnIndex(LOCATION_ROUTE));
                    location.neighborhood = cursor.getString(cursor.getColumnIndex(LOCATION_NEIGHBORHOOD));
                    location.postCode = cursor.getString(cursor.getColumnIndex(LOCATION_POSTAL_CODE));
                    location.postCodeSuffix = cursor.getString(cursor.getColumnIndex(LOCATION_POSTAL_CODE_SUFFIX));
                    location.city = cursor.getString(cursor.getColumnIndex(LOCATION_CITY));
                    location.county = cursor.getString(cursor.getColumnIndex(LOCATION_COUNTY));
                    location.state = cursor.getString(cursor.getColumnIndex(LOCATION_STATE));
                    location.country = cursor.getString(cursor.getColumnIndex(LOCATION_COUNTRY));
                    locations.put(location.id, location);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return locations;
    }


}
