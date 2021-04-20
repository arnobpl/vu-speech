/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.android.speech;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.google.cloud.android.speech.R.*;

public class MainActivity extends AppCompatActivity implements MessageDialogFragment.Listener {

    private FileWriter tfWriter;

    String auth = "";
    String token = "";

    private File transcriptFile;

    private Boolean fileCreated = Boolean.FALSE;

    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    private static final String STATE_RESULTS = "results";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private String TAG = "MainActivity ";

    private SpeechService mSpeechService;
    private SpeechSource mSpeechSource;
    private VoiceRecorder mVoiceRecorder;

    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            showStatus(true);
            if (mSpeechService == null) {
                mSpeechService = new SpeechService();
            }
            mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            //Log.i(TAG,"voice started..." + System.currentTimeMillis());
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            showStatus(false);
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }

    };

    // Resource caches
    private int mColorHearing;
    private int mColorNotHearing;

    // View references
    private TextView mStatus;
    private TextView mText;
    private ResultAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
            mStatus.setVisibility(View.VISIBLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Resources resources = getResources();
        final Resources.Theme theme = getTheme();
        mColorHearing = ResourcesCompat.getColor(resources, R.color.status_hearing, theme);
        mColorNotHearing = ResourcesCompat.getColor(resources, R.color.status_not_hearing, theme);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mStatus = (TextView) findViewById(R.id.status);
        mText = (TextView) findViewById(R.id.text);

        Intent intent = getIntent();
        token = getIntent().getStringExtra("token");

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ArrayList<String> results = savedInstanceState == null ? null :
                savedInstanceState.getStringArrayList(STATE_RESULTS);
        mAdapter = new ResultAdapter(results);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecorder();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    protected void onStop() {
        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        // close the transcript file
        if (fileCreated) {
            try {
                tfWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    tfWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null) {
            outState.putStringArrayList(STATE_RESULTS, mAdapter.getResults());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case id.action_language:
                return true;
            case id.action_lesson:
                return true;
            case id.action_settings:
                return true;
            case id.action_profile:
                Intent intent = new Intent(MainActivity.this, UserProfile.class);
                intent.putExtra("token", token );
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
        mSpeechSource = new SpeechSource();
        mSpeechSource.start();

        // open transcript file for write
        if (storageReady()) {
            try {
                fileCreated = createTranscriptFile("temp.txt");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    private void showStatus(final boolean hearingVoice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatus.setTextColor(hearingVoice ? mColorHearing : mColorNotHearing);
            }
        });
    }

    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private int getSpeakerID(double doa){
        double direction, aoa = 0.0;
        // find direction of arrival within +- 10 degrees of active speaker direction
        // if none, create new speaker direction entry
        // TODO add new speakers dynamically; add reset for new session
        for (int n=0; n<SpeechSource.speakerDOA.length; n++) {
            aoa = SpeechSource.speakerDOA[n];
            direction = doa;
            if (aoa==0.0){
                SpeechSource.speakerDOA[n] = doa;
                return n + 1;
            } else if (doa>=350.0 && aoa<=10.0){
                aoa += 360.0;
            } else if (doa<=10.0 && aoa>= 350.0){
                direction += 360.0;
            }
            if (aoa <= direction+10.0 && aoa >= direction-10.0) {
                SpeechSource.speakerDOA[n] = doa;
                return n + 1;
            }
        }
        return 0;
    }

    private String getDirection(double doa) {
        // Segment every 45 degrees
        if (doa >= 337.5 && doa < 22.5) {
            return "[↑] ";
        } else if (doa >= 22.5 && doa < 67.5){
            return "[↗] ";
        } else if (doa >= 67.5 && doa < 112.5) {
            return "[→] ";
        } else if (doa >= 112.5 && doa < 157.5) {
            return "[↘] ";
        } else if (doa >= 157.5 && doa < 202.5) {
            return "[↓] ";
        } else if (doa >= 202.5 && doa < 247.5) {
            return "[↙] ";
        } else if (doa >= 247.5 && doa < 292.5) {
            return "[←] ";
        } // doa >= 292.5 && doa < 337.5
        return "[↖] ";
    }
    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }
                    if (mText != null && !TextUtils.isEmpty(text)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    mText.setText(null);
                                    String bearingX, bearingY, srcText = "";
                                    int n = SpeechSource.activeSpeaker;
                                    double doa = SpeechSource.bearing[n];
                                    try {
                                        srcText = "Spkr" + getSpeakerID(SpeechSource.bearing[n])
                                                + getDirection(SpeechSource.bearing[n]) + text;
                                    } catch (Exception e) {
                                        srcText =  "Spkr0" + "[↑] " + text;
                                    }
                                    try {
                                        bearingX = new DecimalFormat("0.000").format(SpeechSource.x[n]);
                                        bearingY = new DecimalFormat("0.000").format(SpeechSource.y[n]);
                                    } catch (Exception e) {
                                        bearingX = "0.000";
                                        bearingY = "0.000";
                                    }
                                    // reset active speaker
                                    SpeechSource.activeSpeaker = 0;
                                    mAdapter.addResult(srcText);
                                    Log.i(TAG,srcText + " <" + bearingY + ":" + bearingX + "> " + System.currentTimeMillis());
                                    mRecyclerView.smoothScrollToPosition(0);
                                    if (fileCreated) {
                                        try {
                                            tfWriter.write( srcText + "\n");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                } else {
                                    mText.setText(text);
                                }
                            }
                        });
                    }
                }
            };

    private static class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_result, parent, false));
            text = (TextView) itemView.findViewById(R.id.text);
        }

    }

    private static class ResultAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final ArrayList<String> mResults = new ArrayList<>();

        ResultAdapter(ArrayList<String> results) {
            if (results != null) {
                mResults.addAll(results);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText(mResults.get(position));
        }

        @Override
        public int getItemCount() {
            return mResults.size();
        }

        void addResult(String result) {
            mResults.add(0, result);
            notifyItemInserted(0);
        }

        public ArrayList<String> getResults() {
            return mResults;
        }

    }

    public Boolean createTranscriptFile(String fileName)
            throws IOException {
        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS);
            path.mkdirs();
            File file = new File(path, fileName);
            Log.i(TAG,"Created file:" + path + file.separator + fileName);
            try {
                //tfWriter = new FileWriter(file);
                //return Boolean.TRUE;
                Log.i(TAG,"Created filewriter");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }

    public static boolean storageReady() {
        String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            return false;
        } else {
            return true;
        }
    }
}
