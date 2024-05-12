package com.android.poetry_vocabulary;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.poetry_vocabulary.pojo.Poem;
import com.android.poetry_vocabulary.util.PoemDatabaseHelper;

public class AddPoemActivity extends AppCompatActivity {

    private EditText poemNameEditText;
    private EditText writerNameEditText;
    private EditText contentEditText;
    private EditText explanationEditText;
    private Spinner dynastySpinner;

    private PoemDatabaseHelper poemDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_poem);

        // 设置沉浸式状态栏
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化控件
        poemNameEditText = findViewById(R.id.poem_name_edit_text);
        writerNameEditText = findViewById(R.id.writer_name_edit_text);
        contentEditText = findViewById(R.id.content_edit_text);
        explanationEditText = findViewById(R.id.explanation_edit_text);
        dynastySpinner = findViewById(R.id.dynasty_spinner);

        // 设置spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.dynasty_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dynastySpinner.setAdapter(adapter);
        dynastySpinner.setSelection(0);

        // 保存按钮
        findViewById(R.id.add_poem_button).setOnClickListener(v -> {
            if (validateInput()) {
                String poemName = poemNameEditText.getText().toString().trim();
                String writerName = writerNameEditText.getText().toString().trim();
                String content = contentEditText.getText().toString().trim();
                String explanation = explanationEditText.getText().toString().trim();
                String dynasty = dynastySpinner.getSelectedItem().toString();

                Poem poem = new Poem(poemName, writerName, content, dynasty, explanation);
                if (poemDatabaseHelper.insertPoem(poem) > 0) {
                    Toast.makeText(this, "诗词添加成功！", Toast.LENGTH_SHORT).show();
                    clearInput(); // 添加成功后清空输入框
                } else {
                    Toast.makeText(this, "诗词添加失败！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 清空按钮
        findViewById(R.id.clear_poem_button).setOnClickListener(v -> clearInput());
    }

    /**
     * 校验输入数据
     *
     * @return true: 输入有效, false: 输入无效
     */
    private boolean validateInput() {
        if (TextUtils.isEmpty(poemNameEditText.getText().toString().trim())) {
            Toast.makeText(this, "请输入诗词名！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(writerNameEditText.getText().toString().trim())) {
            Toast.makeText(this, "请输入作者名！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(contentEditText.getText().toString().trim())) {
            Toast.makeText(this, "请输入诗词内容！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 清空输入框
     */
    private void clearInput() {
        poemNameEditText.setText("");
        writerNameEditText.setText("");
        contentEditText.setText("");
        explanationEditText.setText("");
        dynastySpinner.setSelection(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        poemDatabaseHelper = PoemDatabaseHelper.getInstance(this);
        poemDatabaseHelper.openWriteDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        poemDatabaseHelper.closeDB();
    }
}