package com.android.poetry_vocabulary;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.poetry_vocabulary.pojo.Poem;
import com.android.poetry_vocabulary.util.PoemDatabaseHelper;

import java.util.Locale;

public class PoemLearnActivity extends AppCompatActivity {

    // 定义类成员变量
    private static final int MY_DATA_CHECK_CODE = 1234;

    private TextView poemTitleTextView, poemOtherTextView, poemContentView, poemExplanationView;
    private Button poemChangeButton, poemReadButton;
    private PoemDatabaseHelper poemDatabaseHelper;
    private Poem currentPoem;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        poemDatabaseHelper = PoemDatabaseHelper.getInstance(this);
        if (poemDatabaseHelper.queryAllPoems().isEmpty()) {
            Toast.makeText(this, "诗词库为空，请添加诗词", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_poem_learn);
        setupViews();
        setupButtons();
        setupInitialPoem();
        initTextToSpeech();
    }

    // 设置视图
    private void setupViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        poemTitleTextView = findViewById(R.id.poem_title_text_view);
        poemOtherTextView = findViewById(R.id.poem_other_text_view);
        poemContentView = findViewById(R.id.poem_content_view);
        poemExplanationView = findViewById(R.id.poem_explanation_view);
        poemChangeButton = findViewById(R.id.poem_change_button);
        poemReadButton = findViewById(R.id.poem_read_button);
    }

    // 设置按钮
    private void setupButtons() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                400,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = 30;

        poemChangeButton.setOnClickListener(v -> changePoem());
        poemChangeButton.setLayoutParams(params);
        poemChangeButton.setBackgroundResource(R.drawable.rounded_button_bg);

        poemReadButton.setOnClickListener(v -> readPoem());
        poemReadButton.setLayoutParams(params);
        poemReadButton.setBackgroundResource(R.drawable.rounded_button_bg);
    }

    // 设置初始诗词
    private void setupInitialPoem() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            currentPoem = getRandomPoem();
        } else {
            currentPoem = getPoemFromBundle(bundle);
        }
        displayPoem(currentPoem);
    }

    // 获取随机诗词
    private Poem getRandomPoem() {
        return poemDatabaseHelper.queryAllPoems().get((int) (Math.random() * poemDatabaseHelper.queryAllPoems().size()));
    }

    // 从Bundle中获取诗词
    private Poem getPoemFromBundle(Bundle bundle) {
        String poemName = bundle.getString("poem_name");
        String writerName = bundle.getString("writer_name");
        String dynasty = bundle.getString("dynasty");
        String content = bundle.getString("content");
        String explanation = bundle.getString("explanation");
        return new Poem(poemName, writerName, content, dynasty, explanation);
    }

    // 更换诗词
    private void changePoem() {
        Poem newPoem;
        do {
            newPoem = getRandomPoem();
        } while (newPoem.getPoemId().equals(currentPoem.getPoemId()));
        currentPoem = newPoem;
        displayPoem(currentPoem);
    }

    // 显示诗词
    private void displayPoem(Poem poem) {
        poemTitleTextView.setText(poem.getPoemName());
        poemOtherTextView.setText(String.format("作者：%s 朝代：%s", poem.getWriterName(), poem.getDynasty()));
        poemContentView.setText(formatContent(poem.getContent()));
        poemExplanationView.setText(String.format("解释：%s", poem.getExplanation()));
    }

    // 格式化诗词内容
    private String formatContent(String content) {
        StringBuilder formattedContent = new StringBuilder();
        String[] lines = content.split(" ");
        for (String line : lines) {
            formattedContent.append(line).append("\n");
        }
        return formattedContent.toString();
    }

    // 为朗诵格式化诗词
    private String formatPoemForSpeech(Poem poem) {
        StringBuilder formattedContent = new StringBuilder();
        String[] lines = poem.getContent().split(" ");
        for (String line : lines) {
            formattedContent.append(line).append(" ...");
        }
        return formattedContent.toString();
    }

    // 朗诵诗词
    private void readPoem() {
        if (currentPoem != null && textToSpeech != null) {
            String poemTitle = currentPoem.getPoemName();
            String poemWriter = currentPoem.getWriterName();
            String poemDynasty = currentPoem.getDynasty();
            String poemExplanation = currentPoem.getExplanation();
            String poemContent = formatPoemForSpeech(currentPoem);
            String message = String.format("《%s》\n作者：%s 朝代：%s\n\n%s\n\n%s", poemTitle, poemWriter, poemDynasty, poemContent, poemExplanation);
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // 注册检查语音数据的ActivityResultLauncher
    private final ActivityResultLauncher<Intent> requestVoiceData = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    // 语音数据可用,初始化 TextToSpeech 引擎
                    textToSpeech = new TextToSpeech(PoemLearnActivity.this, this::handleTextToSpeechStatus);
                } else {
                    // 语音数据不可用,禁用朗诵功能
                    textToSpeech = null;
                    poemReadButton.setEnabled(false);
                }
            }
    );

    // 初始化TextToSpeech
    private void initTextToSpeech() {
        Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        requestVoiceData.launch(checkIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                textToSpeech = new TextToSpeech(this, this::handleTextToSpeechStatus);
            } else {
                textToSpeech = null;
                poemReadButton.setEnabled(false);
            }
        }
    }

    // 处理TextToSpeech的状态
    private void handleTextToSpeechStatus(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.CHINA);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech.setLanguage(Locale.getDefault());
            }
        } else {
            textToSpeech = null;
            poemReadButton.setEnabled(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        poemDatabaseHelper.openReadDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        poemDatabaseHelper.closeDB();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textToSpeech != null) {
            textToSpeech.setLanguage(Locale.CHINA);
        }
    }
}
