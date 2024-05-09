package com.android.poetry_vocabulary.util;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.poetry_vocabulary.pojo.Poem;

import java.util.ArrayList;
import java.util.List;

public class PoemDatabaseHelper extends SQLiteOpenHelper {
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
    private final static String CREATE_TABLE_SQL = "CREATE TABLE if not exists " + TABLE_NAME + " (" +
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
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(POEM_NAME, poem.getPoemName());
        values.put(WRITER_NAME, poem.getWriterName());
        values.put(CONTENT, poem.getContent());
        values.put(DYNASTY, poem.getDynasty());
        values.put(EXPLANATION, poem.getExplanation());
        return db.insert(TABLE_NAME, null, values);
    }

    // 删除数据
    public int deletePoem(long poemId) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = POEM_ID + " = ?";
        String[] selectionArgs = {String.valueOf(poemId)};
        return db.delete(TABLE_NAME, selection, selectionArgs);
    }

    // 更新数据
    public int updatePoem(Poem poem) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(POEM_NAME, poem.getPoemName());
        values.put(WRITER_NAME, poem.getWriterName());
        values.put(CONTENT, poem.getContent());
        values.put(DYNASTY, poem.getDynasty());
        values.put(EXPLANATION, poem.getExplanation());
        String selection = POEM_ID + " = ?";
        String[] selectionArgs = {String.valueOf(poem.getPoemId())};
        return db.update(TABLE_NAME, values, selection, selectionArgs);
    }

    // 查询数据, 返回 ALL Poem 列表
    public List<Poem> queryAllPoems() {
        List<Poem> poemList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long poemId = cursor.getLong(cursor.getColumnIndex(POEM_ID));
                @SuppressLint("Range") String poemName = cursor.getString(cursor.getColumnIndex(POEM_NAME));
                @SuppressLint("Range") String writerName = cursor.getString(cursor.getColumnIndex(WRITER_NAME));
                @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex(CONTENT));
                @SuppressLint("Range") String dynasty = cursor.getString(cursor.getColumnIndex(DYNASTY));
                @SuppressLint("Range") String explanation = cursor.getString(cursor.getColumnIndex(EXPLANATION));
                Poem poem = new Poem(poemId, poemName, writerName, content, dynasty, explanation);
                poemList.add(poem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return poemList;
    }
}