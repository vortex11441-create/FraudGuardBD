package com.quantumbuilders.fraudguardbd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null || !intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            return;
        }

        final PendingResult pendingResult = goAsync();
        
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            String format = bundle.getString("format");
            if (pdus != null) {
                StringBuilder fullMessage = new StringBuilder();
                for (Object pdu : pdus) {
                    SmsMessage smsMessage;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
                    } else {
                        smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    }
                    fullMessage.append(smsMessage.getMessageBody());
                }
                
                String message = fullMessage.toString();
                SharedPreferences prefs = context.getSharedPreferences("FraudGuardPrefs", Context.MODE_PRIVATE);
                boolean useCloudAi = prefs.getBoolean("use_cloud_ai_auto", false);
                boolean autoBlock = prefs.getBoolean("auto_block_scams", false);
                
                new Thread(() -> {
                    try {
                        ScamResult result;
                        if (useCloudAi && !BuildConfig.OPENROUTER_API_KEY.isEmpty()) {
                            try {
                                result = OpenRouterService.analyze(message);
                            } catch (Exception e) {
                                result = ScamDetector.analyzeLocally(message);
                            }
                        } else {
                            result = ScamDetector.analyzeLocally(message);
                        }

                        boolean isScam = "High".equals(result.risk) || "Medium".equals(result.risk);
                        
                        if (isScam) {
                            // Notify user
                            NotificationHelper.showScamAlert(context, message, result);
                            
                            // If auto-block is ON, try to stop the message from reaching other apps
                            if (autoBlock) {
                                abortBroadcast();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("SmsReceiver", "Error processing SMS", e);
                    } finally {
                        pendingResult.finish();
                    }
                }).start();
            } else {
                pendingResult.finish();
            }
        } else {
            pendingResult.finish();
        }
    }
}