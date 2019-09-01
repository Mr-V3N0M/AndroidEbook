package com.example.shwenyarmya.Adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.util.TypedValue;
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

public class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private String type;
    private int columnWidth;
    private List<AuthorList> authorLists;

    public AuthorAdapter(Activity activity, List<AuthorList> authorLists, String type, InterstitialAdView interstitialAdView) {
        this.activity = activity;
        this.type = type;
        this.authorLists = authorLists;
        method = new Method(activity, interstitialAdView);
        Resources r = activity.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());
        columnWidth = (int) ((method.getScreenWidth() - ((15 + 1) * padding)));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.author_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.imageView.setLayoutParams(new LinearLayout.LayoutParams(columnWidth / 3, columnWidth / 3));

        holder.textView_Name.setText(authorLists.get(position).getAuthor_name());
        holder.textView_Name.setTypeface(Method.scriptable);
        Picasso.get().load(authorLists.get(position).getAuthor_image())
                .placeholder(R.drawable.placeholder_portable)
                .into(holder.imageView);

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
        private LinearLayout relativeLayout;
        private TextView textView_Name;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_author_adapter);
            relativeLayout = itemView.findViewById(R.id.relativeLayout_author_adapter);
            textView_Name = itemView.findViewById(R.id.textView_authorName_adapter);

        }
    }
}
