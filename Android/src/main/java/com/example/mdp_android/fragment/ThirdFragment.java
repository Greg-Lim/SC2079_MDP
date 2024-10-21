package com.example.mdp_android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mdp_android.MainActivity;
import com.example.mdp_android.R;
import com.example.mdp_android.ViewModels.TimerViewModel;

import org.json.JSONException;
import org.json.JSONObject;

public class ThirdFragment extends Fragment {

    private TextView timerTextView;
    private Button startButton, stopButton;
    private ProgressBar timerProgressBar;
    private TimerViewModel timerViewModel;
    private static final long TIMER_DURATION = 6 * 60 * 1000; // 6 minutes

    public ThirdFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_third, container, false);

        timerTextView = view.findViewById(R.id.timerTextView);
        timerProgressBar = view.findViewById(R.id.timerProgressBar);
        startButton = view.findViewById(R.id.startButton);
        stopButton = view.findViewById(R.id.stopButton);

        // Initialize ViewModel
        timerViewModel = new ViewModelProvider(requireActivity()).get(TimerViewModel.class);

        // Observe remaining time and update UI
        timerViewModel.getRemainingTime().observe(getViewLifecycleOwner(), millisRemaining -> {
            long minutes = millisRemaining / 1000 / 60;
            long seconds = (millisRemaining / 1000) % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
            timerTextView.setText(timeFormatted);

            // Update the progress bar
            int progress = (int) (millisRemaining * 100 / TIMER_DURATION);
            timerProgressBar.setProgress(progress);
        });

        // Observe if the timer is running
        timerViewModel.getIsTimerRunning().observe(getViewLifecycleOwner(), isRunning -> {
            if (isRunning) {
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            } else {
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });

        // Handle Start Button
        startButton.setOnClickListener(v -> {
            timerViewModel.startTimer();
            sendStartCommand(); // Send command to start robot
        });


        // Handle Stop Button
        stopButton.setOnClickListener(v -> {
            timerViewModel.stopTimer();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).robotStatusText.setText("Robot Stop");
            }
        });

        return view;
    }

    private void sendStartCommand() {
        // Logic to send the "start" command
        try {
            // Week 9 task 2
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("cat", "control");
            jsonMessage.put("value", "start");

            // Send the command (assuming MainActivity handles command sending)
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).sendCommand(jsonMessage);
                ((MainActivity) getActivity()).robotStatusText.setText("Moving");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // If you want to save timer state on fragment
    }
}