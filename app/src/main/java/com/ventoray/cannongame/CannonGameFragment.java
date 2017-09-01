package com.ventoray.cannongame;


import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class CannonGameFragment extends Fragment {

    private CannonView mCannonView;


    public CannonGameFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_game, container, false);
        mCannonView = (CannonView) view.findViewById(R.id.cannonView);

        return view;
    }

    /**
     *  Set up volume control once activity is created
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Allows volume keys to set game volume! - probably because that's what we specify when building the sound pool
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }


    @Override
    public void onPause() {
        super.onPause();
        mCannonView.stopGame();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCannonView.releaseResources();
    }
}
