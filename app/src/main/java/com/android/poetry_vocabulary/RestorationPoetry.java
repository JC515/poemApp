package com.android.poetry_vocabulary;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RestorationPoetry extends AppCompatActivity {
    // 诗句原文，用于校验答案
    private final String originalPoem = "锄禾日当午 汗滴禾下土 谁知盘中餐 粒粒皆辛苦";
    // 可变的诗句，用于复原
    private String poem = originalPoem;
    // 选中的按钮
    private final List<Button> selectedButtons = new ArrayList<>();
    // 网格布局
    private GridLayout gridLayout;
    // 退出按钮
    private Button exitButton;
    // 校验按钮
    private Button verifyButton;
    // 还原按钮
    private Button restoreButton;


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
        // 初始化按钮
        initButtons();
        // 初始化网格布局
        initGridLayout();
        // 添加按钮到布局中
        LinearLayout layout = findViewById(R.id.main);
        layout.addView(exitButton);
        layout.addView(verifyButton);
        layout.addView(restoreButton);
        layout.addView(gridLayout);
    }

    private void initButtons() {
        exitButton = new Button(this);
        exitButton.setText("退出");
        exitButton.setOnClickListener(v -> finish());

        verifyButton = new Button(this);
        verifyButton.setText("校验");
        verifyButton.setOnClickListener(v -> verifyAnswer());

        restoreButton = new Button(this);
        restoreButton.setText("自动复原");
        restoreButton.setOnClickListener(v -> restorePoem());
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

        Toast.makeText(this, "诗句已还原为原始顺序", Toast.LENGTH_SHORT).show();
    }
}