package com.example.textaudioconverter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton t2s;
        t2s=findViewById(R.id.imageButton3);

        Intent text2speech;
        text2speech = new Intent(MainActivity.this, TextSpeech.class);

        t2s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(text2speech);
            }
        });

        ImageButton p2s;
        p2s=findViewById(R.id.imageButton2);

        Intent pdfReder;
        pdfReder = new Intent(MainActivity.this, PDF_Reader.class);

        p2s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(pdfReder);
            }
        });

        ImageButton s2t;
        s2t=findViewById(R.id.imageButton);

        Intent Speech2text;
        Speech2text = new Intent(MainActivity.this, SpeechToText.class);

        s2t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Speech2text);
            }
        });

    }
}