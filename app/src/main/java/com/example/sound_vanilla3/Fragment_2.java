package com.example.sound_vanilla3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class Fragment_2 extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView =(ViewGroup) inflater.inflate(
                R.layout.frame2,container,false);
        MainActivity.pickValue = 2;
        return  rootView;
    }

}
