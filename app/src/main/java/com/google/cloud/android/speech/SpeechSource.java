/**
 * Created by gary on 12/07/18.
 */

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
import android.util.Log;

import android.support.annotation.NonNull;

import com.google.common.base.Utf8;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static java.lang.Math.atan2;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
     * Updates the speaker source parameters when voice is heard.
     **/
    public class SpeechSource {


        private static final String TAG = "SpeechSource";
        private ServerSocket serverSocketST; // speech source track
        private Socket socketST;
        private final int portST = 9000; // monitor active speech source input
        private static int disSize = 2048;
        private byte[] disBuffer = new byte[disSize];
        private static int disBytes;
        private final int nChannels = 4;
        private final double pi = 3.141592653;

        private Thread mThread;
        private final Object mLock = new Object();

        /** Speech source parameters**/
        public static boolean[] active = {false, false, false, false}; // active speaker detected
        public static int[] speakerID = {0, 0, 0, 0}; // speaker identifier
        public static int activeSpeaker = 0; // active speaker identifier
        public static String[] tag = {"", "", "", ""};
        //public static long[] timeStamp = {0, 0, 0, 0}; // ignore; use system time

        /** speaker location **/
        public static double[] x = {0.0, 0.0, 0.0, 0.0};
        public static double[] y = {0.0, 0.0, 0.0, 0.0};
        public static double[] z = {0.0, 0.0, 0.0, 0.0};
        public static double[] activity = {0.0, 0.0, 0.0, 0.0};
        public static double[] bearing = {0.0, 0.0, 0.0, 0.0};
        public static double[] speakerDOA = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        /**
         * Listens to active speech source messages
         */
        public void start() {
            Log.i(TAG,"Starting...");

            mThread = new Thread(new com.google.cloud.android.speech.SpeechSource.VoiceTracking());
            mThread.start();
        }

        /**
         * Stops recording audio.
         */
        public void stop() {
            synchronized (mLock) {
                try {
                    if (mThread != null) {
                        mThread.interrupt();
                        mThread = null;
                        Log.i(TAG,"Close Socket");
                    }
                    disBuffer = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Continuously monitors source tracks for active speech.
         */
        private class VoiceTracking implements Runnable {

            private JSONObject jObject;
            private JSONArray jsonArray;
            private String jString;
            private DataInputStream DIS;
            private byte[] buffer;

            @Override
            public void run() {
                buffer = new byte[4096];
                try {
                    Log.i(TAG, "Connecting..." + portST);
                    serverSocketST = new ServerSocket(portST);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Log.i(TAG, "Waiting..." + portST);
                    socketST = serverSocketST.accept();
                    Log.i(TAG, "Connected..." + socketST.getInetAddress() + ":" + portST);
                    // Start processing captured voice audio.
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    DIS = new DataInputStream(socketST.getInputStream());
                    // create receive array and overlay big endian buffer array
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Start server input processing
                Log.i(TAG, "Listening...");
                while (true) {
                    synchronized (mLock) {
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        try {
                            disBytes = DIS.available();
                            //Log.i(TAG, "Reading..." + disBytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (disBytes > 0) {
                            try {
                                jString = inputStreamToString(DIS);
                                if (jString.length()<390) {
                                    Log.i("JOBJ ", disBytes + " len " + jString.length() + ": " + jString);
                                    continue;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                jObject = new JSONObject(jString);
                            } catch (JSONException e) {
                                Log.e("JOBJ ", disBytes + " len " + jString.length() + ": " + jString);
                                e.printStackTrace();
                                continue;
                            }
                            try {
                                jsonArray = jObject.getJSONArray("src");
                            } catch (JSONException e) {
                                Log.e("JARY Bytes ", disBytes + " len " + jObject.length() + ": " + jObject.toString());
                                e.printStackTrace();
                                continue;
                            }
                            // parse json source track message
                            for(int i=0; i<jsonArray.length(); i++)
                            {
                                // get audio channels with active source data
                                try {
                                    JSONObject curr = jsonArray.getJSONObject(i);
                                    //* ignore timestamp; use system time for voice activity
                                    //speakerID[i] = curr.getInt("id");
                                    //tag[i] = curr.getString("tag");
                                    x[i] = curr.getDouble("x");
                                    y[i] = curr.getDouble("y");
                                    z[i] = curr.getDouble("z");
                                    activity[i] = curr.getDouble("activity");
                                    bearing[i] = 90.0 - (180.0/pi)*atan2(y[i], x[i]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

            private String inputStreamToString(InputStream is) throws IOException {
                int bytesRead = is.read(buffer);
                String s = new String(buffer, 0, bytesRead, "UTF-8");
                // find start and end of Track message
                if (bytesRead<451) return s;
                int begin = s.indexOf("timeStamp") - 7;
                int end = 4 + s.indexOf("]", begin);
                return s.substring(begin, end);
            }
        }
    }
