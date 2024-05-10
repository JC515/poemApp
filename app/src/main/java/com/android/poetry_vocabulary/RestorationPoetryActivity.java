package com.android.poetry_vocabulary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.poetry_vocabulary.pojo.Poem;
import com.android.poetry_vocabulary.util.PoemDatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RestorationPoetryActivity extends AppCompatActivity {
    // 诗句原文，用于校验答案
    private String originalPoem;
    // 可变的诗句，用于复原
    private String poem;
    // 选中的按钮
    private final List<Button> selectedButtons = new ArrayList<>();
    private LinearLayout rootLayout;
    // 网格布局
    private GridLayout gridLayout;
    // 退出按钮
    private Button exitButton;
    // 校验按钮
    private Button verifyButton;
    // 还原按钮
    private Button restoreButton;
    // 更换按钮
    private Button changeButton;
    // 显示诗词信息
    TextView poemTextView;
    // 记录当前诗词对象
    Poem currentPoem;
    // 诗句数据库
    PoemDatabaseHelper poemDatabaseHelper;
    // 诗句数据
    List<Poem> poemDataList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restoration_poetry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 获取布局
        rootLayout = findViewById(R.id.main);
        // 获取PoemDatabaseHelper实例
        poemDatabaseHelper = PoemDatabaseHelper.getInstance(this);
        // 获取诗词数据
        getPoemData();
        // 设置随机诗句
        setRandomPoem();
        // 添加组件到布局中
        layoutAddView();
    }

    public void layoutAddView() {
        initViews();
        // 添加组件到布局中
        rootLayout.addView(exitButton);
        rootLayout.addView(verifyButton);
        rootLayout.addView(restoreButton);
        rootLayout.addView(poemTextView);
        rootLayout.addView(gridLayout);
        rootLayout.addView(changeButton);
    }

    public void deleteView() {
        // 删除组件
        rootLayout.removeView(exitButton);
        rootLayout.removeView(verifyButton);
        rootLayout.removeView(restoreButton);
        rootLayout.removeView(poemTextView);
        rootLayout.removeView(gridLayout);
        rootLayout.removeView(changeButton);
    }

    public void getPoemData() {
        // 获取诗句数据
        poemDataList = poemDatabaseHelper.queryAllPoems();
    }

    public void setRandomPoem() {
        if (currentPoem == null) {
            // 随机获取诗词
            int randomIndex = (int) (Math.random() * poemDataList.size());
            currentPoem = poemDataList.get(randomIndex);
        } else {
            while (true) {
                int randomIndex = (int) (Math.random() * poemDataList.size());
                Poem tempPoem = poemDataList.get(randomIndex);
                // 确保随机诗句与当前诗句不同
                if (!Objects.equals(tempPoem.getPoemId(), currentPoem.getPoemId())) {
                    currentPoem = tempPoem;
                    break;
                }
            }
        }
        // 设置随机诗句
        originalPoem = currentPoem.getContent();
        // poem是用于被打乱诗句
        poem = originalPoem;
    }

    @SuppressLint("SetTextI18n")
    private void initViews() {
        // 初始化按钮
        exitButton = new Button(this);
        exitButton.setText("退出");
        exitButton.setOnClickListener(v -> finish());

        verifyButton = new Button(this);
        verifyButton.setText("校验");
        verifyButton.setOnClickListener(v -> verifyAnswer());

        restoreButton = new Button(this);
        restoreButton.setText("自动复原");
        restoreButton.setOnClickListener(v -> restorePoem());

        changeButton = new Button(this);
        changeButton.setText("换一首");
        changeButton.setOnClickListener(v -> {
            // 更换诗句
            setRandomPoem();
            // 打乱诗句
            shufflePoem(poem);
            // 更新界面
            updateView();
//            Toast.makeText(this, "更换成功！", Toast.LENGTH_SHORT).show();
        });
        // 初始化网格布局
        initGridLayout();
        // 初始化TextView
        poemTextView = new TextView(this);
        poemTextView.setText(currentPoem.getPoemName() + "\n" + currentPoem.getDynasty() + "\n" + currentPoem.getWriterName());
        poemTextView.setGravity(Gravity.CENTER);
        poemTextView.setTextSize(20);

        poemTextView.setPadding(10, 30, 10, 10);
    }

    public void updateView() {
        // 移除所有子视图
        deleteView();
        gridLayout.removeAllViews();
        // 更新组件，添加到布局中
        layoutAddView();
    }

    private void initGridLayout() {
        gridLayout = new GridLayout(this);
        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        gridLayout.setOrientation(GridLayout.HORIZONTAL);
        gridLayout.setPadding(50, 50, 50, 50);

        poem = shufflePoem(poem);// 随机打乱诗句

        String[] s = poem.split(" ");
        ArrayList<String> list = new ArrayList<>(Arrays.asList(s));

        gridLayout.setColumnCount(list.get(0).length());// 列数为每一句诗句的长度,需确保每一句诗字数相同
        gridLayout.setRowCount(list.size());// 行数为诗句的个数

        gridLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        // 添加按钮到网格布局中
        addWordsToGridLayout(list);
    }

    private void addWordsToGridLayout(ArrayList<String> list) {
        // 将诗句中的每一个字按顺序作为按钮添加到网格布局中
        for (int i = 0; i < list.size(); i++) {
            String[] words = list.get(i).split("");
            for (int j = 0; j < words.length; j++) {
                Button wordButton = createWordButton(i, j, words[j]);// 创建按钮
                gridLayout.addView(wordButton);
            }
        }
    }

    private Button createWordButton(int row, int col, String text) {
        Button wordButton = new Button(this);
        wordButton.setText(text);// 设置按钮文字

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.rowSpec = GridLayout.spec(row, 1f);
        params.columnSpec = GridLayout.spec(col, 1f);
        params.width = 100;// 设置按钮宽度
        params.height = 150;// 设置按钮高度
        wordButton.setLayoutParams(params);

        wordButton.setGravity(Gravity.CENTER);// 设置按钮居中

        // 按钮点击事件,交换选中的两个按钮的文字
        wordButton.setOnClickListener(v -> {
            Button clickedButton = (Button) v;
            if (selectedButtons.isEmpty()) {
                selectedButtons.add(clickedButton);
            } else if (selectedButtons.size() == 1 && selectedButtons.get(0) != clickedButton) {
                Button firstButton = selectedButtons.get(0);
                String temp = firstButton.getText().toString();
                firstButton.setText(clickedButton.getText());
                clickedButton.setText(temp);
                selectedButtons.clear();
            } else {
                selectedButtons.clear();
                selectedButtons.add(clickedButton);
            }
        });

        return wordButton;
    }

    private String shufflePoem(String poem) {
        // 随机打乱诗句
        List<Character> charList = new ArrayList<>();
        for (char c : poem.toCharArray()) {
            if (c != ' ') {
                charList.add(c);
            }
        }
        // 打乱诗句
        Collections.shuffle(charList);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < poem.length(); i++) {
            char c = poem.charAt(i);
            if (c == ' ') {
                sb.append(' ');
            } else {
                sb.append(charList.remove(0));
            }
        }
        return sb.toString();
    }

    private void verifyAnswer() {
        // 校验答案
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            sb.append(button.getText());
        }
        String userAnswer = sb.toString().replace(" ", "");

        if (userAnswer.equals(originalPoem.replace(" ", ""))) {
            Toast.makeText(this, "答案正确!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "答案错误,请继续尝试", Toast.LENGTH_SHORT).show();
        }
    }

    private void restorePoem() {
        // 还原诗句
        selectedButtons.clear();
        gridLayout.removeAllViews();

        String[] s = originalPoem.split(" ");
        ArrayList<String> list = new ArrayList<>(Arrays.asList(s));

        gridLayout.setColumnCount(list.get(0).length());
        gridLayout.setRowCount(list.size());

        addWordsToGridLayout(list);

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
    }
}