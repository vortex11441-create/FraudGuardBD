package com.quantumbuilders.fraudguardbd;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.io.Serializable;

public class ResultActivity extends AppCompatActivity {

    public static class AnalysisData implements Serializable {
        public String originalMessage;
        public ScamResult result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        AnalysisData data = (AnalysisData) getIntent().getSerializableExtra("analysis_data");
        if (data == null) {
            finish();
            return;
        }

        AppBarLayout appBar = findViewById(R.id.resultAppBar);
        TextView riskTitle = findViewById(R.id.resRiskTitle);
        TextView scamType = findViewById(R.id.resScamType);
        ProgressBar progressBar = findViewById(R.id.scoreProgressBar);
        TextView scoreText = findViewById(R.id.scoreText);
        TextView reason = findViewById(R.id.resReason);
        ChipGroup flagsGroup = findViewById(R.id.redFlagsGroup);
        LinearLayout adviceContainer = findViewById(R.id.adviceContainer);
        TextView safeReply = findViewById(R.id.resSafeReply);
        TextView originalMsg = findViewById(R.id.resOriginalMessage);

        ScamResult res = data.result;
        
        // Dynamic Risk Theme
        int color;
        int lightColor;
        String riskText;
        
        if ("High".equals(res.risk)) {
            color = Color.parseColor("#E53935");
            lightColor = Color.parseColor("#FFEBEE");
            riskText = "HIGH RISK DETECTED";
        } else if ("Medium".equals(res.risk)) {
            color = Color.parseColor("#FB8C00");
            lightColor = Color.parseColor("#FFF3E0");
            riskText = "MEDIUM RISK ALERT";
        } else {
            color = Color.parseColor("#43A047");
            lightColor = Color.parseColor("#E8F5E9");
            riskText = "NO SCAM DETECTED";
        }
        
        appBar.setBackgroundColor(color);
        riskTitle.setText(riskText);
        scamType.setText(res.type);
        
        progressBar.setProgress(res.score);
        scoreText.setText(String.valueOf(res.score));
        scoreText.setTextColor(color);
        
        reason.setText(res.reason);
        safeReply.setText(res.safeReply);
        originalMsg.setText(data.originalMessage);

        // Add Chips with theme
        flagsGroup.removeAllViews();
        for (String flag : res.redFlags) {
            Chip chip = new Chip(this);
            chip.setText(flag);
            chip.setChipBackgroundColorResource(android.R.color.white);
            chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(color));
            chip.setChipStrokeWidth(2.0f);
            chip.setTextColor(color);
            flagsGroup.addView(chip);
        }

        // Add Advice with theme
        adviceContainer.removeAllViews();
        for (String adv : res.advice) {
            CheckBox cb = new CheckBox(this);
            cb.setText(adv);
            cb.setChecked(true);
            cb.setEnabled(false);
            cb.setTextColor(Color.parseColor("#546E7A"));
            cb.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#43A047")));
            adviceContainer.addView(cb);
        }

        findViewById(R.id.btnCopySafeReply).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Safe Reply", res.safeReply);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Response copied to clipboard", Toast.LENGTH_SHORT).show();
        });
    }
}