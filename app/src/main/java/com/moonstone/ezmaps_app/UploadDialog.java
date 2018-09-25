package com.moonstone.ezmaps_app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;
import android.view.View.OnClickListener;


public class UploadDialog extends BottomSheetDialogFragment implements OnClickListener {

    private Button _choosePhote;
    private Button _takePhoto;
    private Button _cancelButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);

        Button _choosePhoto = view.findViewById(R.id.choosePhoto);
        Button _takePhoto = view.findViewById(R.id.takePhoto);
        Button _cancelButton = view.findViewById(R.id.cancelButton);

        _choosePhoto.setOnClickListener(this);
        _takePhoto.setOnClickListener(this);
        _cancelButton.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.choosePhoto:
                this.dismiss();


                startActivity(new Intent(getActivity(), ImageUpload.class));
                break;

            case R.id.takePhoto:
                this.dismiss();

                startActivity(new Intent(getActivity(), CameraUpload.class));
                break;

            case R.id.cancelButton:
                this.dismiss();
                break;
        }
    }



}