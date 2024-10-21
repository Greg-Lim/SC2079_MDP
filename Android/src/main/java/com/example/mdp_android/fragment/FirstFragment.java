package com.example.mdp_android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mdp_android.ViewModels.ChatViewModel;
import com.example.mdp_android.MainActivity;
import com.example.mdp_android.R;
import com.example.mdp_android.bluetooth.BluetoothChat;
import com.example.mdp_android.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private Button send;
    private EditText textBox;
    private TextView logActivityTextView;

    // Declare ViewModel
    private ChatViewModel chatViewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        chatViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class); // Ensure ViewModel is activity-scoped

        send = view.findViewById(R.id.messageButton);
        textBox = view.findViewById(R.id.textBoxChat);
        logActivityTextView = view.findViewById(R.id.logActivity);

        // Observe the chat log in the ViewModel
        chatViewModel.getChatLog().observe(getViewLifecycleOwner(), log -> {
            logActivityTextView.setText(log);  // Update TextView with the current chat log
        });

        send.setOnClickListener(v -> {
            String receivedText = textBox.getText().toString();

            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                BluetoothChat connectedThread = mainActivity.getConnectedThread();
                chatViewModel.appendToLog(receivedText);
                if (connectedThread != null) {
                    // If connectedThread is not null, send the message
                    connectedThread.write(receivedText.getBytes());

                } else {
                    // If connectedThread is null, show an error
                    Toast.makeText(getContext(), "Bluetooth not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
