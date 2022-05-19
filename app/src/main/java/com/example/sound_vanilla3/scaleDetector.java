package com.example.sound_vanilla3;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONException;
import org.json.JSONObject;
import org.jtransforms.fft.DoubleFFT_1D;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.*;

import ca.uol.aig.fftpack.RealDoubleFFT;
import android.media.audiofx.NoiseSuppressor;

// FFT(Fast Fourier Transform) DFT 알고리즘 : 데이터를 시간 기준(time base)에서 주파수 기준(frequency base)으로 바꾸는데 사용.

public class scaleDetector extends Activity implements OnClickListener {

    int frequency = 8192; //주파수가 8192일 경우 4096 까지 측정이 가능함
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    //
    private RealDoubleFFT transformer;
    int blockSize = 4096; // 2048->1024개의 배열이 나옴. 배열 한 칸당 4hz의 범위를 포함하고 있음. //4096->배열 2048이고 한칸당 2hz //배열 번호 1씩 증가-> hz는 2씩 증가한다.
    //배열이 40일때 hz는 80헤르츠를 가지고있다는것.
    DoubleFFT_1D fft = new DoubleFFT_1D(blockSize); //JTransform 라이브러리로 FFT 수행

    //frequency -> 측정 주파수 대역으로 퓨리에 변환 시 f/2 만큼의 크기의 주파수를 분석 할 수 있음.
    //blockSize -> 한 분기마다 측정하는 사이즈로 double 배열로 저장 시 , b/2 개의 배열이 나옴. f/b -> 배열 하나에 할당되는 주파수 범위로 8192/2048 -> 4Hz임

    Button startStopButton;

    int aaa = 0;

    boolean started = false;
    // RecordAudio는 여기에서 정의되는 내부 클래스로서 AsyncTask를 확장한다.
    RecordAudio recordTask;
    // Bitmap 이미지를 표시하기 위해 ImageView를 사용한다. 이 이미지는 현재 오디오 스트림에서 주파수들의 레벨을 나타낸다.
    // 이 레벨들을 그리려면 Bitmap에서 구성한 Canvas 객체와 Paint객체가 필요하다.
    private static final String TAG = scaleDetector.class.getSimpleName();


    List<Double> f253 = new LinkedList<>();
    List<Double> f254 = new LinkedList<>();
    List<Double> f255 = new LinkedList<>();

    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;

    BarChart chart;
    ArrayList xlabels = new ArrayList();
    ArrayList ylabels = new ArrayList();
    BarData data;

    List<String> scaleBuffer = new ArrayList<>();
    JSONObject jsonObject = new JSONObject();

    TextView t0;
    TextView t1;
    TextView t2;
    TextView t3;

