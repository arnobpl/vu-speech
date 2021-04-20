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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * Continuously records audio and notifies the {@link VoiceRecorder.Callback} when voice (or any
 * sound) is heard.
 *
 * The recorded audio format is always {@link AudioFormat#ENCODING_PCM_16BIT} and
 * {@link AudioFormat#CHANNEL_IN_MONO}. This class will automatically pick the right sample rate
 * for the device. Use {@link #getSampleRate()} to get the selected value.
 */
public class VoiceRecorder {

    private int bitNumber = 16;
    private int nChannels = 4;
    private int aSource = 0;

//    private static final int[] SAMPLE_RATE_CANDIDATES = new int[]{16000, 11025, 22050, 44100};

    private static final String TAG = "VoiceRecorder";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioTrack speaker;

    private static final String clientAddress = "192.168.1.25";
    private static final String userName = "respeaker";
    private static final String passWord = "respeaker";

    private static final int AMPLITUDE_THRESHOLD = 500;
    private static final int SPEECH_TIMEOUT_MILLIS = 2 * 1000;
    private static final int MAX_SPEECH_LENGTH_MILLIS = 30 * 1000;

    public static long mLastVoiceHeardMillis = Long.MAX_VALUE;
    public static long mVoiceStartedMillis = Long.MAX_VALUE;

    public static abstract class Callback {

        /**
         * Called when the recorder starts hearing voice.
         */
        public void onVoiceStart() {
        }

        /**
         * Called when the recorder is hearing voice.
         *
         * @param data The audio data in {@link AudioFormat#ENCODING_PCM_16BIT}.
         * @param size The size of the actual data in {@code data}.
         */
        public void onVoice(byte[] data, int size) {
        }

        /**
         * Called when the recorder stops hearing voice.
         */
        public void onVoiceEnd() {
        }
    }

    private final Callback mCallback;

    private ServerSocket serverSocketPF; // speech source audio
    private Socket socketPF;
    private final int portPF = 10000; // listen for separated speech source

    private Thread mThread;

    private int sizeInBytes = 0;
    private byte[][] mBuffer  = {null, null, null, null};

    private byte[] disBuffer;
    private int disSize;
    private int disBytes;

    /** may need to adjust buffer size for sample rate and latency */
    /** 4 channels 16 bit interleaved audio at 16000 Hz */

    private final Object mLock = new Object();

    /** Total voice time from start to timeout/end **/

    public VoiceRecorder(@NonNull Callback callback) {
        mCallback = callback;
    }

    /**
     * Starts recording audio.
     *
     * The caller is responsible for calling {@link #stop()} later.
     */
    public void start() {
        //Log.i(TAG,"Starting..." + System.currentTimeMillis());
        // Stop recording if it is currently ongoing.
        stop();

        sizeInBytes = createAudioRecord();
        if (sizeInBytes <= 0) {
            throw new RuntimeException("Cannot instantiate VoiceRecorder - buffer size=" + sizeInBytes);
        }
        for (int i=0; i<nChannels; i++){
            mBuffer[i] = new byte[sizeInBytes];
        }
        disSize = sizeInBytes*nChannels;
        disBuffer = new byte[disSize];
        Log.i(TAG,"mBuffer: " + sizeInBytes + "disBuffer: " + disSize);
        mThread = new Thread(new ProcessVoice());
        mThread.start();
    }

    /**
     * Stops recording audio.
     */
    public void stop() {
        synchronized (mLock) {
            for (int i=0; i<nChannels; i++) {
                mBuffer[i] = null;
            }
            dismiss();
            try {
                if (mThread != null) {
                    mThread.interrupt();
                    mThread = null;
                    Log.i(TAG,"Close Socket");
                }
                disBuffer = null;
                if (speaker != null) speaker.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Dismisses the currently ongoing utterance.
     */
    public void dismiss() {
        if (mLastVoiceHeardMillis != Long.MAX_VALUE) {
            mLastVoiceHeardMillis = Long.MAX_VALUE;
            //SpeechSource.activeSpeaker = 0;
            for (int n=0; n<nChannels; n++) {
                SpeechSource.active[n] = false;
            }
            Log.i(TAG, "Voice dismiss..." + System.currentTimeMillis());
            mCallback.onVoiceEnd();
        }
    }

    /**
     * Retrieves the sample rate currently used to record audio.
     * @return The sample rate of recorded audio.
     */
    public int getSampleRate() {
        return SAMPLE_RATE; // may be programmable for different audio environments
    }

    /**
     * @return a minimum buffer size for sample rate
     */
    private int createAudioRecord() {

        return sizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING);
    }

    /**
     * Continuously processes the captured audio and notifies {@link #mCallback} of corresponding
     * events.
     */
    private class ProcessVoice implements Runnable {

        private DataInputStream DIS;
        private boolean playAudio = false;
        private int[] aSum = {0, 0, 0, 0, 0};
        private int offset = 0;
        private int i = 0;
        private int mSize = 0;
        private int jump = bitNumber / 8;

        @Override
        public void run() {
            try {
                Log.i(TAG, "Connecting..." + portPF);
                serverSocketPF = new ServerSocket(portPF);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Log.i(TAG, "Waiting..." + portPF);
                socketPF = serverSocketPF.accept();
                Log.i(TAG, "Connected..." + socketPF.getInetAddress() + ":" + portPF);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                DIS = new DataInputStream(socketPF.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Start server input processing
            Log.i(TAG, "Listening...");
            if (playAudio) {
                Log.i(TAG, "Playing...");
                speaker = new AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        sizeInBytes,
                        AudioTrack.MODE_STREAM);
                speaker.play();
            }

            // Start recording.
            int q = 0;
            while (true) {
                synchronized (mLock) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    // read socket and extract data for each audio channel
                    try {
                        disBytes = DIS.available();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final long now = System.currentTimeMillis();
                    if (disBytes > 0) {
                        offset = 0;
                        try {
                            disBytes = DIS.read(disBuffer, offset, disSize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // deinterleave source audio into separate buffers
                        // each 8 bytes (16bits * 4 channels) is one sample
                        i = 0;
                        mSize = 0;
                        SpeechSource.activeSpeaker = 0;
                        try {
                            while (offset < disBytes - 1) {
                                for (int n = 0; n < nChannels; n++) {
                                    mBuffer[n][mSize] = disBuffer[offset];
                                    mBuffer[n][mSize + 1] = disBuffer[offset + 1];
                                    offset += jump;
                                }
                                mSize += jump;
                            }
                        } catch (Exception e) {
                            Log.i(TAG, "disBytes " + disBytes + " offset " + offset + "msize " + mSize);
                            e.printStackTrace();
                        }
                        //Log.i(TAG, "All Data " + mSize + " / " + disBytes);
                        // single channel approach processes only channel 0
                        if (isHearingVoice(mBuffer[0], mSize)) {
                            if (mLastVoiceHeardMillis == Long.MAX_VALUE) {
                                Log.i(TAG, "Voice start..." + now);
                                mVoiceStartedMillis = now;
                                mCallback.onVoiceStart();
                            }
                            mCallback.onVoice(mBuffer[0], mSize);
                            //Log.i(TAG, "Sent..." + mSize);
                            mLastVoiceHeardMillis = now;
                            if (now - mVoiceStartedMillis > MAX_SPEECH_LENGTH_MILLIS) {
                                Log.i(TAG, "Voice max time..." + now);
                                end();
                            }
                        } else if (mLastVoiceHeardMillis != Long.MAX_VALUE) {
                            mCallback.onVoice(mBuffer[0], mSize);
                            if (now - mLastVoiceHeardMillis > SPEECH_TIMEOUT_MILLIS) {
                                Log.i(TAG, "Voice no audio..." + now);
                                end();
                            }
                        }
                        // TODO handle simultaneous speaker input
                        /* begin multi-channel input
                        for (int n = 0; n < nChannels; n++) {
                            if (aSum[n] != 0) {
                                SpeechSource.active[n] = true;
                                SpeechSource.activeSpeaker = n;
                            }
                            // flag start of speaker audio stream
                            if (SpeechSource.active[n] &&
                                    mLastVoiceHeardMillis == Long.MAX_VALUE) {
                                Log.i(TAG, "Voice start..." + n + " " + now);
                                mVoiceStarted = true;
                                mVoiceStartedMillis = now;
                                mCallback.onVoiceStart();
                            }
                            // send active speaker audio to speech translation
                            if (mVoiceStarted && SpeechSource.active[n] && SpeechSource.activity[n] > 0.25) {
                                //Log.i(TAG, "Voice chan " + n + " : " + mSize + " : "  + now);
                                mCallback.onVoice(mBuffer[n], mSize);
                                //Log.i(TAG, "Voice chan " + n + " active " + SpeechSource.activity[n]);
                                if (playAudio && n == 0) speaker.write(mBuffer[n], 0, mSize);
                                mLastVoiceHeardMillis = now;
                                // check for maximum speech interval
                                if (now - mVoiceStartedMillis > MAX_SPEECH_LENGTH_MILLIS) {
                                    Log.i(TAG, "Voice max time..." + n + ": " + now);
                                    end();
                                }
                            }
                            // check for speech timeout interval (no audio)
                            if (mVoiceStarted && now - mLastVoiceHeardMillis > SPEECH_TIMEOUT_MILLIS) {
                                Log.i(TAG, "Voice timeout..." + n + ": " + now);
                                end();
                            }
                        }
                        end multi-channel input*/
                    }
                }
            }
        }

        private void end() {
            mLastVoiceHeardMillis = Long.MAX_VALUE;
            for (int n = 0; n < nChannels; n++) {
                SpeechSource.active[n] = false;
            }
            mCallback.onVoiceEnd();
        }

        private boolean isHearingVoice(byte[] buffer, int size) {
            for (int i = 0; i < size - 1; i += 2) {
                // The buffer has LINEAR16 in little endian.
                int s = buffer[i + 1];
                if (s < 0) s *= -1;
                s <<= 8;
                s += Math.abs(buffer[i]);
                if (s > AMPLITUDE_THRESHOLD) {
                    return true;
                }
            }
            return false;
        }
    }
}
