package com.example.essaycorrect.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.essaycorrect.R;
import com.example.essaycorrect.entity.Article;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    private List<Article> articleList;
    private OnItemClickListener onItemClickListener;

    // 定义点击事件接口
    public interface OnItemClickListener {
        void onItemClick(Article article, int position);
    }

    // 设置点击监听器的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public ArticleAdapter(List<Article> articleList) {
        this.articleList = articleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_article, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articleList.get(position);
        holder.titleTextView.setText(article.getTitle());
        holder.contentTextView.setText(article.getContent());
        holder.timeTextView.setText(formatTime(article.getCreateTime()));

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(article, position);
            }
        });

        // 还可以设置长按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemClickListener != null) {
                // 这里可以处理长按事件
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;
        TextView timeTextView;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }

    public static String formatTime(String originalTime) {
        if (originalTime == null || originalTime.isEmpty()) {
            return "未知时间";
        }

        try {
            // 解析原始ISO 8601格式时间
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
            Date date = originalFormat.parse(originalTime);

            // 格式化为更友好的显示格式：yyyy-MM-dd HH:mm
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return targetFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            // 如果解析失败，尝试其他可能的格式
            return tryAlternativeFormats(originalTime);
        }
    }

    /**
     * 尝试其他可能的时间格式
     */
    private static String tryAlternativeFormats(String timeString) {
        try {
            // 尝试不带毫秒的格式
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
            Date date = format1.parse(timeString);
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return targetFormat.format(date);
        } catch (ParseException e1) {
            try {
                // 尝试最简单的格式
                SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = format2.parse(timeString);
                SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                return targetFormat.format(date);
            } catch (ParseException e2) {
                // 如果所有格式都解析失败，返回原始字符串或截断T字符
                return timeString.replace("T", " ");
            }
        }
    }
}