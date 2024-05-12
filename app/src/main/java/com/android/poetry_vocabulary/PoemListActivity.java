package com.android.poetry_vocabulary;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.poetry_vocabulary.adapter.PoemAdapter;
import com.android.poetry_vocabulary.pojo.Poem;
import com.android.poetry_vocabulary.util.PoemDatabaseHelper;

import java.util.List;

public class PoemListActivity extends AppCompatActivity {

    EditText search_edit_text;

//    RadioGroup sort_radio_group, order_radio_group;

    Button search_button;
    PoemDatabaseHelper dbHelper;

    private PoemAdapter poemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_poem_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        findViewById(R.id.exit_button).setOnClickListener(v -> finish());


        search_edit_text = findViewById(R.id.search_edit_text);
//        sort_radio_group = findViewById(R.id.sort_radio_group);
//        order_radio_group = findViewById(R.id.order_radio_group);
        search_button = findViewById(R.id.search_button);
        search_button.setOnClickListener(v -> {
            String keyword = search_edit_text.getText().toString();
//            int sort = sort_radio_group.getCheckedRadioButtonId(); // 0-无，1-按标题，2-按作者，3-朝代
//            int order = order_radio_group.getCheckedRadioButtonId(); // 0-升序，1-降序
            List<Poem> poemList = dbHelper.queryByCondition(keyword);
            poemAdapter.setPoemList(poemList);
        });

        // 获取 RecyclerView 引用
        RecyclerView poemRecyclerView = findViewById(R.id.poem_recycler_view);

        // 设置布局管理器
        poemRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 创建适配器
        poemAdapter = new PoemAdapter(this);

        // 设置适配器
        poemRecyclerView.setAdapter(poemAdapter);

        // 加载数据
        loadPoems();
    }

    private void loadPoems() {
        // 获取数据库操作工具类实例
        dbHelper = PoemDatabaseHelper.getInstance(this);

        // 查询数据
        List<Poem> poemList = dbHelper.queryAllPoems();

        // 更新适配器数据
        poemAdapter.setPoemList(poemList);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 打开数据库连接
        dbHelper.openReadDB();
        poemAdapter.openDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭数据库连接
        dbHelper.closeDB();
        poemAdapter.closeDatabase();
    }
}