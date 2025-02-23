package com.example.kusano.kaimonomemo2018;

        import java.util.Date;
        import java.lang.String;

        import android.R.integer;
        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.util.Log;

public class DBAdapter
{
    static final String DATABASE_NAME = "mynote.db";
    static final int DATABASE_VERSION = 3;

    public static final String TABLE_NAME = "notes";
    public static final String COL_ID = "_id";
    public static final String COL_NOTE = "note";
    public static final String COL_LASTUPDATE = "lastupdate";

    //2015.3.8 追加
    public static final String COL_PRIORITY = "priority";
    public static final String COL_NUMBER = "number";
    public static final String COL_SHOP = "shop";
    public static final String COL_LISTPRICE = "listprice";

    //2016.1.2 追加
    public static final String COL_TOBUYORNOT= "ToBuyOrNot";

    //2020.1.13 追加
    public static final String COL_BRAND= "brand";

    //2020.5.03 追加
    public static final String COL_SORTID = "sort_id";

    //	public static final String strUnit = "× ";

    protected final Context context;
    protected DatabaseHelper dbHelper;
    protected SQLiteDatabase db;

    public DBAdapter(Context context)
    {
        this.context = context;
        dbHelper = new DatabaseHelper(this.context);
    }

    //
    //	SQLiteOpenHelper
    //
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        public DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL
                    (
                            "CREATE TABLE " + TABLE_NAME + " ("
                                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                                    + COL_NOTE + " TEXT NOT NULL,"
                                    + COL_LASTUPDATE + " TEXT NOT NULL,"
                                    + COL_PRIORITY + " INTEGER NOT NULL,"
                                    + COL_NUMBER + " INTEGER NOT NULL,"
                                    + COL_SHOP + " TEXT NOT NULL,"
                                    + COL_LISTPRICE + " TEXT NOT NULL,"
                                    + COL_TOBUYORNOT + " INTEGER NOT NULL,"
                                    + COL_BRAND + " TEXT,"
                                    + COL_SORTID + " INTEGER"
                                    + ");"
                    );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            // DBバージョンアップ時のデータ移行を実装
            if (oldVersion < newVersion)
            {
                db.execSQL(
                        "alter table " + TABLE_NAME +
                                " ADD COLUMN "+ COL_SORTID +" INTEGER"
                );
            }
        }
    }

    //
    // Adapter Methods
    //
    public DBAdapter open()
    {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        dbHelper.close();
    }

    //
    // App Methods
    //

    //▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽ 選択 ▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽

    //全件取得
    public Cursor getAllNotes(String strArgKey, String strArgValue)
    {
//Log.v("xxx:", strArgKey+"  "+strArgValue+"  "+"☆★☆★☆★☆★☆getAllNotes passed ★☆★☆★☆★☆★");

        if(strArgValue != null) {
            //2022.12.25 修正
            //return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + strArgKey + "=" + strArgValue + " ORDER BY " + COL_SORTID + ";", null);
            //return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + strArgKey + "=" + strArgValue + " ORDER BY " + COL_PRIORITY + ";", null);
            //2024.1.7 修正
            return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + strArgKey + "=" + strArgValue + " ORDER BY " + COL_PRIORITY + ", " + COL_TOBUYORNOT + ";", null);
//COL_TOBUYORNOT
        }
        else
        {
            //2022.12.25 修正
            //return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_SORTID + ";", null);
            //return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_PRIORITY + ";", null);
            //2024.1.7 修正
            return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_PRIORITY + ", " + COL_TOBUYORNOT + ";", null);
        }
    }

    //2020.12.30 追加
    //最終行取得
    public Cursor getNewestNote()
    {
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC LIMIT 1;", null);
    }
    //▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽ 追加 ▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽

    //新規登録処理
    public void saveNote(String note, int ToBuyOrNot)
    {
        Date dateNow = new Date();
//Log.v("xxx:", "☆★☆★☆★☆★☆ note: " +note+ "★☆★☆★☆★☆★");

        //「put」メソッドを使ってカラムとカラムに設定する値を追加していきます。
        ContentValues values = new ContentValues();
        values.put(COL_NOTE, note);
        values.put(COL_LASTUPDATE, dateNow.toLocaleString());
        values.put(COL_NUMBER, 1);

        values.put(COL_PRIORITY, "1");		                        //修正予定
        values.put(COL_SHOP, "");
        values.put(COL_LISTPRICE, 0);

        //2016.1.2 追加
        values.put(COL_TOBUYORNOT, ToBuyOrNot);

        //2020.1.13 追加
        values.put(COL_BRAND, "");

        //2020.5.03 追加
        values.put(COL_SORTID, getLastSort_id() + 1);

        db.insertOrThrow(TABLE_NAME, null, values);
    }

    //2020.8.10 追加
    //CSVファイルからＩｎｓｅｒｔ
    public void InertFromCSV(int id, String note, String lastupdate, int priority, int number, String shop, int listprice, int tobuyornot, String brand, int sortid)
    {
        //「put」メソッドを使ってカラムとカラムに設定する値を追加していきます。
        ContentValues values = new ContentValues();

        values.put(COL_ID, id);
        values.put(COL_NOTE, note);
        values.put(COL_LASTUPDATE, lastupdate);
		values.put(COL_PRIORITY, priority);
        values.put(COL_NUMBER, number);
        values.put(COL_SHOP, shop);
        values.put(COL_LISTPRICE, listprice);
        values.put(COL_TOBUYORNOT, tobuyornot);
        values.put(COL_BRAND, brand);
        values.put(COL_SORTID, sortid);

        db.insertOrThrow(TABLE_NAME, null, values);
    }

    //▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽ 更新 ▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽

    //ポップアップ表示時のDB更新用
    //2015.04.14追加
    //2020.01.02修正
    public boolean updateNote(int id, String note, int number, int listprice, String shop, String brand)
    {
        Date dateNow = new Date();

        //「put」メソッドを使ってカラムとカラムに設定する値を追加していきます。
        ContentValues values = new ContentValues();

        values.put(COL_NOTE, note);
        values.put(COL_LASTUPDATE, dateNow.toLocaleString());
        values.put(COL_NUMBER, number);
        values.put(COL_SHOP, shop);
        values.put(COL_LISTPRICE, listprice);
        values.put(COL_BRAND, brand);
//		values.put(COL_PRIORITY, priority);

        return db.update(TABLE_NAME, values, COL_ID + "=" + id, null) > 0;
    }

    //優先購入のチェック更新処理
    public boolean updatePriority(int id, String note, int intPriority)
    //2024.1.1追加
    {
        Date dateNow = new Date();
        //「put」メソッドを使ってカラムとカラムに設定する値を追加していきます。
        ContentValues values = new ContentValues();

        values.put(COL_NOTE, note);
        values.put(COL_LASTUPDATE, dateNow.toLocaleString());
        values.put(COL_PRIORITY, intPriority);

        Log.d("xxx:", "☆★☆★☆★☆★☆ note id intPriority "+note+" "+id+" "+intPriority+" ★☆★☆★☆★☆★");

        return db.update(TABLE_NAME, values, COL_ID + "=" + id, null) > 0;
    }

    //色反転モード時のDB更新用
    //2016.1.2追加
    public boolean updateNoteToBuyOrNot(int id, String note, int ToBuyOrNot)
    {
        Date dateNow = new Date();

        //「put」メソッドを使ってカラムとカラムに設定する値を追加していきます。
        ContentValues values = new ContentValues();

        values.put(COL_NOTE, note);
        values.put(COL_LASTUPDATE, dateNow.toLocaleString());
        values.put(COL_TOBUYORNOT, ToBuyOrNot);		//2016.1.2 追加

        return db.update(TABLE_NAME, values, COL_ID + "=" + id, null) > 0;
    }

    public int getLastSort_id()
    {
        Cursor cursor = db.rawQuery("SELECT MAX(" + COL_SORTID + ") AS mxSortID FROM " + TABLE_NAME + ";", null);
        int mxSortID = 0;

        try {
            if (cursor.moveToNext())
            {
                mxSortID = cursor.getInt(cursor.getColumnIndex("mxSortID"));
            }
        } finally
        {
            cursor.close();
        }
        return mxSortID;
    }

    //▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽ 削除 ▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽▼▽

    public boolean deleteNote(int id)
    {
        return db.delete(TABLE_NAME, COL_ID + "=" + id, null) > 0;
    }

    public boolean deleteAllNotes()
    {
        return db.delete(TABLE_NAME, null, null) > 0;
    }

}
