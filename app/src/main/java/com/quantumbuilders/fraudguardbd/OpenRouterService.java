package com.quantumbuilders.fraudguardbd;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenRouterService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    public static ScamResult analyze(String messageText) throws Exception {
        String apiKey = BuildConfig.OPENROUTER_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new Exception("OpenRouter API Key is missing in local.properties");
        }

        String systemPrompt = "You are FraudGuard BD, a specialized security expert in Bangladeshi SMS scam detection.\n" +
                "Your goal is to analyze SMS messages for fraudulent patterns common in Bangladesh (bKash, Nagad, fake jobs, lottery, etc.).\n" +
                "Be extremely precise. If a message asks for OTP, PIN, or payment for a prize, it is 100% a HIGH RISK scam.\n\n" +
                "Output must be strictly valid JSON in this format:\n" +
                "{\n" +
                "  \"type\": \"One of: OTP Scam, Prize Scam, Job Scam, Loan Scam, Courier Scam, SIM/NID Scam, Legal Threat Scam, Facebook Scam, Charity Scam, Suspicious Message, No Strong Scam Pattern\",\n" +
                "  \"risk\": \"One of: Low, Medium, High\",\n" +
                "  \"score\": 0 to 100,\n" +
                "  \"reason\": \"Detailed reason for your choice\",\n" +
                "  \"redFlags\": [\"list of flags\"],\n" +
                "  \"advice\": [\"specific safety advice\"],\n" +
                "  \"safeReply\": \"A polite but firm safe response in Bangla\"\n" +
                "}";

        String userPrompt = "Analyze this Bangladeshi SMS and provide a security report:\n\n" +
                "\"" + messageText + "\"\n\n" +
                "Examples for reference:\n" +
                "1. \"বিকাশ: আপনার একাউন্ট স্থগিত করা হয়েছে। পিন রিসেট করতে কল করুন...\" -> HIGH RISK (OTP/PIN Scam)\n" +
                "2. \"অভিনন্দন! আপনি ৫০,০০০ টাকা জিতেছেন। কুরিয়ার চার্জ ২০০ টাকা দিন...\" -> HIGH RISK (Prize Scam)\n" +
                "3. \"Your parcel is arriving today. Tracking ID: 12345\" -> LOW RISK (Legitimate)\n" +
                "4. \"Dear Customer, your internet bill is due. Pay via app.\" -> LOW RISK (Legitimate)";

        JSONObject requestBody = new JSONObject();
        // Using a slightly more capable model for better results
        requestBody.put("model", "google/gemini-2.0-flash-001");
        
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
        messages.put(new JSONObject().put("role", "user").put("content", userPrompt));
        requestBody.put("messages", messages);

        // Tuning for better accuracy
        requestBody.put("temperature", 0.1); // Lower temperature = more stable and accurate results
        requestBody.put("top_p", 0.9);
        
        JSONObject responseFormat = new JSONObject();
        responseFormat.put("type", "json_object");
        requestBody.put("response_format", responseFormat);

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("HTTP-Referer", "https://github.com/quantumbuilders/fraudguardbd"); 
        conn.setRequestProperty("X-Title", "FraudGuard BD"); 
        
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(20000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        InputStream stream = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
        
        if (stream == null) {
            throw new Exception("HTTP " + responseCode + ": No response body from OpenRouter.");
        }

        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }
        }
        String responseBody = responseBuilder.toString();

        if (responseCode == 200) {
            return parseOpenRouterResponse(responseBody);
        } else {
            throw new Exception("OpenRouter Error (HTTP " + responseCode + "): " + responseBody);
        }
    }

    private static ScamResult parseOpenRouterResponse(String jsonString) throws Exception {
        JSONObject root = new JSONObject(jsonString);
        JSONArray choices = root.optJSONArray("choices");
        if (choices == null || choices.length() == 0) throw new Exception("OpenRouter returned no choices.");
        
        JSONObject message = choices.getJSONObject(0).optJSONObject("message");
        if (message == null) throw new Exception("OpenRouter response message is missing.");
        
        String content = message.optString("content", "").trim();
        if (content.isEmpty()) throw new Exception("OpenRouter returned empty content.");

        JSONObject resultJson;
        // Clean markdown backticks if AI accidentally included them
        if (content.startsWith("```json")) {
            content = content.replace("```json", "").replace("```", "").trim();
        }
        
        if (content.startsWith("[")) {
            JSONArray array = new JSONArray(content);
            resultJson = array.getJSONObject(0);
        } else {
            resultJson = new JSONObject(content);
        }
        
        ScamResult result = new ScamResult();
        result.isFromCloudAi = true;
        result.type = resultJson.optString("type", "Unknown");
        result.risk = resultJson.optString("risk", "Medium");
        result.score = resultJson.optInt("score", 50);
        result.reason = resultJson.optString("reason", "");
        result.safeReply = resultJson.optString("safeReply", "");

        JSONArray flagsArray = resultJson.optJSONArray("redFlags");
        if (flagsArray != null) {
            for (int i = 0; i < flagsArray.length(); i++) {
                result.redFlags.add(flagsArray.getString(i));
            }
        }

        JSONArray adviceArray = resultJson.optJSONArray("advice");
        if (adviceArray != null) {
            for (int i = 0; i < adviceArray.length(); i++) {
                result.advice.add(adviceArray.getString(i));
            }
        }

        return result;
    }
}