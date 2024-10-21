package com.example.mdp_android.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mdp_android.MainActivity;
import com.example.mdp_android.R;
import com.example.mdp_android.databinding.FragmentSecondBinding;
import com.example.mdp_android.map.MapView;

import org.json.JSONException;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private MapView map;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        // BUTTONS FOR GAME SETTING
        if (getActivity() != null) {
            Log.e("SecondFragment", "add_obstacle_btn is NOT null!");
            map = getActivity().findViewById(R.id.mapView);
        }
        // MUST HAVE to get id of the buttons
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize buttons using view binding
        Button addObstacle = binding.addObstacleBtn;
        Button removeObstacle = binding.removeObstacleBtn;
        Button sendMapDetails = binding.sendMapBtn;
        Button loadMap = binding.loadMapBtn;

        MainActivity mainActivity = (MainActivity) getActivity();

        // D.1) ADD OBSTACLE
        addObstacle.setOnClickListener(v -> {
            Log.e("SecondFragment", "OBSTACLE BUTTON CLICKED!");
            map.setIsAddingObstacle(true);
        });

        removeObstacle.setOnClickListener(v->{
            map.setIsRemoveObstacle(true);
        });
        sendMapDetails.setOnClickListener(v->{
            try {
                map.sendAllObstacles();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

            return view;

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*
        binding.buttonSecond.setOnClickListener(v ->
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment)
        );
        */

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}