package com.android.poetry_vocabulary;

import android.os.Bundle;
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

    EditText poem_name_edit_text, writer_name_edit_text, content_edit_text, explanation_edit_text;
    Spinner dynasty_spinner;

    PoemDatabaseHelper poemDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_poem);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        poem_name_edit_text = findViewById(R.id.poem_name_edit_text);
        writer_name_edit_text = findViewById(R.id.writer_name_edit_text);
        content_edit_text = findViewById(R.id.content_edit_text);
        explanation_edit_text = findViewById(R.id.explanation_edit_text);

        // 设置spinner
        dynasty_spinner = findViewById(R.id.dynasty_spinner);
        // 设置适配器
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.dynasty_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dynasty_spinner.setAdapter(adapter);
        // 设置默认选中项
        dynasty_spinner.setSelection(0);


        // 退出按钮
        findViewById(R.id.exits_poem_button).setOnClickListener(v -> finish());
        // 保存按钮
        findViewById(R.id.add_poem_button).setOnClickListener(v -> {
            String poem_name = poem_name_edit_text.getText().toString();
            String writer_name = writer_name_edit_text.getText().toString();
            String content = content_edit_text.getText().toString();
            String explanation = explanation_edit_text.getText().toString();
            String dynasty = dynasty_spinner.getSelectedItem().toString();
            Poem poem = new Poem(poem_name, writer_name, content, dynasty, explanation);
            if (PoemDatabaseHelper.getInstance(this).insertPoem(poem) > 0) {
                Toast.makeText(this, "诗词添加成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "诗词添加失败！", Toast.LENGTH_SHORT).show();
            }
        });
        // 清空按钮
        findViewById(R.id.clear_poem_button).setOnClickListener(v -> {
            poem_name_edit_text.setText("");
            writer_name_edit_text.setText("");
            content_edit_text.setText("");
            explanation_edit_text.setText("");
            dynasty_spinner.setSelection(0);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        poemDatabaseHelper = PoemDatabaseHelper.getInstance(this);
        // 打开数据库写对象
        poemDatabaseHelper.openWriteDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭数据库写对象
        poemDatabaseHelper.closeDB();
    }
}