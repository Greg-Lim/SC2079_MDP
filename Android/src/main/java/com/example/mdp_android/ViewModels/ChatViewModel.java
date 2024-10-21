package com.example.mdp_android.ViewModels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChatViewModel extends ViewModel {
    private static final int MAX_LINES = 10;
    // MutableLiveData to hold the chat message log
    private final MutableLiveData<String> chatLog = new MutableLiveData<>("");

    // Getter for chatLog as LiveData
    public LiveData<String> getChatLog() {
        return chatLog;
    }

    // Append message to the current log
    public void appendToLog(String message) {
        String currentLog = chatLog.getValue();
        if (currentLog == null) currentLog = "";

        // Split the log into lines and check if it exceeds the max lines
        String[] lines = currentLog.split("\n");
        if (lines.length > MAX_LINES) {
            // Keep only the last MAX_LINES lines
            currentLog = String.join("\n", java.util.Arrays.copyOfRange(lines, lines.length - MAX_LINES, lines.length)) +"\n" + message + "\n";
        }
        else{
            currentLog += message + "\n";
        }

        // Set the updated log
        chatLog.postValue(currentLog);

        // Log to confirm the update
        Log.d("ChatViewModel", "Log updated: " + chatLog.getValue());
    }
}
