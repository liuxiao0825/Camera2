package com.example.camera2.fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.camera2.MainActivity;
import com.example.camera2.R;
import com.example.camera2.camera.Camera2Proxy;
import com.example.camera2.surfaceview.Camera2SurfaceView;
import com.example.camera2.utils.ImageUtils;

import java.nio.ByteBuffer;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TakePictureFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "TakePictureFragment";
    private View view;
    private Camera2Proxy mCameraProxy;
    private Camera2SurfaceView mCameraView;
    private ImageView switchCamera;//切换摄像头
    private ImageView takePicture;//拍照
    private ImageView picture;//相册、缩略图

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_take_picture, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //切换摄像头
        switchCamera = view.findViewById(R.id.switch_camera);
        switchCamera.setOnClickListener(this);
        //拍照
        takePicture = view.findViewById(R.id.take_picture_iv);
        takePicture.setOnClickListener(this);
        //相册、缩略图
        picture = view.findViewById(R.id.picture_iv);
        picture.setOnClickListener(this);
        picture.setImageBitmap(ImageUtils.getLatestThumbBitmap());//设置缩略图
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
            case R.id.switch_camera://切换摄像头
                mCameraProxy.switchCamera(mCameraView.getWidth(),mCameraView.getHeight());
                break;
            case R.id.take_picture_iv://拍照
                mCameraProxy.capturePicture();
                mCameraProxy.setImageAvailableListener(mOnImageAvailableListener);//图片监听
                break;
            case R.id.picture_iv://相册
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivity(intent);
                break;
        }
    }

    /**
     * 回调界面，用于通知新图像可用
     *可以在这里处理拍照得到的临时照片 例如，写入本地
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "onImageAvailable: ");
            new ImageSaveTask().execute(reader.acquireNextImage()); // 保存图片
        }
    };
    /**
     * 图片保存
     */
    private class ImageSaveTask extends AsyncTask<Image, Void, Bitmap> {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected Bitmap doInBackground(Image ... images) {
            ByteBuffer buffer = images[0].getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            if (mCameraProxy.isFrontCamera()) { //前置摄像头
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                // 前置摄像头需要左右镜像
                Bitmap rotateBitmap = ImageUtils.rotateBitmap(bitmap, 0, true, true);
                ImageUtils.saveBitmap(rotateBitmap);
                rotateBitmap.recycle();
            } else {
                ImageUtils.saveImage(bytes);
            }
            images[0].close();
            return ImageUtils.getLatestThumbBitmap();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG, "onPostExecute: "+bitmap);
            picture.setImageBitmap(bitmap);
        }
    }

}