package com.example.shwenyarmya.Fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.onesignal.OneSignal;
import com.example.shwenyarmya.Activity.AboutUs;
import com.example.shwenyarmya.Activity.MainActivity;
import com.example.shwenyarmya.Activity.PrivacyPolicy;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.YouApplication;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class SettingFragment extends Fragment {

    private YouApplication MyApp;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        MainActivity.toolbar.setTitle(getResources().getString(R.string.action_settings));

        MyApp = YouApplication.getInstance();

        SwitchCompat notificationSwitch = rootView.findViewById(R.id.switch_notification);
        TextView textView_shareApp = rootView.findViewById(R.id.textView_shareApp_setting);
        TextView textView_rateApp = rootView.findViewById(R.id.textView_rateApp_setting);
        TextView textView_moreApp = rootView.findViewById(R.id.textView_moreApp_setting);
        TextView textView_privacy_policy = rootView.findViewById(R.id.textView_privacy_policy_setting);
        TextView textView_aboutUs = rootView.findViewById(R.id.textView_aboutUs_setting);

        notificationSwitch.setChecked(MyApp.getNotification());

        textView_shareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    String sAux = "\n" + getResources().getString(R.string.Let_me_recommend_you_this_application) + "\n\n";
                    sAux = sAux + "https://play.google.com/store/apps/details?id=" + requireActivity().getPackageName();
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "choose one"));
                } catch (Exception e) {
                }

            }
        });

        textView_rateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("market://details?id=" + requireActivity().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + requireActivity().getPackageName())));
                }
            }
        });

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyApp.saveIsNotification(isChecked);
                OneSignal.setSubscription(isChecked);
            }
        });

        textView_aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_ab = new Intent(requireActivity(), AboutUs.class);
                startActivity(intent_ab);
            }
        });

        textView_privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_pri = new Intent(requireActivity(), PrivacyPolicy.class);
                startActivity(intent_pri);
            }
        });

        textView_moreApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_app))));
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }
}
