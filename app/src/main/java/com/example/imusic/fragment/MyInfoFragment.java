package com.example.imusic.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.imusic.R;
import com.example.imusic.activity.RegisterAndLoginActivity;
import com.example.imusic.utils.AnalysisUtils;

public class MyInfoFragment extends Fragment {
    private View view;
    private LinearLayout login_btn,ll_myinfo;
    private TextView tv_myinfo_username;
    public MyInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_info, container, false);
        init();
        setLoginParams(readLoginStatus());
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), RegisterAndLoginActivity.class);
                getActivity().startActivityForResult(intent,1);
            }
        });

        return view;
    }
    private void init(){
        login_btn = view.findViewById(R.id.login_btn);
        tv_myinfo_username = view.findViewById(R.id.tv_myinfo_username);
        ll_myinfo = view.findViewById(R.id.ll_myinfo);
    }

    public void setLoginParams(boolean isLogin) {
        if(isLogin){
            ll_myinfo.setVisibility(View.VISIBLE);
            login_btn.setVisibility(View.GONE);
            tv_myinfo_username.setText(AnalysisUtils.readLoginUserName(getActivity()));
        }else{
            ll_myinfo.setVisibility(View.GONE);
            login_btn.setVisibility(View.VISIBLE);
        }
    }
    private boolean readLoginStatus() {
        SharedPreferences sp = getActivity().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        boolean isLogin = sp.getBoolean("isLogin",false);
        return isLogin;
    }


}
