package com.example.sound_vanilla3;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class RegisterActivity extends AppCompatActivity {

//    public interface RetrofitAPI {
//        @GET("119.67.63.55:8080/api/v1/person")
//        Call<List<Post>> getData(@Query("userId") String id);
//
//        @FormUrlEncoded
//        @POST("119.67.63.55:8080/api/v1/person")
//        Call<Post> postData(@FieldMap HashMap<String, Object> param);
//    }

    private EditText et_id, et_password;
    private Button btn_regi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_id = findViewById(R.id.et_id);
        et_password = findViewById(R.id.et_password);
        btn_regi = findViewById(R.id.btn_register);


        btn_regi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userID = et_id.getText().toString();
                String userPassword = et_password.getText().toString();

                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("email", userID);
                    jsonObject.put("password", userPassword);
                }catch (JSONException e){
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(),jsonObject.toString(), Toast.LENGTH_SHORT).show();

                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {

                            String host_url = "http://119.67.63.55:8080/api/v1/person";
                            HttpURLConnection con = null;

                            URL url = new URL(host_url);
                            con =(HttpURLConnection) url.openConnection();

                            con.setRequestMethod("PUT");
                            con.setRequestProperty("Content-type","application/json");

                            con.setDoOutput(true);
                            con.setDoInput(true);
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));

                            bw.write(jsonObject.toString());
                            bw.flush();
                            bw.close();

                            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            String returnMsg = in.readLine();
                            Log.d("MSG", "===================================" );
                            Log.d("MSG", "MSG : " + returnMsg);
                            Log.d("MSG", "===================================" );

                            int responseCode = con.getResponseCode();
                            Log.d("MSG", "===================================" );
                            Log.d("MSG", "MSG : " + responseCode);
                            Log.d("MSG", "===================================" );

                            if(responseCode == 200){
                                Toast.makeText(getApplicationContext(),"Success : 로그인 페이지로 넘어감~", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }else if(responseCode == 400){
                                Toast.makeText(getApplicationContext(),"중복?", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getApplicationContext(),"Server connection error", Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();


            }
        });

    }
}