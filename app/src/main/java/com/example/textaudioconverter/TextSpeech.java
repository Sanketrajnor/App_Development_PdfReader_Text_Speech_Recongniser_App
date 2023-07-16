package com.example.textaudioconverter;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

public class TextSpeech extends AppCompatActivity {
    private TextToSpeech mTTS;
    private EditText mEditText;
    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;

    private ImageButton mButtonSave;
    private ImageButton mButtonSpeak;

    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.app_name));
        setContentView(R.layout.activity_text_to_speech);
        mButtonSpeak = findViewById(R.id.speak_img_btn);
        mButtonSave = findViewById(R.id.save_btn);

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(new Locale("En","IN"));

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        mButtonSpeak.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        //outputTextView=findViewById(R.id.text_view);
        mEditText = findViewById(R.id.edit_text);
        mSeekBarPitch = findViewById(R.id.pitch);
        mSeekBarSpeed = findViewById(R.id.speed_seek);
        mButtonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilenameDialog();
            }
        });

    }

    private void speak() {
        text = mEditText.getText().toString();
        float pitch = (float) mSeekBarPitch.getProgress() / 50;
        if (pitch < 0.1) pitch = 0.1f;
        float speed = (float) mSeekBarSpeed.getProgress() / 50;
        if (speed < 0.1) speed = 0.1f;

        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);

        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        // Generate a unique filename for the audio file
        String filename = "speech_" + System.currentTimeMillis() + ".mp3";

        // Generate the audio file path
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File audioFile = new File(path, filename);

        // Speak the text and save the audio file
        mTTS.synthesizeToFile(text, null, audioFile, filename);

    }


    private void showFilenameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save audio as");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filename = input.getText().toString().trim();
                if (TextUtils.isEmpty(filename)) {
                    Toast.makeText(TextSpeech.this, "Please enter a filename", Toast.LENGTH_SHORT).show();
                } else {
                    saveAudio(filename);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void saveAudio(String filename) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String utteranceId = UUID.randomUUID().toString();
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(dir, filename + ".mp3");

            mTTS.synthesizeToFile(text, null, file, utteranceId);

            Toast.makeText(this, "Audio saved in " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Saving audio is not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }
}