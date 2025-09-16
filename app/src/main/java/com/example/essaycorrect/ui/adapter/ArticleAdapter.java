package com.example.essaycorrect.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.essaycorrect.R;
import com.example.essaycorrect.data.model.Article;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    private List<Article> articleList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Article article, int position);
    }

    public ArticleAdapter(List<Article> articleList) {
        this.articleList = articleList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
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

        // 设置长按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemClickListener != null) {
                // 可以处理长按事件，比如显示删除选项
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return articleList != null ? articleList.size() : 0;
    }

    /**
     * 更新数据
     */
    public void updateArticles(List<Article> newArticles) {
        this.articleList = newArticles;
        notifyDataSetChanged();
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

    /**
     * 格式化时间显示
     */
    public static String formatTime(String originalTime) {
        if (originalTime == null || originalTime.isEmpty()) {
            return "未知时间";
        }

        try {
            // 解析原始ISO 8601格式时间
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
            Date date = originalFormat.parse(originalTime);

            // 格式化为更友好的显示格式
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return targetFormat.format(date);

        } catch (ParseException e) {
            // 如果解析失败，尝试其他可能的格式
            return tryAlternativeFormats(originalTime);
        }
    }

    /**
     * 尝试其他可能的时间格式
     */
    private static String tryAlternativeFormats(String timeString) {
        String[] formats = {
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
                Date date = dateFormat.parse(timeString);
                SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                return targetFormat.format(date);
            } catch (ParseException e) {
                // 继续尝试下一个格式
            }
        }

        // 如果所有格式都解析失败，返回处理过的字符串
        return timeString.replace("T", " ");
    }
}