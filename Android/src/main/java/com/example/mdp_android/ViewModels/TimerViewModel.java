package com.example.mdp_android.ViewModels;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TimerViewModel extends ViewModel {

    private final MutableLiveData<Long> remainingTime = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isTimerRunning = new MutableLiveData<>(false);
    private long timerDuration = 6 * 60 * 1000; // 6 minutes
    private CountDownTimer countDownTimer;

    public LiveData<Long> getRemainingTime() {
        return remainingTime;
    }

    public LiveData<Boolean> getIsTimerRunning() {
        return isTimerRunning;
    }

    public void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Cancel any existing timer
        }

        countDownTimer = new CountDownTimer(timerDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime.setValue(millisUntilFinished); // Update remaining time
            }

            @Override
            public void onFinish() {
                remainingTime.setValue(0L);
                isTimerRunning.setValue(false);
            }
        };

        isTimerRunning.setValue(true);
        countDownTimer.start();
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning.setValue(false);
    }

    public void setTimerDuration(long duration) {
        this.timerDuration = duration;
    }

    public void saveTimerState(long remainingMillis) {
        timerDuration = remainingMillis;
        isTimerRunning.setValue(false);
    }
}
