package com.example.imusic.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.imusic.R;

public class SplashFragment_2 extends Fragment {
    private TextView splash_skip;
    private View mView;
    public SplashFragment_2(){

    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.splash_fragment_2, container, false);
        splash_skip = mView.findViewById(R.id.splash_skip);
        splash_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.splash_fragment_2,new SplashFragment_5()).addToBackStack(null).commit();
            }
        });

        return  mView;
    }
    public void finish(){
        SplashFragment_2.this.finish();
    }
}
