package com.example.imusic.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imusic.R;
import com.example.imusic.utils.MD5Utils;

public class RegisterAndLoginActivity extends AppCompatActivity {
    private TextView tv_back,tv_login_btn,tv_register_btn;
    private Button btn_register,btn_login;
    private EditText et_username,et_psw,et_psw_again;
    private String userName,psw,pswAgain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
        setLoginStatus();           //将登入样式设置为最初样式

        //返回被点击
        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterAndLoginActivity.this.finish();
            }
        });
        //注册按钮点击
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEditString();
                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(RegisterAndLoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(psw)){
                    Toast.makeText(RegisterAndLoginActivity.this, "请输入密码",Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(pswAgain)){
                    Toast.makeText(RegisterAndLoginActivity.this, "请再次输入密码",Toast.LENGTH_SHORT).show();
                    return;
                }else if(!psw.equals(pswAgain)){
                    Toast.makeText(RegisterAndLoginActivity.this, "输入两次的密码不一样",Toast.LENGTH_SHORT).show();
                    return;
                }else if(isExistUserName(userName)){
                    Toast.makeText(RegisterAndLoginActivity.this, "此账户已经存在",Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    Toast.makeText(RegisterAndLoginActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                    saveRegisterInfo(userName, psw);
                    clearEdit();
                    setLoginStatus();
                    et_username.setText(userName);
                    et_username.setSelection(userName.length());
                }
            }
        });
        //登入按钮点击
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = et_username.getText().toString().trim();
                psw = et_psw.getText().toString().trim();
                String md5Psw = MD5Utils.md5(psw);
                pswAgain = readPsw(userName);
                if (TextUtils.isEmpty(userName)){
                    Toast.makeText(RegisterAndLoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }else if (TextUtils.isEmpty(psw)){
                    Toast.makeText(RegisterAndLoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }else if (md5Psw.equals(pswAgain)){
                    Toast.makeText(RegisterAndLoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                    saveLoginStatus(true, userName);
                    Intent data = new Intent();
                    data.putExtra("isLogin", true);
                    setResult(RESULT_OK, data);
                    RegisterAndLoginActivity.this.finish();
                    return;
                } else if ((!TextUtils.isEmpty(pswAgain) && !md5Psw.equals(pswAgain))) {
                    Toast.makeText(RegisterAndLoginActivity.this, "输入的用户名和密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    Toast.makeText(RegisterAndLoginActivity.this, "此用户名不存在,请先注册",Toast.LENGTH_SHORT).show();
                }
            }
        });


        //顶部导航登入
        tv_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLoginStatus();
            }
        });
        //顶部导航注册
        tv_register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRegisterStatus();

            }
        });
    }

    //保存用户名，与密码
    private void saveRegisterInfo(String userName, String psw){
        String md5Psw = MD5Utils.md5(psw);
        SharedPreferences sp = getSharedPreferences("loginInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(userName, md5Psw);
        editor.commit();
    }
    //再SharedPregerences 中读取用户名，判断是否存在
    private boolean isExistUserName(String userName){
        boolean has_userName = false;
        SharedPreferences sp = getSharedPreferences("loginInfo", MODE_PRIVATE);
        String spPsw = sp.getString(userName, "");
        if (!TextUtils.isEmpty(spPsw)){
            has_userName = true;
        }
        return has_userName;
    }
    //根据用户名读取密码
    private String readPsw(String userName){
        SharedPreferences sp = getSharedPreferences("loginInfo",MODE_PRIVATE);
        return sp.getString(userName,"");
    }
    //保存登入状态和用户名
    private void saveLoginStatus(boolean status, String userName){
        SharedPreferences sp = getSharedPreferences("loginInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isLogin", status);
        editor.putString("loginUserName", userName);
        editor.commit();
    }


//获取控件
    private void init() {
        tv_back = findViewById(R.id.tv_back);
        btn_register = findViewById(R.id.btn_register);
        btn_login = findViewById(R.id.btn_login);
        et_username = findViewById(R.id.et_username);
        et_psw = findViewById(R.id.et_psw);
        et_psw_again = findViewById(R.id.et_psw_again);
        tv_login_btn = findViewById(R.id.tv_login_btn);
        tv_register_btn = findViewById(R.id.tv_register_btn);
    }
    private void getEditString(){
        userName = et_username.getText().toString().trim();
        psw = et_psw.getText().toString().trim();
        pswAgain = et_psw_again.getText().toString().trim();
    }



    //初始样式
    private void setInitStatus(){
        tv_register_btn.setBackgroundColor(Color.parseColor("#ffffff"));
        tv_register_btn.setTextColor(Color.parseColor("#4a5396"));
        tv_login_btn.setBackgroundColor(Color.parseColor("#ffffff"));
        tv_login_btn.setTextColor(Color.parseColor("#4a5396"));
        et_psw.setText("");
        et_psw_again.setText("");
        btn_login.setVisibility(View.GONE);
        btn_register.setVisibility(View.GONE);
    }
    //设置为注册样式
    private void setRegisterStatus(){
        setInitStatus();
        tv_register_btn.setBackgroundColor(Color.parseColor("#4a5396"));
        tv_register_btn.setTextColor(Color.parseColor("#ffffff"));
        et_psw_again.setVisibility(View.VISIBLE);
        btn_register.setVisibility(View.VISIBLE);
    }
    //设置为登入样式
    private void setLoginStatus(){
        setInitStatus();
        tv_login_btn.setBackgroundColor(Color.parseColor("#4a5396"));
        tv_login_btn.setTextColor(Color.parseColor("#ffffff"));
        et_psw_again.setVisibility(View.GONE);
        btn_login.setVisibility(View.VISIBLE);
    }
    //清除文本框内容
    private void clearEdit(){
        et_username.setText("");
        et_psw.setText("");
        et_psw_again.setText("");
    }
}
