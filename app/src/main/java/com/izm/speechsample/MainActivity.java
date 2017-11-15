package com.izm.speechsample;

import android.hardware.SensorManager;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.content.Context;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TimerCallBack, SensorEventListener{

    private boolean startFlag;
    private Button button;
    private TextView textView;
    private SpeechRecognizer sr;
    private CountUpTimer timer = new CountUpTimer();
    private SensorManager sensorManager;
    private Sensor sensor;

    @Override
    public void callBackTime(long mm, long ss, long ms){
        textView.setText(String.format("%1$02d:%2$02d.%3$01d", mm, ss, ms));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startFlag = false;

        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ChangeStetus();
            }
        });

        textView = (TextView)findViewById(R.id.textview);
        textView.setText("00:00:0");

        timer.setCallBack(this);

        // センサーオブジェクトを取得
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // 近接センサーのオブジェクトを取得
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startSpeechRecognizer();
        // 近接センサーを有効
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        stopSpeechRecognizer();
        // 近接センサーを無効
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    private void ChangeStetus()
    {
        if(!startFlag){
            button.setText("Stop");
            textView.setText("00:00:0");
            timer.start(100);
        }
        else{
            button.setText("Start");
            timer.stop();
        }

        startFlag = !startFlag;
    }

    protected void startSpeechRecognizer()
    {
       try{
            if(sr == null){
                sr = SpeechRecognizer.createSpeechRecognizer(this);
                if (!SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "音声認識が使えません",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                sr.setRecognitionListener(new listener());
            }
           // インテントの作成
           Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
           // 言語モデル指定
           intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                   RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
           sr.startListening(intent);
       } catch (Exception ex) {
           Toast.makeText(getApplicationContext(), "ERROR! StartSpeechRecognizer",
                   Toast.LENGTH_LONG).show();
           finish();
       }

    }

    protected void stopSpeechRecognizer()
    {
        if(sr != null) sr.destroy();
        sr = null;
    }

    public void restartListeningService()
    {
        stopSpeechRecognizer();
        startSpeechRecognizer();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            // values = 5 or 0
            if (event.values[0] > 0 ) {
            } else {
                startSpeechRecognizer();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class listener implements RecognitionListener {
        public void onBeginningOfSpeech() {
            /*Toast.makeText(getApplicationContext(), "onBeginningofSpeech",
                    Toast.LENGTH_SHORT).show();*/
        }

        public void onBufferReceived(byte[] buffer) {
        }

        public void onEndOfSpeech() {
            /*Toast.makeText(getApplicationContext(), "onEndofSpeech",
                    Toast.LENGTH_SHORT).show();*/
        }

        public void onError(int error) {
            String reason = "";
            switch (error) {
                // Audio recording error
                case SpeechRecognizer.ERROR_AUDIO:
                    reason = "ERROR_AUDIO";
                    break;
                // Other client side errors
                case SpeechRecognizer.ERROR_CLIENT:
                    reason = "ERROR_CLIENT";
                    break;
                // Insufficient permissions
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    reason = "ERROR_INSUFFICIENT_PERMISSIONS";
                    break;
                // 	Other network related errors
                case SpeechRecognizer.ERROR_NETWORK:
                    reason = "ERROR_NETWORK";
                    /* ネットワーク接続をチェックする処理をここに入れる */
                    break;
                // Network operation timed out
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    reason = "ERROR_NETWORK_TIMEOUT";
                    break;
                // No recognition result matched
                case SpeechRecognizer.ERROR_NO_MATCH:
                    reason = "ERROR_NO_MATCH";
                    break;
                // RecognitionService busy
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    reason = "ERROR_RECOGNIZER_BUSY";
                    break;
                // Server sends error status
                case SpeechRecognizer.ERROR_SERVER:
                    reason = "ERROR_SERVER";
                    /* ネットワーク接続をチェックをする処理をここに入れる */
                    break;
                // No speech input
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    reason = "ERROR_SPEECH_TIMEOUT";
                    break;
            }
            Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
            //restartListeningService();
        }

        public void onEvent(int eventType, Bundle params) {
        }

        public void onPartialResults(Bundle partialResults) {
        }

        public void onReadyForSpeech(Bundle params) {
            //Toast.makeText(getApplicationContext(), "話してください",
            //        Toast.LENGTH_SHORT).show();
        }

        public void onResults(Bundle results) {
            // 結果をArrayListとして取得
            ArrayList results_array = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            // 取得した文字列を結合
            String resultsString = "";
            for (int i = 0; i < results.size(); i++) {
                resultsString += results_array.get(i) + ";";

                if(results_array.get(i).equals("スタート") && !startFlag){
                    ChangeStetus();
                }
                if(results_array.get(i).equals("ストップ") && startFlag){
                    ChangeStetus();
                }
            }
            // トーストを使って結果表示
            Toast.makeText(getApplicationContext(), resultsString, Toast.LENGTH_LONG).show();
            //restartListeningService();
        }

        public void onRmsChanged(float rmsdB) {
        }
    }
}
