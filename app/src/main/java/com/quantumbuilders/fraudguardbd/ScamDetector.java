package com.quantumbuilders.fraudguardbd;

import java.util.ArrayList;

public class ScamDetector {

    public static ScamResult analyzeLocally(String text) {
        String lowerText = text.toLowerCase();
        ScamResult result = new ScamResult();
        result.isFromCloudAi = false;
        result.score = 0;
        
        // Critical Security Indicators
        boolean hasOTP = lowerText.contains("otp") || lowerText.contains("code") || lowerText.contains("কোড");
        boolean hasPIN = lowerText.contains("pin") || lowerText.contains("password") || lowerText.contains("পাসওয়ার্ড") || lowerText.contains("পিন");
        
        // Scam Type Indicators (Bangla & English)
        boolean hasMoney = lowerText.contains("টাকা") || lowerText.contains("tk") || lowerText.contains("money") 
                            || lowerText.contains("bkash") || lowerText.contains("nagad") || lowerText.contains("rocket")
                            || lowerText.contains("বিকাশ") || lowerText.contains("নগদ") || lowerText.contains("রকেট")
                            || lowerText.contains("ফি") || lowerText.contains("fee") || lowerText.contains("charge") || lowerText.contains("চার্জ");
                            
        boolean hasUrgency = lowerText.contains("urgent") || lowerText.contains("immediately") || lowerText.contains("জরুরি")
                            || lowerText.contains("এখনই") || lowerText.contains("বন্ধ") || lowerText.contains("বাতিল") 
                            || lowerText.contains("block") || lowerText.contains("suspend");
                            
        boolean hasLink = lowerText.contains("http") || lowerText.contains("www.") || lowerText.contains(".com") || lowerText.contains(".net") || lowerText.contains("tinyurl") || lowerText.contains("bit.ly");
        
        boolean hasPrize = lowerText.contains("prize") || lowerText.contains("win") || lowerText.contains("অভিনন্দন") || lowerText.contains("জিতেছেন") 
                            || lowerText.contains("উপহার") || lowerText.contains("লটারি") || lowerText.contains("lottery");
                            
        boolean hasJob = lowerText.contains("job") || lowerText.contains("আয়") || lowerText.contains("income") || lowerText.contains("চাকরি") || lowerText.contains("পার্ট টাইম");
        
        boolean hasCourier = lowerText.contains("parcel") || lowerText.contains("courier") || lowerText.contains("পার্সেল") || lowerText.contains("কুরিয়ার") || lowerText.contains("ডেলিভারি");
        
        boolean hasVerification = lowerText.contains("nid") || lowerText.contains("verify") || lowerText.contains("verifi") || lowerText.contains("ভেরিফাই") || lowerText.contains("এনআইডি") || lowerText.contains("সিম");
        
        boolean hasLegalThreat = lowerText.contains("police") || lowerText.contains("legal") || lowerText.contains("court") || lowerText.contains("থানা") || lowerText.contains("মামলা") || lowerText.contains("আইনি");

        boolean hasFacebook = lowerText.contains("facebook") || lowerText.contains("fb") || lowerText.contains("ফেসবুক") || lowerText.contains("recovery");

        // Scoring Logic
        if (hasOTP) { result.score += 60; result.redFlags.add("Asking for OTP or Verification Code"); }
        if (hasPIN) { result.score += 60; result.redFlags.add("Asking for PIN or Password"); }
        if (hasMoney) { result.score += 35; result.redFlags.add("Requesting money, fees, or mobile banking payment"); }
        if (hasUrgency) { result.score += 30; result.redFlags.add("Urgent warning, threat, or pressure"); }
        if (hasLink) { result.score += 25; result.redFlags.add("Contains a suspicious link"); }
        if (hasPrize) { result.score += 40; result.redFlags.add("Unrealistic prize, lottery, or gift offer"); }
        if (hasJob) { result.score += 35; result.redFlags.add("Fake job or high-income offer"); }
        if (hasCourier) { result.score += 30; result.redFlags.add("Suspicious courier/parcel notification"); }
        if (hasVerification) { result.score += 40; result.redFlags.add("Requesting NID or identity verification"); }
        if (hasLegalThreat) { result.score += 50; result.redFlags.add("Legal threat or police impersonation"); }
        if (hasFacebook) { result.score += 30; result.redFlags.add("Suspicious Facebook account activity notification"); }

        // Risk Classification
        // Hard overrides for critical red flags
        boolean isCritical = hasOTP || hasPIN || hasLegalThreat || (hasMoney && hasUrgency) || (hasLink && (hasOTP || hasPIN || hasVerification));
        
        if (result.score >= 55 || isCritical) {
            result.risk = "High";
        } else if (result.score >= 25 || hasPrize || hasJob || hasCourier || hasVerification || hasFacebook) {
            result.risk = "Medium";
        } else {
            result.risk = "Low";
        }

        // Type Classification
        if (hasOTP || hasPIN) result.type = "OTP/PIN Scam";
        else if (hasPrize) result.type = "Prize Scam";
        else if (hasJob) result.type = "Job Scam";
        else if (hasCourier) result.type = "Courier Scam";
        else if (hasVerification) result.type = "SIM/NID Scam";
        else if (hasLegalThreat) result.type = "Legal Threat Scam";
        else if (hasFacebook) result.type = "Facebook Scam";
        else if (hasMoney) result.type = "Financial Scam";
        else if (result.risk.equals("Low")) result.type = "No Strong Scam Pattern";
        else result.type = "Suspicious Message";

        result.reason = "Detected specific red flags commonly used in Bangladeshi SMS scams (keywords, urgency, and sensitive data requests).";
        
        if (!result.risk.equals("Low")) {
            result.advice.add("Do not share OTP, PIN, password, card number, CVV, NID, or bank details.");
            result.advice.add("Do not click unknown links.");
            result.advice.add("Do not send money to unknown numbers for 'fees' or 'charges'.");
            result.advice.add("Verify through official support or by visiting the office in person.");
            result.safeReply = "I cannot share personal information or make payments via SMS. I will verify this through official channels.";
        } else {
            result.advice.add("Message appears normal, but always stay alert and never share secrets.");
            result.safeReply = "Ok.";
        }
        
        return result;
    }
}