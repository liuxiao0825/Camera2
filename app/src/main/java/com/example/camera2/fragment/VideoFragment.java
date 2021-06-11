package com.example.camera2.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;

import com.example.camera2.R;
import com.example.camera2.camera.Camera2Proxy;
import com.example.camera2.surfaceview.Camera2SurfaceView;
import com.example.camera2.utils.ImageUtils;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VideoFragment extends Fragment implements View.OnClickListener {

    private View view;
    private static final String TAG = "VideoFragment";
    private Camera2Proxy mCameraProxy;
    private Camera2SurfaceView mCameraView;
    private ImageView startVideo;
    private ImageView stopVideo;
    private Chronometer time;
    private ImageView videoPicture;
    private ImageView switchCamera;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_video, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //开始录像
        startVideo = view.findViewById(R.id.video_start);
        startVideo.setOnClickListener(this);
        //停止录像
        stopVideo = view.findViewById(R.id.video_stop);
        stopVideo.setOnClickListener(this);
        //切换摄像头
        switchCamera = view.findViewById(R.id.video_switch_camera);
        switchCamera.setOnClickListener(this);
        //缩略图
        videoPicture = view.findViewById(R.id.video_iv);
        videoPicture.setOnClickListener(this);
        videoPicture.setImageBitmap(ImageUtils.getLatestThumbBitmap());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCameraView = getActivity().findViewById(R.id.camera_view);
        mCameraProxy = mCameraView.getCameraProxy();
        time = getActivity().findViewById(R.id.time);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video_start://开始录像
                startVideo.setVisibility(View.GONE);
                switchCamera.setVisibility(View.GONE);
                videoPicture.setVisibility(View.GONE);
                stopVideo.setVisibility(View.VISIBLE);
                //设置开始计时时间
                time.setVisibility(View.VISIBLE);
                time.setBase(SystemClock.elapsedRealtime());
                time.setFormat(" %s");
                //启动计时器
                time.start();
                //开始录像
                mCameraProxy.startVideo();
                break;
            case R.id.video_stop://停止录像
                stopVideo.setVisibility(View.GONE);
                startVideo.setVisibility(View.VISIBLE);
                switchCamera.setVisibility(View.VISIBLE);
                videoPicture.setVisibility(View.VISIBLE);
                //停止录像
                Bitmap bitmap = mCameraProxy.stopVideo(mCameraView.getWidth(), mCameraView.getHeight());
                videoPicture.setImageBitmap(bitmap);//设置缩略图
                Log.d(TAG, "onClick: "+bitmap);
                // 停止计时
                time.stop();
                time.setVisibility(View.GONE);
                break;
            case R.id.video_switch_camera:
                mCameraProxy.switchCamera(mCameraView.getWidth(),mCameraView.getHeight());
                break;
            case R.id.video_iv://缩略图、进入视屏库
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                startActivity(intent);

        }
    }


}