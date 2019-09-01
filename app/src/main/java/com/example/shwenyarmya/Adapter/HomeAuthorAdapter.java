package com.example.shwenyarmya.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.AuthorList;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.Method;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAuthorAdapter extends RecyclerView.Adapter<HomeAuthorAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private String type;
    private List<AuthorList> authorLists;

    public HomeAuthorAdapter(Activity activity, List<AuthorList> authorLists, String type, InterstitialAdView interstitialAdView) {
        this.activity = activity;
        this.type = type;
        this.authorLists = authorLists;
        method = new Method(activity, interstitialAdView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.home_author_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        Picasso.get().load(authorLists.get(position).getAuthor_image())
                .placeholder(R.drawable.placeholder_portable)
                .into(holder.imageView);

        holder.textView.setText(authorLists.get(position).getAuthor_name());

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                method.interstitialAdShow(position, type, authorLists.get(position).getAuthor_id(), authorLists.get(position).getAuthor_name(), "", "");
            }
        });

    }

    @Override
    public int getItemCount() {
        return authorLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;
        private TextView textView;
        private LinearLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_home_author_adapter);
            textView = itemView.findViewById(R.id.textView_home_author_adapter);
            relativeLayout = itemView.findViewById(R.id.relativeLayout_home_author_adapter);

        }
    }
}
