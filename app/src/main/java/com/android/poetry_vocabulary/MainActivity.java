package com.android.poetry_vocabulary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.poetry_vocabulary.pojo.Sentence;
import com.android.poetry_vocabulary.pojo.Sentences;
import com.android.poetry_vocabulary.util.PoemDatabaseHelper;
import com.android.poetry_vocabulary.util.ResourceUtil;

import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "poetry_vocabulary_preferences_config";
    private static final String KEY_FIRST_START = "is_first_start";
    private static final String KEY_SERVICE_RUNNING = "service_is_running";

    private Intent bgmServiceIntent;
    private List<Sentence> sentences;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setupSystemBarsInsets();
        setupClickListeners();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        setupBgmService();
        checkAndImportData();
        loadSentences();
        setRandomSentence();
    }

    private void setupSystemBarsInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_restoration).setOnClickListener(v -> startActivity(new Intent(this, RestorationPoetryActivity.class)));
        findViewById(R.id.btn_learn).setOnClickListener(v -> startActivity(new Intent(this, PoemLearnActivity.class)));
        findViewById(R.id.btn_add).setOnClickListener(v -> startActivity(new Intent(this, AddPoemActivity.class)));
        findViewById(R.id.btn_list).setOnClickListener(v -> startActivity(new Intent(this, PoemListActivity.class)));
        findViewById(R.id.btn_music).setOnClickListener(v -> toggleBgmService());
    }

    private void setupBgmService() {
        bgmServiceIntent = new Intent(this, BGMService.class);
        boolean isServiceRunning = sharedPreferences.getBoolean(KEY_SERVICE_RUNNING, false);
        toggleBgmServiceState(isServiceRunning);
    }

    private void checkAndImportData() {
        if (sharedPreferences.getBoolean(KEY_FIRST_START, true)) {
            sharedPreferences.edit().putBoolean(KEY_FIRST_START, false).apply();
            importPoemsFromXml();
        }
    }

    private void importPoemsFromXml() {
        try {
            InputStream inputStream = getAssets().open("poems.xml");
            PoemDatabaseHelper.getInstance(this).insertPoemsFromXml(inputStream);
            inputStream.close();
        } catch (Exception e) {
            Log.e("MainActivity", "Error reading poems.xml", e);
        }
    }

    private void toggleBgmService() {
        boolean isServiceRunning = isBgmServiceRunning();
        toggleBgmServiceState(!isServiceRunning);
    }

    private void toggleBgmServiceState(boolean isRunning) {
        if (bgmServiceIntent != null) {
            sharedPreferences.edit().putBoolean(KEY_SERVICE_RUNNING, isRunning).apply();
            if (isRunning) {
                startService(bgmServiceIntent);
                Toast.makeText(this, "播放音乐", Toast.LENGTH_SHORT).show();
            } else {
                stopService(bgmServiceIntent);
                Toast.makeText(this, "暂停音乐", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isBgmServiceRunning() {
        return sharedPreferences.getBoolean(KEY_SERVICE_RUNNING, false);
    }

    private void loadSentences() {
        if (sentences == null) {
            try {
                InputStream inputStream = ResourceUtil.getInputStreamFromAssets(this, "famous_sentences.xml");
                getSentencesFromXml(inputStream);
            } catch (Exception e) {
                Log.e("MainActivity", "Error reading sentences.xml", e);
            }
        }
    }

    private void setRandomSentence() {
        if (sentences != null) {
            TextView sentenceTextView = findViewById(R.id.app_description);
            String currentSentence = sentenceTextView.getText().toString();
            Sentence randomSentence = getRandomSentence(currentSentence);
            sentenceTextView.setText(randomSentence.getContent());
        }
    }

    private Sentence getRandomSentence(String currentSentence) {
        int index = (int) (Math.random() * sentences.size());
        Sentence sentence = sentences.get(index);
        while (sentence.getContent().equals(currentSentence)) {
            index = (int) (Math.random() * sentences.size());
            sentence = sentences.get(index);
        }
        return sentence;
    }

    private void getSentencesFromXml(InputStream inputStream) throws Exception {
        Sentences temp = ResourceUtil.parseXmlFromInputStream(inputStream, Sentences.class);
        sentences = temp.getContentList();
        inputStream.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRandomSentence();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        toggleBgmServiceState(false);
    }
}