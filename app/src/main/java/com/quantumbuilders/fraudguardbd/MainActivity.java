package com.quantumbuilders.fraudguardbd;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    private EditText messageInput;
    private TextView aiStatusText;
    private ImageView aiStatusIcon;
    private SwitchMaterial aiSwitch;

    private int sampleIndex = 0;
    private final String[] samples = {
        "আপনার অ্যাকাউন্ট সাময়িকভাবে বন্ধ করা হয়েছে। চালু রাখতে OTP কোডটি রিপ্লাই করুন।",
        "অভিনন্দন! আপনি ৫০,০০০ টাকা জিতেছেন। টাকা নিতে এখনই রেজিস্ট্রেশন করুন: fake-link",
        "ঘরে বসে দৈনিক ২০০০ টাকা আয় করুন। কোনো অভিজ্ঞতা লাগবে না। Join করতে ফি মাত্র ৫০০ টাকা।",
        "আপনার পার্সেল আটকে আছে। ডেলিভারি চার্জ ৭০ টাকা এখনই পাঠান, না হলে পার্সেল বাতিল হবে।"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageInput = findViewById(R.id.messageInput);
        aiStatusText = findViewById(R.id.geminiStatusText); 
        aiStatusIcon = findViewById(R.id.geminiStatusIcon); 
        aiSwitch = findViewById(R.id.geminiSwitch);         
        SwitchMaterial blockSwitch = findViewById(R.id.blockSwitch);

        Button btnCheckLocal = findViewById(R.id.btnCheckLocal);
        Button btnCheckCloudAi = findViewById(R.id.btnCheckGemini); 
        Button btnLoadSample = findViewById(R.id.btnLoadSample);

        SharedPreferences prefs = getSharedPreferences("FraudGuardPrefs", MODE_PRIVATE);
        aiSwitch.setChecked(prefs.getBoolean("use_cloud_ai_auto", false));
        aiSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && BuildConfig.OPENROUTER_API_KEY.isEmpty()) {
                Toast.makeText(this, "Add OPENROUTER_API_KEY in local.properties", Toast.LENGTH_LONG).show();
                aiSwitch.setChecked(false);
                return;
            }
            prefs.edit().putBoolean("use_cloud_ai_auto", isChecked).apply();
        });

        blockSwitch.setChecked(prefs.getBoolean("auto_block_scams", false));
        blockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_block_scams", isChecked).apply();
        });

        updateStatusUI();

        btnCheckLocal.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter or paste a message to scan", Toast.LENGTH_SHORT).show();
                return;
            }
            ScamResult result = ScamDetector.analyzeLocally(text);
            showResultScreen(text, result);
        });

        btnCheckCloudAi.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter or paste a message to scan", Toast.LENGTH_SHORT).show();
                return;
            }
            if (BuildConfig.OPENROUTER_API_KEY.isEmpty()) {
                Toast.makeText(this, "API Key is missing from local.properties", Toast.LENGTH_LONG).show();
                return;
            }
            
            Toast.makeText(this, "AI is analyzing message patterns...", Toast.LENGTH_SHORT).show();
            
            new Thread(() -> {
                try {
                    ScamResult result = OpenRouterService.analyze(text);
                    runOnUiThread(() -> showResultScreen(text, result));
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "AI Analysis Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
        });

        btnLoadSample.setOnClickListener(v -> {
            messageInput.setText(samples[sampleIndex]);
            sampleIndex = (sampleIndex + 1) % samples.length;
        });

        findViewById(R.id.btnShareApp).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Download FraudGuard BD");
            intent.putExtra(Intent.EXTRA_TEXT, "Protect yourself from SMS scams in Bangladesh! Download FraudGuard BD: [Your Link Here]");
            startActivity(Intent.createChooser(intent, "Share via"));
        });

        requestPermissions();
        
        if (getIntent().hasExtra("scam_message")) {
            String msg = getIntent().getStringExtra("scam_message");
            messageInput.setText(msg);
            btnCheckLocal.performClick();
        }
    }

    private void updateStatusUI() {
        if (BuildConfig.OPENROUTER_API_KEY.isEmpty()) {
            aiStatusText.setText("OFFLINE");
            aiStatusText.setTextColor(Color.parseColor("#757575"));
            aiStatusIcon.setColorFilter(Color.parseColor("#757575"));
        } else {
            aiStatusText.setText("READY");
            aiStatusText.setTextColor(Color.parseColor("#4CAF50"));
            aiStatusIcon.setColorFilter(Color.parseColor("#4CAF50"));
        }
    }

    private void showResultScreen(String message, ScamResult result) {
        Intent intent = new Intent(this, ResultActivity.class);
        ResultActivity.AnalysisData data = new ResultActivity.AnalysisData();
        data.originalMessage = message;
        data.result = result;
        intent.putExtra("analysis_data", data);
        startActivity(intent);
    }

    private void requestPermissions() {
        String[] permissions = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            permissions = new String[]{
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
            };
        }
        
        boolean needRequest = false;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needRequest = true;
                break;
            }
        }
        
        if (needRequest) {
            ActivityCompat.requestPermissions(this, permissions, 100);
        }
    }
}