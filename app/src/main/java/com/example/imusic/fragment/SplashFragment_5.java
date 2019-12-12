package com.example.imusic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.imusic.R;
import com.example.imusic.activity.MainActivity;

public class SplashFragment_5 extends Fragment {
    private Button splash_bn;
    private View mView;
    public SplashFragment_5(){

    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.splash_fragment_5, container, false);
        splash_bn = mView.findViewById(R.id.splash_bn);
        splash_bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                SplashFragment_5.this.finish();
            }
        });
        return  mView;
    }
    public void finish(){

    }
}
