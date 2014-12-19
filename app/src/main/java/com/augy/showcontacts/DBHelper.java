package com.augy.showcontacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by adarshpandey on 12/19/14.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_CONTACT = "contacts.db";
    private static final int DB_VERSION = 4;

    public static final String TABLE_CONTACT = "contact";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DISPLAY_NAME = "display_name";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_CONTACT + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_DISPLAY_NAME
            + " VARCHAR, "+COLUMN_PHONE_NUMBER+" VARCHAR);";

    public DBHelper(Context context) {
        super(context, DB_CONTACT, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
        onCreate(db);

    }

    public long addContact(Contact contact) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DISPLAY_NAME, contact.displayName);
        values.put(COLUMN_PHONE_NUMBER, contact.phoneNumber);

        return database.insert(TABLE_CONTACT, null, values);
    }

    public Cursor getContacts(String queryString) {
        SQLiteDatabase database = this.getReadableDatabase();

        StringBuilder mainQuery = new StringBuilder();
        mainQuery.append("Select * from ").append(TABLE_CONTACT);

        if (!TextUtils.isEmpty(queryString)) {
            StringBuilder whereClause = new StringBuilder();
            whereClause.append(COLUMN_DISPLAY_NAME).append(" like '%")
                    .append(queryString).append("%'").append(" or ")
                    .append(COLUMN_PHONE_NUMBER).append(" like '%").append(queryString).append("%'");

            mainQuery.append(" where ").append(whereClause);
        }


       /* String query = "Select * from " + TABLE_CONTACT + " where " + COLUMN_DISPLAY_NAME
                + " like '%"+queryString+"%'" + " or " + COLUMN_PHONE_NUMBER +" like '%"+queryString+"%'";
*/
        return database.rawQuery(mainQuery.toString(), null);
    }
}
