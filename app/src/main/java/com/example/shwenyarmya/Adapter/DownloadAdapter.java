package com.example.shwenyarmya.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.example.shwenyarmya.DataBase.DatabaseHandler;
import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.DownloadList;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.Method;

import java.io.File;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private List<DownloadList> downloadLists;
    private String type;
    private int lastPosition = -1;
    private DatabaseHandler db;

    public DownloadAdapter(Activity activity, List<DownloadList> downloadLists, String type, InterstitialAdView interstitialAdView) {
        this.activity = activity;
        this.type = type;
        this.downloadLists = downloadLists;
        db = new DatabaseHandler(activity);
        method = new Method(activity, interstitialAdView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.download_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.textView_Name.setText(downloadLists.get(position).getTitle());
        holder.textView_Name.setTypeface(Method.scriptable);
        holder.textView_author.setText(activity.getString(R.string.author_by) + "\u0020" + downloadLists.get(position).getAuthor());

        Picasso.get().load("file://" + downloadLists.get(position).getImage()).placeholder(R.drawable.placeholder_landscap).into(holder.imageView);

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                method.interstitialAdShow(position, type, downloadLists.get(position).getId(), downloadLists.get(position).getTitle(), "", downloadLists.get(position).getUrl());
            }
        });

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!db.checkIdDownloadBook(downloadLists.get(position).getId())) {
                    db.deleteDownloadBook(downloadLists.get(position).getId());
                    File file = new File(downloadLists.get(position).getUrl());
                    File file_image = new File(downloadLists.get(position).getImage());
                    file.delete();
                    file_image.delete();
                    downloadLists.remove(position);
                    notifyDataSetChanged();

                } else {
                    Toast.makeText(activity, activity.getResources().getString(R.string.no_data), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (position % 2 == 1) {
            holder.cardView_category_adapter.setBackgroundResource(R.drawable.download_corner);
        } else {

        }

        if (position > lastPosition) {
            if (position % 2 == 0) {
                Animation animation = AnimationUtils.loadAnimation(activity,
                        R.anim.up_from_bottom_right);
                holder.itemView.startAnimation(animation);
                lastPosition = position;
            } else {
                Animation animation = AnimationUtils.loadAnimation(activity,
                        R.anim.up_from_bottom_left);
                holder.itemView.startAnimation(animation);
                lastPosition = position;
            }
        }

    }

    @Override
    public int getItemCount() {
        return downloadLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout relativeLayout;
        private RoundedImageView imageView;
        private TextView textView_Name, textView_author;
        private Button button;
        LinearLayout cardView_category_adapter;


        public ViewHolder(View itemView) {
            super(itemView);

            relativeLayout = itemView.findViewById(R.id.relativelayout_download_adapter);
            button = itemView.findViewById(R.id.button_delete_adapter);
            imageView = itemView.findViewById(R.id.imageView_download_adapter);
            textView_Name = itemView.findViewById(R.id.textViewName_download_adapter);
            textView_author = itemView.findViewById(R.id.textView_subTitle_download_adapter);
            cardView_category_adapter = itemView.findViewById(R.id.cardView_category_adapter);

        }
    }
}
