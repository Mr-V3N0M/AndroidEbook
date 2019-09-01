package com.example.shwenyarmya.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.shwenyarmya.Item.DownloadList;
import com.example.shwenyarmya.Item.SubCategoryList;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "My Book";

    // book fav table name
    private static final String TABLE_NAME = "book_fav";

    // book continue table name
    private static final String TABLE_NAME_CONTINUE = "book_continue";

    // book download table name
    private static final String TABLE_NAME_DOWNLOAD = "book_download";

    // book epub read position table name
    private static final String TABLE_NAME_EPUB = "epub";

    // book pdf read position table name
    private static final String TABLE_NAME_PDF = "pdf";

    // book Table Columns names
    private static final String ID = "auto_id";
    private static final String KEY_BOOK_ID = "book_id";
    private static final String KEY_BOOK_TITLE = "book_title";
    private static final String KEY_BOOK_DESCRIPTION = "book_description";
    private static final String KEY_BOOK_IMAGE = "image";
    private static final String KEY_BOOK_BACKGROUND_IMAGE = "cover_image";
    private static final String KEY_BOOK_FILE_TYPE = "book_file_type";
    private static final String KEY_BOOK_FILE_URL = "book_file_url";
    private static final String KEY_BOOK_RATE = "book_rate";
    private static final String KEY_BOOK_RATE_AVG = "book_rate_avg";
    private static final String KEY_BOOK_VIEW = "book_view";
    private static final String KEY_BOOK_AUTHOR_NAME = "book_author_name";

    //book epub Table Columns name
    private static final String KEY_EPUB_BOOK_ID = "id";
    private static final String KEY_EPUB_BOOK_LAST_READ_POSITION = "last_read_position";

    //book pdf Table Columns name
    private static final String KEY_PDF_BOOK_ID = "id";
    private static final String KEY_PDF_BOOK_LAST_READ_POSITION = "pdf_last_read_position";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_BOOK_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_BOOK_ID + " TEXT,"
                + KEY_BOOK_TITLE + " TEXT," + KEY_BOOK_DESCRIPTION + " TEXT," + KEY_BOOK_IMAGE + " TEXT,"
                + KEY_BOOK_BACKGROUND_IMAGE + " TEXT," + KEY_BOOK_FILE_TYPE + " TEXT,"
                + KEY_BOOK_RATE + " TEXT," + KEY_BOOK_RATE_AVG + " TEXT,"
                + KEY_BOOK_VIEW + " TEXT," + KEY_BOOK_AUTHOR_NAME + " TEXT"
                + ")";
        db.execSQL(CREATE_BOOK_TABLE);

        String CREATE_BOOK_CONTINUE_TABLE = "CREATE TABLE " + TABLE_NAME_CONTINUE + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_BOOK_ID + " TEXT,"
                + KEY_BOOK_TITLE + " TEXT," + KEY_BOOK_DESCRIPTION + " TEXT," + KEY_BOOK_IMAGE + " TEXT,"
                + KEY_BOOK_BACKGROUND_IMAGE + " TEXT," + KEY_BOOK_FILE_TYPE + " TEXT,"
                + KEY_BOOK_RATE + " TEXT," + KEY_BOOK_RATE_AVG + " TEXT,"
                + KEY_BOOK_VIEW + " TEXT," + KEY_BOOK_AUTHOR_NAME + " TEXT"
                + ")";
        db.execSQL(CREATE_BOOK_CONTINUE_TABLE);

        String CREATE_TABLE_DOWNLOAD = "CREATE TABLE " + TABLE_NAME_DOWNLOAD + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_BOOK_ID + " TEXT,"
                + KEY_BOOK_TITLE + " TEXT," + KEY_BOOK_IMAGE + " TEXT,"
                + KEY_BOOK_AUTHOR_NAME + "" + " TEXT," + KEY_BOOK_FILE_URL + " TEXT" + ")";
        db.execSQL(CREATE_TABLE_DOWNLOAD);

        String CREATE_TABLE_EPUB = "CREATE TABLE " + TABLE_NAME_EPUB + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_EPUB_BOOK_ID + " TEXT,"
                + KEY_EPUB_BOOK_LAST_READ_POSITION + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE_EPUB);


        String CREATE_TABLE_PDF = "CREATE TABLE " + TABLE_NAME_PDF + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_PDF_BOOK_ID + " TEXT,"
                + KEY_PDF_BOOK_LAST_READ_POSITION + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE_PDF);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CONTINUE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DOWNLOAD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EPUB);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PDF);
        onCreate(db);
    }


    // Adding new book detail
    public void addDetail(SubCategoryList scdList) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_BOOK_ID, scdList.getId());
        values.put(KEY_BOOK_TITLE, scdList.getBook_title());
        values.put(KEY_BOOK_DESCRIPTION, scdList.getBook_description());
        values.put(KEY_BOOK_IMAGE, scdList.getBook_cover_img());
        values.put(KEY_BOOK_BACKGROUND_IMAGE, scdList.getBook_bg_img());
        values.put(KEY_BOOK_FILE_TYPE, scdList.getBook_file_type());
        values.put(KEY_BOOK_RATE, scdList.getTotal_rate());
        values.put(KEY_BOOK_RATE_AVG, scdList.getRate_avg());
        values.put(KEY_BOOK_VIEW, scdList.getBook_views());
        values.put(KEY_BOOK_AUTHOR_NAME, scdList.getAuthor_name());


        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }


    public List<SubCategoryList> getBookDetail() {
        List<SubCategoryList> scdLists = new ArrayList<SubCategoryList>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SubCategoryList list = new SubCategoryList();
                list.setId(cursor.getString(1));
                list.setBook_title(cursor.getString(2));
                list.setBook_description(cursor.getString(3));
                list.setBook_cover_img(cursor.getString(4));
                list.setBook_bg_img(cursor.getString(5));
                list.setBook_file_type(cursor.getString(6));
                list.setTotal_rate(cursor.getString(7));
                list.setRate_avg(cursor.getString(8));
                list.setBook_views(cursor.getString(9));
                list.setAuthor_name(cursor.getString(10));

                // Adding book to list
                scdLists.add(list);
            } while (cursor.moveToNext());
        }

        // return category list
        return scdLists;
    }

    // Updating view fav
    public int updateFavView(String id, String view) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_BOOK_VIEW, view);

        // updating row
        return db.update(TABLE_NAME, values, KEY_BOOK_ID + "=" + id, null);
    }

    // Updating ratingSend fav
    public int updateFavRate(String id, String total_rate, String rate_avg) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_BOOK_RATE, total_rate);
        values.put(KEY_BOOK_RATE_AVG, rate_avg);

        // updating row
        return db.update(TABLE_NAME, values, KEY_BOOK_ID + "=" + id, null);
    }

    // Deleting single book
    public boolean deleteDetail(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, KEY_BOOK_ID + "=" + id, null) > 0;

    }

    //check book id in database or not
    public boolean checkId(String id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY_BOOK_ID + "=" + id;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    //-------------Continue Table-------------------//

    // Adding new book detail
    public void addContinueBook(SubCategoryList subCategoryList) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_BOOK_ID, subCategoryList.getId());
        values.put(KEY_BOOK_TITLE, subCategoryList.getBook_title());
        values.put(KEY_BOOK_DESCRIPTION, subCategoryList.getBook_description());
        values.put(KEY_BOOK_IMAGE, subCategoryList.getBook_cover_img());
        values.put(KEY_BOOK_BACKGROUND_IMAGE, subCategoryList.getBook_bg_img());
        values.put(KEY_BOOK_FILE_TYPE, subCategoryList.getBook_file_type());
        values.put(KEY_BOOK_RATE, subCategoryList.getTotal_rate());
        values.put(KEY_BOOK_RATE_AVG, subCategoryList.getRate_avg());
        values.put(KEY_BOOK_VIEW, subCategoryList.getBook_views());
        values.put(KEY_BOOK_AUTHOR_NAME, subCategoryList.getAuthor_name());


        // Inserting Row
        db.insert(TABLE_NAME_CONTINUE, null, values);
        db.close(); // Closing database connection
    }


    // Getting All book
    public List<SubCategoryList> getContinueBook(String item) {
        List<SubCategoryList> subCategoryLists = new ArrayList<SubCategoryList>();

        String selectQuery;
        if (item.equals("all")) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME_CONTINUE + " ORDER BY " + ID + " DESC ";
        } else {
            selectQuery = "SELECT  * FROM " + TABLE_NAME_CONTINUE + " ORDER BY " + ID + " DESC " + " limit " + item;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SubCategoryList list = new SubCategoryList();
                list.setId(cursor.getString(1));
                list.setBook_title(cursor.getString(2));
                list.setBook_description(cursor.getString(3));
                list.setBook_cover_img(cursor.getString(4));
                list.setBook_bg_img(cursor.getString(5));
                list.setBook_file_type(cursor.getString(6));
                list.setTotal_rate(cursor.getString(7));
                list.setRate_avg(cursor.getString(8));
                list.setBook_views(cursor.getString(9));
                list.setAuthor_name(cursor.getString(10));


                // Adding book to list
                subCategoryLists.add(list);
            } while (cursor.moveToNext());
        }

        // return book list
        return subCategoryLists;
    }

    // Updating view continue
    public int updateContinueView(String id, String view) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_BOOK_VIEW, view);

        // updating row
        return db.update(TABLE_NAME_CONTINUE, values, KEY_BOOK_ID + "=" + id, null);
    }

    // Updating ratingSend continue
    public int updateContinueRate(String id, String total_rate, String rate_avg) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_BOOK_RATE, total_rate);
        values.put(KEY_BOOK_RATE_AVG, rate_avg);

        // updating row
        return db.update(TABLE_NAME_CONTINUE, values, KEY_BOOK_ID + "=" + id, null);
    }


    // Deleting single book
    public boolean deleteContinue_Book(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME_CONTINUE, KEY_BOOK_ID + "=" + id, null) > 0;

    }

    //check book id in database or not
    public boolean checkId_ContinueBook(String id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_CONTINUE + " WHERE " + KEY_BOOK_ID + "=" + id;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    //-------------Continue Table-------------------//

    //-------------Download Table-------------------//

    // Adding new book detail
    public void addDownload(DownloadList downloadList) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_BOOK_ID, downloadList.getId());
        values.put(KEY_BOOK_TITLE, downloadList.getTitle());
        values.put(KEY_BOOK_IMAGE, downloadList.getImage());
        values.put(KEY_BOOK_AUTHOR_NAME, downloadList.getAuthor());
        values.put(KEY_BOOK_FILE_URL, downloadList.getUrl());

        // Inserting Row
        db.insert(TABLE_NAME_DOWNLOAD, null, values);
        db.close(); // Closing database connection
    }


    // Getting All book
    public List<DownloadList> getDownload() {
        List<DownloadList> downloadLists = new ArrayList<DownloadList>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_DOWNLOAD;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DownloadList list = new DownloadList();
                list.setId(cursor.getString(1));
                list.setTitle(cursor.getString(2));
                list.setImage(cursor.getString(3));
                list.setAuthor(cursor.getString(4));
                list.setUrl(cursor.getString(5));

                // Adding book to list
                downloadLists.add(list);
            } while (cursor.moveToNext());
        }

        // return book list
        return downloadLists;
    }

    // Deleting single book
    public boolean deleteDownloadBook(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME_DOWNLOAD, KEY_BOOK_ID + "=" + id, null) > 0;

    }

    //check book id in database or not
    public boolean checkIdDownloadBook(String id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_DOWNLOAD + " WHERE " + KEY_BOOK_ID + "=" + id;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    //-------------Download Table-------------------//

    //-------------EPUB Table-------------------//

    // Adding new epub detail
    public void addEpub(String id, String lastReadPosition) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EPUB_BOOK_ID, id);
        values.put(KEY_EPUB_BOOK_LAST_READ_POSITION, lastReadPosition);

        // Inserting Row
        db.insert(TABLE_NAME_EPUB, null, values);
        db.close(); // Closing database connection
    }

    // Getting epub
    public String getEpub(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME_EPUB, new String[]{KEY_EPUB_BOOK_ID,
                        KEY_EPUB_BOOK_LAST_READ_POSITION}, KEY_EPUB_BOOK_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null, null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(KEY_EPUB_BOOK_LAST_READ_POSITION));

    }

    // Updating epub in database
    public int updateEpub(String id, String lastReadPosition) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EPUB_BOOK_LAST_READ_POSITION, lastReadPosition);
        // updating row
        return db.update(TABLE_NAME_EPUB, values, KEY_EPUB_BOOK_ID + "=" + id, null);
    }

    //check epub id in database or not
    public boolean checkIdEpub(String id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_EPUB + " WHERE " + KEY_EPUB_BOOK_ID + "=" + id;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    //-------------EPUB Table-------------------//


    //-------------PDF Table-------------------//

    // Adding new pdf detail
    public void addPdf(String id, String lastReadPosition) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PDF_BOOK_ID, id);
        values.put(KEY_PDF_BOOK_LAST_READ_POSITION, lastReadPosition);

        // Inserting Row
        db.insert(TABLE_NAME_PDF, null, values);
        db.close(); // Closing database connection
    }

    // Getting pdf
    public String getPdf(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME_PDF, new String[]{KEY_PDF_BOOK_ID,
                        KEY_PDF_BOOK_LAST_READ_POSITION}, KEY_PDF_BOOK_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null, null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(KEY_PDF_BOOK_LAST_READ_POSITION));

    }

    // Updating pdf in database
    public int updatePdf(String id, String lastReadPosition) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PDF_BOOK_LAST_READ_POSITION, lastReadPosition);
        // updating row
        return db.update(TABLE_NAME_PDF, values, KEY_PDF_BOOK_ID + "=" + id, null);
    }

    //check pdf id in database or not
    public boolean checkIdPdfBook(String id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_PDF + " WHERE " + KEY_PDF_BOOK_ID + "=" + id;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    //-------------PDF Table-------------------//

}
