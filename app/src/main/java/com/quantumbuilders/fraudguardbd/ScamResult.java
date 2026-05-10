package com.quantumbuilders.fraudguardbd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScamResult implements Serializable {
    public String type;
    public String risk; // Low, Medium, High
    public int score;
    public String reason;
    public List<String> redFlags = new ArrayList<>();
    public List<String> advice = new ArrayList<>();
    public String safeReply;
    public boolean isFromCloudAi;

    public ScamResult() {}
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Type: ").append(type != null ? type : "Unknown").append("\n");
        sb.append("Risk: ").append(risk != null ? risk : "Unknown").append("\n");
        sb.append("Score: ").append(score).append("/100\n\n");
        sb.append("Reason:\n").append(reason != null ? reason : "N/A").append("\n\n");
        
        sb.append("Red Flags:\n");
        if (redFlags != null && !redFlags.isEmpty()) {
            for (String flag : redFlags) {
                sb.append("- ").append(flag).append("\n");
            }
        } else {
            sb.append("- None detected\n");
        }
        sb.append("\n");
        
        sb.append("Safety Advice:\n");
        if (advice != null && !advice.isEmpty()) {
            for (String adv : advice) {
                sb.append("- ").append(adv).append("\n");
            }
        } else {
            sb.append("- Be cautious.\n");
        }
        sb.append("\n");
        
        sb.append("Safe Reply:\n").append(safeReply != null ? safeReply : "Do not reply.");
        return sb.toString();
    }
}