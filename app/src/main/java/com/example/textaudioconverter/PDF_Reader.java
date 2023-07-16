package com.example.textaudioconverter;

import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.textaudioconverter.databinding.ActivityPdfReaderBinding;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;

public class PDF_Reader extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String TAG = "PDF_Reader";
    private ActivityPdfReaderBinding binding;
    private TextToSpeech tts;
    private PdfReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.fabPlayTts.setEnabled(false);
        hideControls();

        tts = new TextToSpeech(this, this){

        };

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                binding.fabPlayTts.setImageDrawable(
                        AppCompatResources.getDrawable(PDF_Reader.this, R.drawable.baseline_stop_24)
                );
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                binding.fabPlayTts.setImageDrawable(
                        AppCompatResources.getDrawable(PDF_Reader.this, R.drawable.baseline_play_arrow_24
                        )
                );
            }

            @Override
            public void onDone(String utteranceId) {
                binding.fabPlayTts.setImageDrawable(
                        AppCompatResources.getDrawable(PDF_Reader.this, R.drawable.baseline_play_arrow_24
                        )
                );

                nextPageSpeak();
            }

            @Override
            public void onError(String utteranceId) {
                Toast.makeText(PDF_Reader.this, "Error : " + utteranceId, Toast.LENGTH_LONG).show();
            }
        });

        ActivityResultLauncher<String> selectPdfResult = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (loadPdf(uri)) {
                        setPageContent(1);
                    }
                });

        binding.fabSelectFile.setOnClickListener(v -> {
            pickFile(selectPdfResult);
        });

        binding.fabNextPage.setOnClickListener(v -> {
            nextPage();
        });

        binding.fabPreviousPage.setOnClickListener(v -> {
            if (reader != null) {
                int currentPgNo = Integer.parseInt(binding.tvCurrentPgNo.getText().toString());
                if (1 < currentPgNo) {
                    currentPgNo--;
                    setPageContent(currentPgNo);
                    binding.tvCurrentPgNo.setText(String.valueOf(currentPgNo));

                    if (tts.isSpeaking()) {
                        tts.stop();
                        speak(binding.tvPageContent.getText().toString());
                    }
                }
            }
        });

        binding.fabPlayTts.setOnClickListener(v -> {
            if (reader != null) {
                if (tts.isSpeaking()) {
                    tts.stop();
                    binding.fabPlayTts.setImageDrawable(AppCompatResources.getDrawable(PDF_Reader.this, R.drawable.baseline_play_arrow_24));
                } else {
                    speak(binding.tvPageContent.getText().toString());
                }
            }
        });

        binding.fabAddBig.setOnClickListener(v -> {
            pickFile(selectPdfResult);
        });
    }

    private void nextPageSpeak() {
        if (reader != null) {
            int currentPgNo = Integer.parseInt(binding.tvCurrentPgNo.getText().toString());
            if (currentPgNo < reader.getNumberOfPages()) {
                currentPgNo++;
                setPageContent(currentPgNo);
                binding.tvCurrentPgNo.setText(String.valueOf(currentPgNo));

                if (tts.isSpeaking()) {
                    tts.stop();
                }
                speak(binding.tvPageContent.getText().toString());
            }
        }
    }

    //Changes to Next Page
    private void nextPage() {
        if (reader != null) {
            int currentPgNo = Integer.parseInt(binding.tvCurrentPgNo.getText().toString());
            if (currentPgNo < reader.getNumberOfPages()) {
                currentPgNo++;
                setPageContent(currentPgNo);
                binding.tvCurrentPgNo.setText(String.valueOf(currentPgNo));

                if (tts.isSpeaking()) {
                    tts.stop();
                    speak(binding.tvPageContent.getText().toString());
                }
            }
        }
    }

    private void pickFile(ActivityResultLauncher<String> selectPdfResult) {
        if (tts.isSpeaking()) {
            tts.stop();
        }

        try {
            selectPdfResult.launch("application/pdf");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No File Picker Found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "pickFile: " + e.getMessage());
        }
    }

    //Sets the reader
    private boolean loadPdf(Uri uri) {
        try {
            reader = new PdfReader(getContentResolver().openInputStream(uri));

            if (reader.getNumberOfPages() == 0) {
                Toast.makeText(this, "Empty Pdf", Toast.LENGTH_SHORT).show();
                return false;
            }

            showControls();

            binding.tvTotalPages.setText(Integer.toString(reader.getNumberOfPages()));
            binding.tvPgNoSeperator.setText("/");
            binding.tvCurrentPgNo.setText("1");
            return true;

        } catch (Exception e) {
            Toast.makeText(this, "Error While Reading PDF", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "loadPdf: " + e.getMessage());
            return false;
        }
    }

    //Sets Text of Page to the TextView
    private void setPageContent(int pageNo) {

        if (pageNo <= reader.getNumberOfPages()) {
            try {
                binding.tvPageContent.setText("Page " + pageNo + "\n\n" + PdfTextExtractor.getTextFromPage(reader, pageNo).trim());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //For TextToSpeech Init
    @Override
    public void onInit(int status) {
        binding.fabPlayTts.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }

    //Speaks the text with TextToSpeech
    private void speak(String text) {
        tts.speak(text.trim(), TextToSpeech.QUEUE_FLUSH, null,"PDF_Reader");
        String filename = "speech_" + System.currentTimeMillis() + ".mp3";

        // Generate the audio file path
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File audioFile = new File(path, filename);

        // Speak the text and save the audio file
        tts.synthesizeToFile(text.trim(), null, audioFile, filename);
   }

    private void hideControls() {
        binding.fabPlayTts.setVisibility(View.GONE);
        binding.fabPreviousPage.setVisibility(View.GONE);
        binding.fabNextPage.setVisibility(View.GONE);
        binding.fabSelectFile.setVisibility(View.GONE);
        binding.tvCurrentPgNo.setVisibility(View.GONE);
        binding.tvPgNoSeperator.setVisibility(View.GONE);
        binding.tvTotalPages.setVisibility(View.GONE);
        binding.tvHeadTop.setVisibility(View.GONE);
        binding.ivHeadTop.setVisibility(View.GONE);
        binding.lineHeadTop.setVisibility(View.GONE);
        binding.tvPageContent.setVisibility(View.GONE);
    }

    private void showControls() {
        binding.fabAddBig.setVisibility(View.GONE);
        binding.tvHead2.setVisibility(View.GONE);
        binding.fabPlayTts.setVisibility(View.VISIBLE);
        binding.fabPreviousPage.setVisibility(View.VISIBLE);
        binding.fabNextPage.setVisibility(View.VISIBLE);
        binding.fabSelectFile.setVisibility(View.VISIBLE);
        binding.tvCurrentPgNo.setVisibility(View.VISIBLE);
        binding.tvPgNoSeperator.setVisibility(View.VISIBLE);
        binding.tvTotalPages.setVisibility(View.VISIBLE);
        binding.tvHeadTop.setVisibility(View.VISIBLE);
        binding.ivHeadTop.setVisibility(View.VISIBLE);
        binding.lineHeadTop.setVisibility(View.VISIBLE);
        binding.tvPageContent.setVisibility(View.VISIBLE);
    }
}

