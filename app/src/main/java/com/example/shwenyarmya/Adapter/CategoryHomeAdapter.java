package com.example.shwenyarmya.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.CategoryList;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.Method;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class CategoryHomeAdapter extends RecyclerView.Adapter<CategoryHomeAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private String type;
    private List<CategoryList> categoryLists;
    private int lastPosition = -1;

    public CategoryHomeAdapter(Activity activity, List<CategoryList> categoryLists, String type, InterstitialAdView interstitialAdView) {
        this.activity = activity;
        this.type = type;
        this.categoryLists = categoryLists;
        method = new Method(activity, interstitialAdView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.category_home_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.textView_Name.setText(categoryLists.get(position).getCategory_name());
        holder.textView_Name.setTypeface(Method.scriptable);
        String totalBook = "(" + categoryLists.get(position).getTotal_books() + ") " + activity.getResources().getString(R.string.items_string_category_adapter);
        holder.textView_categoryCount.setText(totalBook);
        Picasso.get().load(categoryLists.get(position).getcat_image())
                .placeholder(R.drawable.placeholder_portable)
                .into(holder.imageView_category_adapter);

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

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                method.interstitialAdShow(position, type, categoryLists.get(position).getCid(), categoryLists.get(position).getCategory_name(), "", "");
            }
        });

    }

    @Override
    public int getItemCount() {
        return categoryLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout relativeLayout;
        private TextView textView_Name, textView_categoryCount;
        RoundedImageView imageView_category_adapter;

        public ViewHolder(View itemView) {
            super(itemView);

            relativeLayout = itemView.findViewById(R.id.relativelayout_category_adapter);
            textView_Name = itemView.findViewById(R.id.textViewName_category_adapter);
            textView_categoryCount = itemView.findViewById(R.id.textView_categoryCount_category_adapter);
            imageView_category_adapter = itemView.findViewById(R.id.imageView_category_adapter);
        }
    }
}
