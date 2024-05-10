package com.android.poetry_vocabulary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.poetry_vocabulary.util.PoemDatabaseHelper;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 跳转还原诗词页面
        findViewById(R.id.btn_1).setOnClickListener(e -> {
            Intent intent = new Intent(MainActivity.this, RestorationPoetryActivity.class);
            startActivity(intent);
        });
        // 跳转学习诗词页面
        findViewById(R.id.btn_2).setOnClickListener(e -> {
            Intent intent = new Intent(MainActivity.this, PoemLearnActivity.class);
            startActivity(intent);
        });
        // 跳转添加诗词页面
        findViewById(R.id.btn_3).setOnClickListener(e -> {
            Intent intent = new Intent(MainActivity.this, AddPoemActivity.class);
            startActivity(intent);
        });
        // 跳转诗词列表页面
        findViewById(R.id.btn_4).setOnClickListener(e -> {
            Intent intent = new Intent(MainActivity.this, PoemListActivity.class);
            startActivity(intent);
        });

        // 检查是否有诗词数据，如果没有，则导入poems.xml文件
        SharedPreferences sharedPreferences = getSharedPreferences("poetry_vocabulary_preferences_config", MODE_PRIVATE);
        // 首次启动应用时，检查是否有诗词数据，如果没有，则导入poems.xml文件
        if (sharedPreferences.getBoolean("is_first_start", true)) {
            sharedPreferences.edit().putBoolean("is_first_start", false).apply();
            try {
                InputStream inputStream = getAssets().open("poems.xml");
                PoemDatabaseHelper.getInstance(this).insertPoemsFromXml(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}