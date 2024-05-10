package com.android.poetry_vocabulary.util;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.poetry_vocabulary.pojo.Poem;
import com.android.poetry_vocabulary.pojo.Poems;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PoemDatabaseHelper extends SQLiteOpenHelper {
    // 数据库读对象
    private SQLiteDatabase mRDB;
    // 数据库写对象
    private SQLiteDatabase mWDB;

    // 打开读取数据库
    public SQLiteDatabase openReadDB() {
        if (mRDB == null || !mRDB.isOpen()) {
            mRDB = sInstance.getReadableDatabase();
        }
        return mRDB;
    }

    // 打开写入数据库
    public SQLiteDatabase openWriteDB() {
        if (mWDB == null || !mWDB.isOpen()) {
            mWDB = sInstance.getWritableDatabase();
        }
        return mWDB;
    }

    // 关闭数据库
    public void closeDB() {
        if (mRDB != null && mRDB.isOpen()) {
            mRDB.close();
            mRDB = null;
        }
        if (mWDB != null && mWDB.isOpen()) {
            mWDB.close();
            mWDB = null;
        }
    }

    // 单例模式
    private static PoemDatabaseHelper sInstance;

    // 获取单例
    public static PoemDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PoemDatabaseHelper(context);
        }
        return sInstance;
    }

    // 数据库名
    private static final String DATABASE_NAME = "poem.db";
    // 数据库版本
    private static final int DATABASE_VERSION = 1;

    private final static String TABLE_NAME = "poems";// 表名
    private final static String POEM_ID = "poem_id";// 诗id，自增， PRIMARY KEY
    private final static String POEM_NAME = "poem_name";// 诗名，NOT NULL
    private final static String WRITER_NAME = "writer_name";// 作者名 NOT NULL
    private final static String CONTENT = "content";// 诗内容 NOT NULL
    private final static String DYNASTY = "dynasty";// 朝代 NOT NULL
    private final static String EXPLANATION = "explanation";// 解释 NOT NULL
    // 创建表的 SQL 语句
    private final static String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            POEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            POEM_NAME + " TEXT NOT NULL, " +
            WRITER_NAME + " TEXT NOT NULL, " +
            CONTENT + " TEXT NOT NULL, " +
            DYNASTY + " TEXT NOT NULL, " +
            EXPLANATION + " TEXT NOT NULL" +
            ");";

    // 构造方法设为私有，禁止外部创建对象
    private PoemDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建表的 SQL 语句
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 在数据库升级时执行
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // 插入数据
    public long insertPoem(Poem poem) {
        // 先查询是否存在相同的诗词
        Poem oldPoem = queryPoem(poem.getPoemName(), poem.getWriterName());
        if (oldPoem != null) {
            // 存在相同的诗词，进行更新
            return updatePoem(poem);
        }
        ContentValues values = new ContentValues();
        values.put(POEM_NAME, poem.getPoemName());
        values.put(WRITER_NAME, poem.getWriterName());
        values.put(CONTENT, poem.getContent());
        values.put(DYNASTY, poem.getDynasty());
        values.put(EXPLANATION, poem.getExplanation());
        return mWDB.insert(TABLE_NAME, null, values);
    }

    // 删除数据
    public int deletePoem(long poemId) {
        String selection = POEM_ID + " = ?";
        String[] selectionArgs = {String.valueOf(poemId)};
        return mWDB.delete(TABLE_NAME, selection, selectionArgs);
    }

    // 更新数据
    public int updatePoem(Poem poem) {
        ContentValues values = new ContentValues();
        values.put(POEM_NAME, poem.getPoemName());
        values.put(WRITER_NAME, poem.getWriterName());
        values.put(CONTENT, poem.getContent());
        values.put(DYNASTY, poem.getDynasty());
        values.put(EXPLANATION, poem.getExplanation());
        String selection = POEM_ID + " = ?";
        String[] selectionArgs = {String.valueOf(poem.getPoemId())};
        return mWDB.update(TABLE_NAME, values, selection, selectionArgs);
    }

    // 查询数据, 返回 ALL Poem 列表
    @SuppressLint("Range")
    public List<Poem> queryAllPoems() {
        List<Poem> poemList = new ArrayList<>();
        SQLiteDatabase db = openReadDB();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Poem poem = new Poem();
            poem.setPoemId(cursor.getLong(cursor.getColumnIndex(POEM_ID)));
            poem.setPoemName(cursor.getString(cursor.getColumnIndex(POEM_NAME)));
            poem.setWriterName(cursor.getString(cursor.getColumnIndex(WRITER_NAME)));
            poem.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
            poem.setDynasty(cursor.getString(cursor.getColumnIndex(DYNASTY)));
            poem.setExplanation(cursor.getString(cursor.getColumnIndex(EXPLANATION)));
            poemList.add(poem);
        }
        cursor.close();
        return poemList;
    }

    // 根据诗词名和作者名查询数据，返回 Poem 对象
    @SuppressLint("Range")
    public Poem queryPoem(String poemName, String writerName) {
        SQLiteDatabase db = openReadDB();
        String selection = POEM_NAME + " = ? AND " + WRITER_NAME + " = ?";
        String[] selectionArgs = {poemName, writerName};
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        Poem poem = null;
        if (cursor.moveToNext()) {
            poem = new Poem();
            poem.setPoemId(cursor.getLong(cursor.getColumnIndex(POEM_ID)));
            poem.setPoemName(cursor.getString(cursor.getColumnIndex(POEM_NAME)));
            poem.setWriterName(cursor.getString(cursor.getColumnIndex(WRITER_NAME)));
            poem.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
            poem.setDynasty(cursor.getString(cursor.getColumnIndex(DYNASTY)));
            poem.setExplanation(cursor.getString(cursor.getColumnIndex(EXPLANATION)));
        }
        cursor.close();
        return poem;
    }

    public void insertPoemsFromXml(InputStream inputStream) throws Exception {
        Serializer serializer = new Persister();
        Poems poems = serializer.read(Poems.class, inputStream);
        List<Poem> poemList = poems.getPoemList();

        SQLiteDatabase db = openWriteDB();
        db.beginTransaction();
        try {
            for (Poem poem : poemList) {
                insertPoem(poem);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

}