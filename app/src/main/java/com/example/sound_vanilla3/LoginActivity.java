package com.example.sound_vanilla3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.HttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText et_id, et_password;
    private Button btn_Goregister, btn_login, btn_GoogleLogin;

    int rCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_id = findViewById(R.id.login_id);
        et_password = findViewById(R.id.login_password);
        btn_GoogleLogin = findViewById(R.id.btn_GoogleLogin);
        btn_login = findViewById(R.id.btn_login);
        btn_Goregister = findViewById(R.id.btn_GoRegister);


        // 회원가입 눌렀을 때 Register Activity로 이동.
        btn_Goregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 로그인 눌렀을 때 행동
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //JsonObject 만들기
                String userID = et_id.getText().toString();
                String userPassword = et_password.getText().toString();
                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("email", userID);
                    jsonObject.put("password", userPassword);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("JSON", "===================================");
                Log.d("JSON", "JSON : " + jsonObject);
                Log.d("JSON", "===================================");
//                Toast.makeText(getApplicationContext(),jsonObject.toString(), Toast.LENGTH_SHORT).show();

                //POST 전송해보기
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {

                            String host_url = "https://webhook.site/a90b0918-38c0-4c59-ae55-f4af0e74063a";
//                            String host_url = "http://119.67.63.55:8080/api/v1/person";
                            HttpURLConnection con = null;

                            URL url = new URL(host_url);
                            con = (HttpURLConnection) url.openConnection();

                            con.setRequestMethod("POST");
                            con.setRequestProperty("Content-type", "application/json");

                            con.setDoOutput(true);
                            con.setDoInput(true);
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));

                            bw.write(jsonObject.toString());
                            bw.flush();
                            bw.close();

                            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            String returnMsg = in.readLine();
                            Log.d("MSG", "===================================");
                            Log.d("MSG", "MSG : " + returnMsg);
                            Log.d("MSG", "===================================");

                            int responseCode = con.getResponseCode();
                            rCode = responseCode;
                            Log.d("MSG", "===================================");
                            Log.d("MSG", "MSG : " + responseCode);
                            Log.d("MSG", "===================================");
                            if (responseCode == 400) {
                                Log.d("error : 400", "===================================");
                            } else if (responseCode == 500) {
                                Log.d("error : 500", "===================================");
                            } else {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();

                try {
                    thread.join();
                    //jsonObject.put("password", userPassword);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.d("aaa:", rCode + "===================================");

                if (rCode / 100 == 4) {
                    Log.d("error : 400", "===================================");
                } else if (rCode / 100 == 5) {
                    Log.d("error : 500", "===================================");
                } else {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }

            }
        });

        //  구글 로그인
        btn_GoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


        //================ onCreate 안 ===================
    }
}