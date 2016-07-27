package com.bd.gslab.androidqdb;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;

public class TtsService extends Service implements TextToSpeech.OnInitListener,AudioManager.OnAudioFocusChangeListener,TextToSpeech.OnUtteranceCompletedListener {
    private TextToSpeech mTts;
    private String spokenText;

    // Logging constants
    private static final boolean DEBUG = false;
    private static final String TAG = "TAG";
    private static final String CLASS = "SpeechService - ";

    // KEY_PARAM_UTTERANCE_ID
    private static final String DONE = "done";

    // used to set the intent in the calling app
    // as well as get the intent here
    protected static final String TEXT = "text";
    protected static final String INITIALIZE = "initialize";
    protected static final String BROADCAST_ACTION = "net.bendele.runwalk.tts.notavailable";

    private static TextToSpeech tts;
    private static AudioManager audioManager;

    private String text2speak;
    private boolean initialize = false;

    private void myLog(String msg) {
        if (DEBUG) {
            if (msg != "") {
                msg = " - " + msg;
            }
            String caller = Thread.currentThread().getStackTrace()[3]
                    .getMethodName();
            Log.d(TAG, CLASS + caller + msg);
        }
    }



    @Override
    public void onCreate() {
        myLog("");
    }

    public TtsService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myLog("");
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initialize = intent.getBooleanExtra(INITIALIZE, false);
        text2speak = intent.getStringExtra(TEXT);
        tts = new TextToSpeech(getApplicationContext(), this);
//        return super.onStartCommand(intent, flags, startId);
        return TtsService.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        myLog("-----");
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onInit(int status) {
        myLog("");
        if (status == TextToSpeech.SUCCESS) {
            // the call to set the utterance listener must be in the
            // onInit method (inside setTts()), in the SUCCESS check.
            setTts();

            HashMap<String, String> myHashParams = new HashMap<String, String>();
            myHashParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, DONE);

            if (initialize) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts.playSilentUtterance(1, TextToSpeech.QUEUE_ADD,"init");
                }else{
                    tts.playSilence(1, TextToSpeech.QUEUE_ADD,myHashParams);
                }
            } else {
                audioManager.requestAudioFocus(this,
                        AudioManager.STREAM_NOTIFICATION,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                myLog("text2speak = " + text2speak);
                speakNow(text2speak);
            }
        } else {
            // report back to the caller that TTS is not available
            Intent intent = new Intent(BROADCAST_ACTION);
            sendBroadcast(intent);
        }

    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void setTts() {
        myLog("");
        if (Build.VERSION.SDK_INT >= 15) {
            myLog("Build.VERSION.SDK_INT >= 15");
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    onDoneSpeaking(utteranceId);
                }

                @Override
                public void onError(String utteranceId) {
                }

                @Override
                public void onStart(String utteranceId) {
                }
            });
        } else {
            myLog("Build.VERSION.SDK_INT < 15");
            tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    onDoneSpeaking(utteranceId);
                }
            });
        }
    }

    private void onDoneSpeaking(String utteranceId) {
        myLog("");
        if (utteranceId.equals(DONE) || utteranceId == DONE) {
            audioManager.abandonAudioFocus(this);
            stopSelf();
        }
    }

    private void speakNow(String textToSpeak) {
        /*
            public int setPitch (float pitch)
                Sets the speech pitch for the TextToSpeech engine. This has no effect
                on any pre-recorded speech.

                Parameters
                    pitch : Speech pitch. 1.0 is the normal pitch, lower values lower the tone
                    of the synthesized voice, greater values increase it.

                Returns
                    ERROR or SUCCESS.
        */

        /*
            public int setSpeechRate (float speechRate)
                Sets the speech rate. This has no effect on any pre-recorded speech.

                Parameters
                    speechRate : Speech rate. 1.0 is the normal speech rate, lower values slow down the
                        speech (0.5 is half the normal speech rate), greater values accelerate it
                        (2.0 is twice the normal speech rate).

                Returns
                    ERROR or SUCCESS.
        */
        /*
            float pitch = 1.0f;
            float speed = 1.0f;
            tts.setPitch(pitch);
            tts.setSpeechRate(speed);
        */

        /*
            public int speak (CharSequence text, int queueMode, Bundle params, String utteranceId)
                Speaks the text using the specified queuing strategy and speech parameters, the
                text may be spanned with TtsSpans. This method is asynchronous, i.e. the method
                just adds the request to the queue of TTS requests and then returns. The synthesis
                might not have finished (or even started!) at the time when this method returns.
                In order to reliably detect errors during synthesis, we recommend setting an
                utterance progress listener (see setOnUtteranceProgressListener(UtteranceProgressListener))
                and using the KEY_PARAM_UTTERANCE_ID parameter.

                Parameters
                    text : The string of text to be spoken. No longer than
                        getMaxSpeechInputLength() characters.
                    queueMode : The queuing strategy to use, QUEUE_ADD or QUEUE_FLUSH.
                    params : Parameters for the request. Can be null. Supported parameter
                        names: KEY_PARAM_STREAM, KEY_PARAM_VOLUME, KEY_PARAM_PAN. Engine specific
                        parameters may be passed in but the parameter keys must be prefixed by the
                        name of the engine they are intended for. For example the keys
                        "com.svox.pico_foo" and "com.svox.pico:bar" will be passed to the engine
                        named "com.svox.pico" if it is being used.
                    utteranceId : An unique identifier for this request.

                Returns
                    ERROR or SUCCESS of queuing the speak operation.
        */

        /*
            public static final int QUEUE_FLUSH
                Queue mode where all entries in the playback queue (media to be played and text to
                be synthesized) are dropped and replaced by the new entry. Queues are flushed with
                respect to a given calling app. Entries in the queue from other callees are not discarded.

                Constant Value: 0 (0x00000000)
        */


        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, DONE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null,null);
        }
        else
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onUtteranceCompleted(String utteranceId) {

    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }
}