    //스레드 관련 부분 1
   // scaleThread scThr = new scaleThread();
      double[] mag = new double[blockSize/2];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_scale);

        startStopButton = (Button) findViewById(R.id.StartStopButton);
        startStopButton.setOnClickListener(this);

        // RealDoubleFFT 클래스 컨스트럭터는 한번에 처리할 샘플들의 수를 받는다. 그리고 출력될 주파수 범위들의 수를 나타낸다.
        transformer = new RealDoubleFFT(blockSize);

        // ImageView 및 관련 객체 설정 부분
       // imageView = (ImageView) findViewById(R.id.ImageView01);
        bitmap = Bitmap.createBitmap((int) blockSize/2, (int) 200, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
      //  imageView.setImageBitmap(bitmap);

        t0 = (TextView)findViewById(R.id.HzText0);
        t1 = (TextView)findViewById(R.id.HzText1);
        t2 = (TextView)findViewById(R.id.HzText2);
        t3 = (TextView)findViewById(R.id.HzText3);

        chart =(BarChart)findViewById(R.id.chart);
        YAxis leftYAxis = chart.getAxisLeft();
        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinValue(0);
        xAxis.setAxisMaxValue((float)1024);
        leftYAxis.setAxisMaxValue((float)200);
        leftYAxis.setAxisMinValue(0);
        chart.getAxisRight().setEnabled(false);

        //chart 그리기
        int xChart=0;
        //x축 라벨 추가
        //4096 / 16 =256 씩 16칸으로 할거임
        for(int i=0; i<1024; i++){
            xlabels.add(Integer.toString(xChart));
            xChart=xChart+1;
        }

        //초기 데이터
        ylabels.add(new BarEntry(2.2f,0));
        ylabels.add(new BarEntry(10f,512));
        ylabels.add(new BarEntry(63.f,800));
        ylabels.add(new BarEntry(70.f,900));

        BarDataSet barDataSet = new BarDataSet(ylabels,"Hz");
      //  chart.animateY(5000);
        data = new BarData(xlabels,barDataSet); //MPAndroidChart v3.1 에서 오류나서 다른 버전 사용
       // barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        chart.setData(data);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(scaleDetector.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        //오디오 녹음을 사용할 것인지 권한 여부를 체크해주는 코드로, 없으면 동작 안됨! +) AndroidManifest에도 오디오 권한 부분 추가되있음
    }

    // 이 액티비티의 작업들은 대부분 RecordAudio라는 클래스에서 진행된다. 이 클래스는 AsyncTask를 확장한다.
    // AsyncTask를 사용하면 사용자 인터페이스를 멍하니 있게 하는 메소드들을 별도의 스레드로 실행한다.
    // doInBackground 메소드에 둘 수 있는 것이면 뭐든지 이런 식으로 실행할 수 있다.

    private class RecordAudio extends AsyncTask<Void, double[], Void> {

        //스레드 관련 부분 2
       // scaleThread scThread = new scaleThread();

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // AudioRecord를 설정하고 사용한다.
                int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
                // short로 이뤄진 배열인 buffer는 원시 PCM 샘플을 AudioRecord 객체에서 받는다.
                // double로 이뤄진 배열인 toTransform은 같은 데이터를 담지만 double 타입인데, FFT 클래스에서는 double타입이 필요해서이다.
                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];
               // double[] mag = new double[blockSize/2];

                NoiseSuppressor noiseSuppressor = null;
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                {
                    noiseSuppressor = NoiseSuppressor.create(audioRecord.getAudioSessionId());
                    Log.d(TAG, "NoiseSuppressor.isAvailable() " + NoiseSuppressor.isAvailable());
                }

                audioRecord.startRecording();

                while (started) {
                    int bufferReadResult = audioRecord.read(buffer, 0, blockSize);

                    //FFT는 Double 형 데이터를 사용하므로 short로 읽은 데이터를 형변환 시켜줘야함. short / short.MAX_VALUE = double
                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / Short.MAX_VALUE; // 부호 있는 16비트
                    }


                    //두개의 FFT 코드를 사용 잡음잡는것은 RealDoubleFFT가 훨씬 더 잘잡는다.
                    //RealDoubleFFT 부분
                    transformer.ft(toTransform);

                    //-> JTransform 부분
                    //Jtransform 은 입력에 실수부 허수부가 들어가야하므로 허수부 임의로 0으로 채워서 생성해줌
                    /*double y[] = new double[blockSize];
                    for (int i = 0; i < blockSize; i++) {
                        y[i] = 0;
                    }
                    //실수 허수를 넣으므로 연산에는 blockSize의 2배인 배열 필요
                    double[] summary = new double[2 * blockSize];
                    for (int k = 0; k < blockSize; k++) {
                        summary[2 * k] = toTransform[k]; //실수부
                        summary[2 * k + 1] = y[k]; //허수부 0으로 채워넣음.
                    }

                    fft.complexForward(summary);
                    for(int k=0;k<blockSize/2;k++){
                        mag[k] = Math.sqrt(Math.pow(summary[2*k],2)+Math.pow(summary[2*k+1],2));
                    }*/
                    //Jtrans 끝

                    // publishProgress를 호출하면 onProgressUpdate가 호출된다.

                    publishProgress(toTransform);
                    //publishProgress(mag); //1D 로 쓸거면 mag, realdouble이면 toTrans
                }
                noiseSuppressor.release();
                audioRecord.stop();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }
        // onProgressUpdate는 우리 엑티비티의 메인 스레드로 실행된다. 따라서 아무런 문제를 일으키지 않고 사용자 인터페이스와 상호작용할 수 있다.
        // 이번 구현에서는 onProgressUpdate가 FFT 객체를 통해 실행된 다음 데이터를 넘겨준다. 이 메소드는 최대 100픽셀의 높이로 일련의 세로선으로

        @Override
        protected void onProgressUpdate(double[]... toTransform) {
            //차트 삭제 부분
            xlabels.clear();
            ylabels.clear();

            int xChart=0;

            for(int i=0; i<1024; i++){

                xlabels.add(Integer.toString(xChart));
                xChart=xChart+1;
            }

            for(int i=43; i<1024; i++){
                if(toTransform[0][i]>0){
                    ylabels.add(new BarEntry((float)toTransform[0][i],i));
                    //ylabels.add(new BarEntry((float)i,i));
                }
            }



            ArrayList<Integer> hzList = new ArrayList<Integer>();
            ArrayList<Double> hzSize = new ArrayList<Double>();

            //i 가 14부터 시작하는 이유: 배열한칸이 2hz 가지고있는상태이고, 20대에서 에어컨소리때문에 방해가생김 28부터 측정한다는뜻
            for(int i=43; i<toTransform[0].length; i++){
                if(toTransform[0][i]>30){
                    hzList.add(i);   //list에는 대역대가들어감 배열 i 순서
                    hzSize.add(toTransform[0][i]); //list에는 toTransform[][i]의 안에있는 값(크기) 가들어감
                }
            }


            Iterator iter = hzList.iterator();
            if(iter.hasNext()==true){
                t0.setText(Integer.toString(hzList.get(0))); //대역대
                t1.setText(Double.toString(hzSize.get(0)));      //소리 크기
                //t2.setText(whichScale(hzList.get(0)*4));
                //StringBuilder scaleAll = new StringBuilder("");
                //String currentScale = whichScale2(toTransform);

                String currentScale = ScaleC4(toTransform) + ScaleD4(toTransform) + ScaleE4(toTransform) + ScaleF4(toTransform)
                        + ScaleG4(toTransform) + ScaleA4(toTransform) + ScaleB4(toTransform) + ScaleC5(toTransform) + ScaleD5(toTransform)
                        + ScaleE5(toTransform) + ScaleF5(toTransform) + ScaleG5(toTransform) + ScaleA5(toTransform) + ScaleB5(toTransform);

                t2.setText(currentScale);
                scaleBuffer.add(currentScale);


                StringBuilder sb = new StringBuilder();
                for(String scale : scaleBuffer) {
                    sb.append(scale).append(",");
                }

                t3.setText(sb.toString());
                //System.out.println(Arrays.toString(cholee));
            }

            //생각해보니까 Transform 배열 전체를 다 넘겨준다음에. double[]... toTransform
            //거기 함수 안에서 if(도) toTransform[262] 값이랑 뭐뭐 200이상 뛰면으로 바꾸면될거같음

            hzSize.clear();
            hzList.clear();

            //차트 없애는 부분 여기
            BarDataSet barDataSet = new BarDataSet(ylabels,"Hz");
            data = new BarData(xlabels,barDataSet);

            chart.setData(data);
            chart.invalidate();

        }
    }

    public String ScaleC4(double[]... toTransform){
        if(toTransform[0][259]>20 ||toTransform[0][260]> 20 || toTransform[0][261]>20 || toTransform[0][262]>20){
            return "C4";
        }
        return "";
    }
    public String ScaleD4(double[]... toTransform){
        if(toTransform[0][292]>20 || toTransform[0][293]>20 || toTransform[0][294]>20 || toTransform[0][295]>20){
            return "D4";
        }
        return "";
    }
    public String ScaleE4(double[]... toTransform){
        if(toTransform[0][328]>50 ||toTransform[0][329]>50 || toTransform[0][330]>50 || toTransform[0][331]>20){
            return "E4";
        }
        return "";
    }
    public String ScaleF4(double[]... toTransform){
        if(toTransform[0][348]>50 || toTransform[0][349]>50 || toTransform[0][350]>50 || toTransform[0][351]>50){
            return "F4";
        }
        return "";
    }
    public String ScaleG4(double[]... toTransform){
        if(toTransform[0][390]>55 || toTransform[0][391]>60 || toTransform[0][392]>60 || toTransform[0][393]>60){
            return "G4";
        }
        return "";
    }
    public String ScaleA4(double[]... toTransform){
        if(toTransform[0][438]>30 || toTransform[0][439]>30 || toTransform[0][440]>55 ||
                toTransform[0][441]>30 || toTransform[0][442]>30){
            return "A4";
        }
        return "";
    }
    public String ScaleB4(double[]... toTransform){
        if(toTransform[0][492]>80 ||toTransform[0][493]>80 || toTransform[0][494]>80 || toTransform[0][495]>80){
            return "B4";
        }
        return "";
    }
    public String ScaleC5(double[]... toTransform){
        if(toTransform[0][522]>20 ||toTransform[0][523]> 20 || toTransform[0][524]>20 || toTransform[0][525]>20){
            return "C5";
        }
        return "";
    }
    public String ScaleD5(double[]... toTransform){
        if(toTransform[0][586]>20 || toTransform[0][587]>20 || toTransform[0][588]>20 || toTransform[0][589]>20){
            return "D5";
        }
        return "";
    }
    public String ScaleE5(double[]... toTransform){
        if(toTransform[0][658]>50 ||toTransform[0][659]>50 || toTransform[0][660]>50 || toTransform[0][661]>20){
            return "E5";
        }
        return "";
    }
    public String ScaleF5(double[]... toTransform){
        if(toTransform[0][697]>50 || toTransform[0][698]>50 || toTransform[0][699]>50 || toTransform[0][700]>50){
            return "F5";
        }
        return "";
    }
    public String ScaleG5(double[]... toTransform){
        if(toTransform[0][782]>55 || toTransform[0][783]>60 || toTransform[0][784]>60 || toTransform[0][785]>60){
            return "G5";
        }
        return "";
    }
    public String ScaleA5(double[]... toTransform){
        if(toTransform[0][878]>10 || toTransform[0][879]>10 || toTransform[0][880]>15 ||
                toTransform[0][881]>10 || toTransform[0][882]>10){
            return "A5";
        }
        return "";
    }
    public String ScaleB5(double[]... toTransform){
        if(toTransform[0][986]>80 || toTransform[0][987]>80 || toTransform[0][988]>80 || toTransform[0][989]>80){
            return "B5";
        }
        return "";
    }

