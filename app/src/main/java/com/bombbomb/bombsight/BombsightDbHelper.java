package com.bombbomb.bombsight;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by cos-mbp-don on 3/22/17.
 */

public class BombsightDbHelper extends SQLiteOpenHelper {


    private static BombsightDbHelper instance;
    private Context appContext;
    private String createLocationTable;

    public static final String DATABASE_NAME = "bombsiteDb";
    public static final int DATABASE_VERSION = 1;

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



    public void addOrUpdateLocation(BombsightLocation location){

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try{
            ContentValues values = new ContentValues();
            values.put(LOCATION_LAT, location.latitude);
            values.put(LOCATION_LON, location.latitude);
            values.put(LOCATION_TYPE, location.latitude);
            values.put(LOCATION_FORMATTED_ADDRESS, location.latitude);
            values.put(LOCATION_STREET_NUMBER, location.latitude);
            values.put(LOCATION_ROUTE, location.latitude);
            values.put(LOCATION_NEIGHBORHOOD, location.latitude);
            values.put(LOCATION_POSTAL_CODE, location.latitude);
            values.put(LOCATION_POSTAL_CODE_SUFFIX, location.latitude);
            values.put(LOCATION_CITY, location.latitude);
            values.put(LOCATION_COUNTY, location.latitude);
            values.put(LOCATION_STATE, location.latitude);
            values.put(LOCATION_COUNTRY, location.latitude);


            int rows = db.update(TABLE_LOCATION, values, LOCATION_ID + "= ?", new String[]{location.id.toString()});

            if (rows != 1){
                db.insertOrThrow(TABLE_LOCATION, null, values);
                db.setTransactionSuccessful();
            }

        } catch (Exception ex){

        } finally {
            db.endTransaction();
        }


    }


}
