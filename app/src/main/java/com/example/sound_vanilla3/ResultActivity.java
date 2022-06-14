package com.example.sound_vanilla3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ResultActivity extends AppCompatActivity {


    private Button btn_goMain;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        textView = findViewById(R.id.score);

        btn_goMain = findViewById(R.id.btn_GoMain);
        btn_goMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String host_url = "http://119.67.63.55:8080/api/v2/score";
//                    HttpURLConnection con = null;
//                    URL url = new URL(host_url);
//                    con = (HttpURLConnection) url.openConnection();
//                    con.setDoInput(true);
//                    con.setDoOutput(true);
//
//                    con.setRequestMethod("POST");
//                    con.setRequestProperty("Content-Type", "application/json");
//                    con.setChunkedStreamingMode(0);
//
//                    OutputStream out = new BufferedOutputStream(con.getOutputStream());
//                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out,"UTF-8"));
//                    writer.write(score);
//                    writer.flush();
//
//                }catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        thread.start();
//
//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        textView.setText("점수 : " + scaleDetector.score);


    }


}