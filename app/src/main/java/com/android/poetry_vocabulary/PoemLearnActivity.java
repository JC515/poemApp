package com.android.poetry_vocabulary;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.poetry_vocabulary.pojo.Poem;
import com.android.poetry_vocabulary.util.PoemDatabaseHelper;

import java.util.Locale;

public class PoemLearnActivity extends AppCompatActivity {

    // 定义视图和按钮
    private TextView poemTitleTextView, poemOtherTextView, poemContentView, poemExplanationView;
    private Button poemChangeButton, exitButton, poemReadButton;
    // 创建数据库帮助类和当前诗歌对象
    private PoemDatabaseHelper poemDatabaseHelper;
    private Poem currentPoem;
    // 创建文字转语音对象
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 启用全屏显示
        EdgeToEdge.enable(this);
        // 设置界面布局
        setContentView(R.layout.activity_poem_learn);
        // 初始化视图
        setupViews();
        // 获取诗歌数据库帮助类实例
        poemDatabaseHelper = PoemDatabaseHelper.getInstance(this);
        // 设置按钮点击事件监听
        setupButtons();
        // 设置初始诗歌
        setupInitialPoem(savedInstanceState);
        // 初始化文字转语音引擎
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.CHINA);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // 处理语言数据缺失或不支持的情况
                    textToSpeech.setLanguage(Locale.getDefault());
                }
            } else {
                // 处理初始化失败的情况
                textToSpeech = null;
            }
        });
    }

    // 设置视图布局
    private void setupViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 初始化各个视图和按钮
        poemTitleTextView = findViewById(R.id.poem_title_text_view);
        poemOtherTextView = findViewById(R.id.poem_other_text_view);
        poemContentView = findViewById(R.id.poem_content_view);
        poemExplanationView = findViewById(R.id.poem_explanation_view);
        poemChangeButton = findViewById(R.id.poem_change_button);
        exitButton = findViewById(R.id.exit_button);
        poemReadButton = findViewById(R.id.poem_read_button);
    }

    // 设置按钮点击事件监听
    private void setupButtons() {
        exitButton.setOnClickListener(v -> finish());
        poemChangeButton.setOnClickListener(v -> changePoem());
        poemReadButton.setOnClickListener(v -> readPoem());
    }

    // 设置初始诗歌
    private void setupInitialPoem(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            currentPoem = getRandomPoem();
        } else {
            currentPoem = getPoemFromBundle(savedInstanceState);
        }
        displayPoem(currentPoem);
    }

    // 随机获取一首诗歌
    private Poem getRandomPoem() {
        return poemDatabaseHelper.queryAllPoems().get((int) (Math.random() * poemDatabaseHelper.queryAllPoems().size()));
    }

    // 从Bundle中获取诗歌对象
    private Poem getPoemFromBundle(Bundle bundle) {
        String poemName = bundle.getString("poem_name");
        String writerName = bundle.getString("writer_name");
        String dynasty = bundle.getString("dynasty");
        String content = bundle.getString("content");
        String explanation = bundle.getString("explanation");
        return new Poem(poemName, writerName, dynasty, content, explanation);
    }

    // 更换诗歌
    private void changePoem() {
        Poem newPoem;
        do {
            newPoem = getRandomPoem();
        } while (newPoem.getPoemId().equals(currentPoem.getPoemId()));
        currentPoem = newPoem;
        displayPoem(currentPoem);
    }

    // 显示诗歌内容
    private void displayPoem(Poem poem) {
        poemTitleTextView.setText(poem.getPoemName());
        poemOtherTextView.setText(String.format("作者：%s 朝代：%s", poem.getWriterName(), poem.getDynasty()));
        poemContentView.setText(formatContent(poem.getContent()));
        poemExplanationView.setText(String.format("解释：%s", poem.getExplanation()));
    }

    // 格式化诗歌内容
    private String formatContent(String content) {
        StringBuilder formattedContent = new StringBuilder();
        String[] lines = content.split(" ");
        for (String line : lines) {
            formattedContent.append(line).append("\n");
        }
        return formattedContent.toString();
    }

    // 播放诗歌朗诵
    private void readPoem() {
        if (currentPoem != null) {
            String poemContent = currentPoem.getContent().replace("\n", " ");
            textToSpeech.speak(poemContent, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 打开诗歌数据库连接
        poemDatabaseHelper.openReadDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭诗歌数据库连接
        poemDatabaseHelper.closeDB();
        if (textToSpeech != null) {
            // 停止文字转语音播放并关闭引擎
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
