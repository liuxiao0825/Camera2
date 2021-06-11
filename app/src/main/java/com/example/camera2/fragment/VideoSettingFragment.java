package com.example.camera2.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.camera2.MainActivity;
import com.example.camera2.R;
import com.example.camera2.camera.Camera2Proxy;
import com.example.camera2.surfaceview.Camera2SurfaceView;
import com.example.camera2.utils.ImageUtils;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VideoSettingFragment extends Fragment implements View.OnClickListener {

    private View view;
    private static final String TAG = "VideoSettingFragment";
    private Context context;
    private Camera2Proxy mCameraProxy;
    private Camera2SurfaceView mCameraView;
    private ImageView videoFlashOn;
    private ImageView videoFlashOff;
    private ImageView videoSetting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_video_setting, container, false);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //打开闪光灯
        videoFlashOn = view.findViewById(R.id.video_flash_on);
        videoFlashOn.setOnClickListener(this);
        //关闭闪光灯
        videoFlashOff = view.findViewById(R.id.video_flash_off);
        videoFlashOff.setOnClickListener(this);
        //设置
        videoSetting = view.findViewById(R.id.video_setting);
        videoSetting.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCameraView = getActivity().findViewById(R.id.camera_view);
        mCameraProxy = mCameraView.getCameraProxy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video_flash_on:
                videoFlashOn.setVisibility(View.GONE);
                videoFlashOff.setVisibility(View.VISIBLE);
                mCameraProxy.flashOn();
                Toast.makeText(context,"开启闪光灯",Toast.LENGTH_SHORT).show();
                break;
            case R.id.video_flash_off:
                videoFlashOn.setVisibility(View.VISIBLE);
                videoFlashOff.setVisibility(View.GONE);
                mCameraProxy.flashOff();
                Toast.makeText(context,"关闭闪光灯",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}