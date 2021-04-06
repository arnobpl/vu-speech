package com.example.vuspeech;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;




//package com.example.mictest;

        import android.app.Activity;
        import android.media.AudioFormat;
        import android.media.AudioRecord;
        import android.media.MediaRecorder;
        import android.os.Build;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.KeyEvent;
        import android.view.View;
        import android.widget.Button;

        import androidx.annotation.RequiresApi;

        import org.jetbrains.annotations.NotNull;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.FileNotFoundException;
        import java.util.Arrays;

        import okhttp3.OkHttpClient;
        import okhttp3.Request;
        import okhttp3.Response;
        import okhttp3.WebSocket;
        import okhttp3.WebSocketListener;
        import okio.ByteString;


public class Transcription extends AppCompatActivity {
    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    //private static final String WEBSOCKET_URL = "ws://10.0.2.2:8000/ws/transcriptData/";
    private static final String WEBSOCKET_URL = "ws://stark.cse.buffalo.edu:8000/ws/transcriptData/";

    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private OkHttpClient client;

    private static final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            Log.d("Vincy says...", "Connection established");

//            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            System.out.println("Receiving : " + text);
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, ByteString bytes) {
            System.out.println("Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, @NotNull String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            System.out.println("Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, Throwable t, Response response) {
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
        client = new OkHttpClient();
        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);


    }

    private void setButtonHandlers() {
        Button startbutton = (Button) findViewById(R.id.btnStart);
        Button stopbutton = (Button) findViewById(R.id.btnStop);
        startbutton.setOnClickListener(btnClick);
        stopbutton.setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnStop, isRecording);
    }

    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    private void startRecording() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;

        Log.d("Vincy says...   ", "Trying to Connect");
        Request request = new Request.Builder().url(WEBSOCKET_URL).build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = client.newWebSocket(request, listener);
        Log.d("Vincy says...   ", "New Websocket created");

        Log.d("Vincy says... ", "Trying to send data");
        recordingThread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            public void run() {
                try {
                    writeAudioDataToSocket(ws);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArraySize = sData.length;
        byte[] bytes = new byte[shortArraySize * 2];
        for (int i = 0; i < shortArraySize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void writeAudioDataToSocket(WebSocket ws) throws FileNotFoundException {
        short[] sData = new short[BufferElements2Rec];

        while (isRecording) {
            // gets the voice output from microphone to byte format

            recorder.read(sData, 0, BufferElements2Rec);
            Log.d("StartRecLoop", "Short writing to bytebuffer " + Arrays.toString(sData));
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                byte[] bData = short2byte(sData);
                JSONObject json = new JSONObject();
                json.put("type", "raw");
//                json.put("stream", Arrays.toString(bData));
                json.put("stream", Arrays.toString(bData));
                System.out.println(json);
                Log.d("starting", "pick up from mic");
//                Log.d("myTag", bData.toString());

                ws.send(json.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() {
        // stops the recording activity
        client.dispatcher().executorService().shutdown();
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    private final View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.btnStart) {
                enableButtons(true);
                startRecording();
            } else if (id == R.id.btnStop) {
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
