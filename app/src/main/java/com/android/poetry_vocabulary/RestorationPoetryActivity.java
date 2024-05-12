package com.android.poetry_vocabulary;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.util.Timer;
import java.util.TimerTask;

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
    //    // 退出按钮
//    private Button exitButton;
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

    TextView introduceTextView;


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
        if (poemDataList == null || poemDataList.isEmpty()) {
            // 数据库为空，显示提示信息
            Toast.makeText(this, "数据库为空，请添加诗词", Toast.LENGTH_SHORT).show();
            // 结束当前Activity
            finish();
            return;
        }
        // 设置随机诗句
        setRandomPoem();
        // 添加组件到布局中
        layoutAddView();
    }

    public void layoutAddView() {
        initViews();
        // 添加组件到布局中
        rootLayout.addView(introduceTextView);
        rootLayout.addView(poemTextView);
        rootLayout.addView(gridLayout);
//        rootLayout.addView(exitButton);
        rootLayout.addView(verifyButton);
        rootLayout.addView(restoreButton);
        rootLayout.addView(changeButton);
    }

    public void deleteView() {
        // 删除组件
//        rootLayout.removeView(exitButton);
        rootLayout.removeView(verifyButton);
        rootLayout.removeView(restoreButton);
        rootLayout.removeView(poemTextView);
        rootLayout.removeView(gridLayout);
        rootLayout.removeView(changeButton);
        rootLayout.removeView(introduceTextView);
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

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    private void initViews() {
        // 初始化按钮
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                400,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = 30; // 单位为像素

        verifyButton = new Button(this);
        verifyButton.setLayoutParams(params);
        verifyButton.setText("校验答案");
        verifyButton.setOnClickListener(v -> verifyAnswer());
        verifyButton.setBackgroundResource(R.drawable.rounded_button_bg);

        restoreButton = new Button(this);
        restoreButton.setLayoutParams(params);
        restoreButton.setText("自动复原");
        restoreButton.setOnClickListener(v -> restorePoem());
        restoreButton.setBackgroundResource(R.drawable.rounded_button_bg);

        changeButton = new Button(this);
        changeButton.setLayoutParams(params);
        changeButton.setText("更换诗词");
        changeButton.setBackgroundResource(R.drawable.rounded_button_bg);
        changeButton.setOnClickListener(v -> {
            // 更换诗句
            setRandomPoem();
            // 打乱诗句
            shufflePoem(poem);
            // 更新界面
            updateView();
        });

        // 初始化网格布局
        initGridLayout();

        // 初始化TextView
        poemTextView = new TextView(this);
        poemTextView.setText(currentPoem.getPoemName() + "\n" + currentPoem.getDynasty() + "\n" + currentPoem.getWriterName());
        poemTextView.setGravity(Gravity.CENTER);
        poemTextView.setTextSize(20);
        poemTextView.setPadding(10, 30, 10, 10);

        introduceTextView = new TextView(this);
        introduceTextView.setText("还原诗句");
        introduceTextView.setGravity(Gravity.CENTER);
        introduceTextView.setTextSize(25);
        introduceTextView.setPadding(10, 120, 10, 10);
        introduceTextView.setTextColor(R.color.black);
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
                900));
        // 添加按钮到网格布局中
        addWordsToGridLayout(list);
    }

    private void addWordsToGridLayout(ArrayList<String> list) {
        // 将诗句中的每一个字按顺序作为按钮添加到网格布局中
        for (int i = 0; i < list.size(); i++) {
            String[] words = list.get(i).split("");
            for (int j = 0; j < words.length; j++) {
                Button wordButton = createWordButton(i, j, words[j]); // 创建按钮
                gridLayout.addView(wordButton);

                // 设置初始位置在屏幕外
                wordButton.setTranslationX(gridLayout.getWidth());

                // 创建飞入动画
                ObjectAnimator animator = ObjectAnimator.ofFloat(wordButton, "translationX", 0f);
                animator.setDuration(500); // 动画持续时间 500 毫秒
                animator.setStartDelay(50 * ((long) i * words.length + j)); // 每个按钮延迟 50 毫秒

                animator.start();
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
        //设置按钮背景
        wordButton.setBackgroundResource(R.drawable.rounded_button_bg);

        // 按钮点击事件,交换选中的两个按钮的文字
        wordButton.setOnClickListener(v -> {
            Button clickedButton = (Button) v;

            if (selectedButtons.isEmpty()) {
                // 第一次点击，选中按钮并改变背景
                selectedButtons.add(clickedButton);
                clickedButton.setBackgroundResource(R.drawable.rounded_button_bg_press);
            } else if (selectedButtons.size() == 1 && selectedButtons.get(0) != clickedButton) {
                // 点击第二个按钮，交换文字并恢复背景
                Button firstButton = selectedButtons.get(0);
                String tempText = firstButton.getText().toString();

                // 淡出动画
                Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // 使用 Timer 设置 100ms 延迟
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(() -> {
                                    // 交换文字
                                    firstButton.setText(clickedButton.getText().toString());
                                    clickedButton.setText(tempText);

                                    // 淡入动画
                                    Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                                    firstButton.startAnimation(fadeIn);
                                    clickedButton.startAnimation(fadeIn);

                                    // 恢复背景颜色
                                    firstButton.setBackgroundResource(R.drawable.rounded_button_bg);
                                    clickedButton.setBackgroundResource(R.drawable.rounded_button_bg);
                                });
                            }
                        }, 100); // 延迟 100 毫秒
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                firstButton.startAnimation(fadeOut);
                clickedButton.startAnimation(fadeOut);

                selectedButtons.clear();
            } else {
                // 点击已选中的按钮，取消选中并恢复背景
                selectedButtons.clear();
                clickedButton.setBackgroundResource(R.drawable.rounded_button_bg);
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
        String temp = originalPoem.replace(" ", "");

        boolean isCorrect = userAnswer.equals(originalPoem.replace(" ", ""));

        // 依次检查按钮并设置动画
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            int finalI = i;
            // 使用 Timer 设置延迟
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        // 设置按钮背景颜色
                        if (button.getText().toString().equals(String.valueOf(temp.charAt(finalI)))) {
                            button.setBackgroundResource(R.drawable.rounded_button_bg_green);
                        } else {
                            button.setBackgroundResource(R.drawable.rounded_button_bg_red);
                        }

                        // 创建跳动动画
                        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.2f, 1f);
                        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.2f, 1f);
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.playTogether(scaleXAnimator, scaleYAnimator);
                        animatorSet.setDuration(300); // 动画持续时间 300 毫秒
                        animatorSet.start();
                    });
                }
            }, 100L * i); // 每个按钮延迟 100 毫秒
        }

        // 显示提示信息
        if (isCorrect) {
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