package com.android.poetry_vocabulary.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.poetry_vocabulary.PoemLearnActivity;
import com.android.poetry_vocabulary.R;
import com.android.poetry_vocabulary.pojo.Poem;
import com.android.poetry_vocabulary.util.PoemDatabaseHelper;

import java.util.List;

public class PoemAdapter extends RecyclerView.Adapter<PoemAdapter.PoemViewHolder> {

    private final Context context;
    private List<Poem> poemList;
    PoemDatabaseHelper poemDatabaseHelper;

    public void openDatabase() {
        poemDatabaseHelper.openWriteDB();
    }


    public void closeDatabase() {
        poemDatabaseHelper.closeDB();
    }

    public PoemAdapter(Context context) {
        this.context = context;
        poemDatabaseHelper = PoemDatabaseHelper.getInstance(context);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setPoemList(List<Poem> poemList) {
        this.poemList = poemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PoemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_poem, parent, false);
        return new PoemViewHolder(itemView);
    }

    private String formatContent(String content) {
        StringBuilder formattedContent = new StringBuilder();
        String[] lines = content.split(" ");
        for (String line : lines) {
            formattedContent.append(line).append("\n");
        }
        return formattedContent.toString();
    }

    @Override
    public void onBindViewHolder(@NonNull PoemViewHolder holder, int position) {
        Poem poem = poemList.get(position);
        holder.poemNameTextView.setText(poem.getPoemName());
        holder.writerNameTextView.setText(poem.getWriterName());
//        holder.dynastyTextView.setText(poem.getDynasty());

        holder.contentTextView.setText(formatContent(poem.getContent()));

//        holder.commentTextView.setText(poem.getExplanation());
        holder.deleteButton.setOnClickListener(e -> {
            poemList.remove(position);
            if (poemDatabaseHelper.deletePoem(poem.getPoemId()) > 0) {
                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                notifyItemRemoved(position);
            } else {
                Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show();
            }
        });
        holder.learn_button.setOnClickListener(e -> {
            Bundle bundle = new Bundle();
            bundle.putLong("poem_id", poem.getPoemId());
            bundle.putString("poem_name", poem.getPoemName());
            bundle.putString("writer_name", poem.getWriterName());
            bundle.putString("dynasty", poem.getDynasty());
            bundle.putString("content", poem.getContent());
            bundle.putString("explanation", poem.getExplanation());
            Intent intent = new Intent(context, PoemLearnActivity.class);
            intent.putExtras(bundle);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return poemList == null ? 0 : poemList.size();
    }

    static class PoemViewHolder extends RecyclerView.ViewHolder {
        TextView poemNameTextView;
        TextView writerNameTextView;
//        TextView dynastyTextView;
        TextView contentTextView;
//        TextView commentTextView;
        Button deleteButton;
        Button learn_button;

        PoemViewHolder(View itemView) {
            super(itemView);
            poemNameTextView = itemView.findViewById(R.id.poem_name_text_view);
            writerNameTextView = itemView.findViewById(R.id.writer_name_text_view);
//            dynastyTextView = itemView.findViewById(R.id.dynasty_text_view);
            contentTextView = itemView.findViewById(R.id.content_text_view);
//            commentTextView = itemView.findViewById(R.id.comment_text_view);
            deleteButton = itemView.findViewById(R.id.delete_button);
            learn_button = itemView.findViewById(R.id.learn_button);
        }
    }
}