/*
    public String whichScale2(double[]... toTransform){

        f253.add(toTransform[0][253]);
        f254.add(toTransform[0][254]);
        f255.add(toTransform[0][255]);

        System.out.println("292 : " + toTransform[0][292]);
        System.out.println("293 : " + toTransform[0][293]);
        System.out.println("294 : " + toTransform[0][294]);
        System.out.println("295 : " + toTransform[0][295]);


        if(toTransform[0][111]>99999){
            return " ";
        }
        else if(toTransform[0][259]>20 ||toTransform[0][260]> 20 || toTransform[0][261]>20 || toTransform[0][262]>20 ){
            return "C4"; //도
        }
        else if(toTransform[0][292]>20 || toTransform[0][293]>20 || toTransform[0][294]>20 || toTransform[0][295]>20){
            System.out.println("SBSB");
            return "D4"; //레
        }
        else if(toTransform[0][328]>50 ||toTransform[0][329]>50 || toTransform[0][330]>50 || toTransform[0][231]>20 ){
            return "E4"; //미
        }
        else if(toTransform[0][348]>50 || toTransform[0][349]>50 || toTransform[0][350]>50 || toTransform[0][351]>50){
            return "F4"; //파
        }
        else if(toTransform[0][390]>55 || toTransform[0][391]>60 || toTransform[0][392]>60 || toTransform[0][393]>60){
            return "G4"; //솔
        }
        else if(toTransform[0][438]>30 || toTransform[0][439]>30 || toTransform[0][440]>55 ||
                toTransform[0][441]>30 || toTransform[0][442]>30){
            return "A4"; //라
        }
        else if(toTransform[0][492]>80 ||toTransform[0][493]>80 || toTransform[0][494]>80 || toTransform[0][495]>80  ){
            return "B4"; //솔
        }
        //5옥타브
        else if(toTransform[0][523]>20 || toTransform[0][524]> 20 || toTransform[0][525]> 20  ){
            System.out.println("FUCK");
            return "C5";
        }
        else if(toTransform[0][587]>44 ||toTransform[0][588]>44 || toTransform[0][589]>44  ){
            return "D5";
        }
        else if(toTransform[0][660]>15 ||toTransform[0][659]>20 || toTransform[0][662]>20 ||
                toTransform[0][663]>20 ||toTransform[0][658]>15 || toTransform[0][657]>28 ){
            return "E5";
        }
        else if(toTransform[0][697]>60 ||toTransform[0][698]>60 ||  toTransform[0][699]>60 || toTransform[0][700]>60  ){
            return "F5";
        }
        else if(toTransform[0][783]>55 ||toTransform[0][784]>55 ){
            return "G5";
        }
        else if(toTransform[0][880]>60 ||toTransform[0][881]>60 || toTransform[0][882]>60 ){
            return "A5";
        }
        else if(toTransform[0][987]>33 ||toTransform[0][988]>33 || toTransform[0][989]>33 ){
            return "B5";
        }
        return " ";
    }
*/
    @Override
    public void onClick(View arg0) {
        if (started) {
            try {
                jsonObject.put("email", scaleBuffer);

                //jsonObject.put("password", userPassword);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            Log.d("JSON", "JSON : " + jsonObject);

            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try  {

                        String host_url = "https://webhook.site/a90b0918-38c0-4c59-ae55-f4af0e74063a";
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

                        int responseCode = con.getResponseCode();
                        Log.d("MSG", "===================================" );
                        Log.d("MSG", "MSG : " + responseCode);
                        Log.d("MSG", "===================================" );

                        aaa= responseCode;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
//            thread.stop();

            try {
                thread.join();


                //jsonObject.put("password", userPassword);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }

            Log.d("aaa:", aaa + "===================================" );

            if(aaa ==400){
                Log.d("error : 400", "===================================" );
            }else if(aaa == 500){
                Log.d("error : 500", "===================================" );
            }
            else{
                Intent intent1 = new Intent(scaleDetector.this, MainActivity.class);
                startActivity(intent1);
            }

            started = false;
            startStopButton.setText("Start");
            recordTask.cancel(true);
        } else {
            started = true;
            startStopButton.setText("Stop");
            recordTask = new RecordAudio();
            recordTask.execute();
        }
    }


}//activity

