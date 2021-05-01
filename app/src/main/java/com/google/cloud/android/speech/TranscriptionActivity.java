package com.google.cloud.android.speech;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class TranscriptionActivity extends AppCompatActivity {

    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BufferElements2Rec = 800000;
    // private static final int BytesPerElement = 1;

    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private OkHttpClient client = null;
    private WebSocket ws = null;
    private int bufferSize;
    String token = "";

    public static final String filename = "login";
    public static final String spusername = "username";
    public static final  String sppassword = "password";


    SharedPreferences sp;

    private CoordinatorLayout coordinatorLayout;

    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    private static final String STATE_RESULTS = "results";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private CardView transcriptionView;
    FloatingActionButton startButton;
    FloatingActionButton stopButton;
    FloatingActionButton firstButton;

    private final StringBuilder transcription = new StringBuilder();
    private TextView textView = null;
    private TextView statusText = null;

    //private static final String WEBSOCKET_URL = "ws://10.0.2.2:8000/ws/transcriptData/";
     private static final String WEBSOCKET_URL = "ws://ec2-3-16-29-185.us-east-2.compute.amazonaws.com:8000/ws/transcriptData/";

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d("Vincy says...", "Connection established");

            //            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            System.out.println("Receiving : " + text);
            if (!text.contains("}")) {
                transcription.append(text);
                textView.setText(transcription.toString());
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            System.out.println("Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            System.out.println("final transcription: " + transcription.toString());
            System.out.println("Closing : " + code + " / " + reason);
            transcription.setLength(0);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            System.out.println("Error : " + t.getMessage());
            t.printStackTrace();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcription);
        setButtonHandlers();
        enableButtons(false);

       /* sp = getSharedPreferences(filename, Context.MODE_PRIVATE);
        if(sp.contains(spusername) && sp.contains(sppassword))
        {

        }  */



        SessionManager sessionManager = new SessionManager(TranscriptionActivity.this);
        HashMap<String, String> userDetails = sessionManager.getUserDetailsFromSession();

        if (userDetails.get(SessionManager.IS_LOGGEDIN).equals("true") && userDetails.get(SessionManager.IS_LOGGEDIN) != null)
        {
            token = userDetails.get(SessionManager.KEY_TOKEN);
        }



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            setButtonHandlers();
            enableButtons(false);

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }

        //setButtonHandlers();
        //enableButtons(false);

        textView = findViewById(R.id.tvTranscription);
        statusText = findViewById(R.id.status);
        statusText.setAlpha(0.0f);
        textView.setMovementMethod(new ScrollingMovementMethod());

        transcriptionView = findViewById(R.id.transcriptionView);

        client = new OkHttpClient();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        Intent intent = getIntent();
        token = getIntent().getStringExtra("token");

        coordinatorLayout = findViewById(R.id.layout);


        //firstButton = findViewById(R.id.btnStart);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_language:
                return true;
            case R.id.action_lesson:
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_profile:
                Intent intent = new Intent(TranscriptionActivity.this, UserProfile.class);
                intent.putExtra("token", token );
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    private void setButtonHandlers() {
        startButton = findViewById(R.id.fabRecord);
        stopButton = findViewById(R.id.fabStopRecord);

        startButton.setOnClickListener(btnClick);
        stopButton.setOnClickListener(btnClick);

    }

    private void enableButton(int id, boolean isEnable) {
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.fabRecord, !isRecording);
        enableButton(R.id.fabStopRecord, isRecording);
    }

    private void startRecording() {
        int size = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        bufferSize = Math.max(size, BufferElements2Rec);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, bufferSize);

        client = new OkHttpClient();
        Request request = new Request.Builder().url(WEBSOCKET_URL).build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        ws = client.newWebSocket(request, listener);

        recorder.startRecording();
        isRecording = true;
        Log.d("Vincy says... Buffer length", String.valueOf(bufferSize));
        Log.d("Vincy says... ", "Running thread");
        recordingThread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            public void run() {
                try {
                    writeAudioDataToSocket(bufferSize);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void writeAudioDataToSocket(int bufferSize) {
        Log.d("Vincy says...   ", "Trying to Connect");

        int bRead;
        while (isRecording) {
            try {
                byte[] bData = new byte[bufferSize];
                bRead = recorder.read(bData, 0, bufferSize);
                if (bRead == AudioRecord.ERROR_BAD_VALUE || bRead == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e("Vincy says...", "Error reading from microphone.");
                    // isRecording = false;
                    stopRecording();
                    break;
                }
                Log.d("StartRecLoop", "Short writing to bytebuffer" + Arrays.toString(bData));

                // byte[] bData = short2byte(sData);

                Log.d("starting", "pick up from mic");
                Log.d("Vincy says...", Arrays.toString(bData));

                ws.send(ByteString.of(bData, 0, bufferSize));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() {
        // stops the recording activity
        if (recorder != null) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;

            ws.send("EOS");
            ws.close(1000, "EOS");
            client.dispatcher().executorService().shutdown();
        }
    }

    private final View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.fabRecord) {
                statusText.setAlpha(1.0f);
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Recording Started", Snackbar.LENGTH_LONG);
                snackbar.show();
                textView.setText("");
                enableButtons(true);
                startRecording();
            } else if (id == R.id.fabStopRecord) {
                statusText.setAlpha(0.0f);
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Recording Stopped", Snackbar.LENGTH_LONG);
                snackbar.show();
                enableButtons(false);
                stopRecording();
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}