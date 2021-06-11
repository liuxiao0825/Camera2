package com.example.camera2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.camera2.camera.Camera2Proxy;
import com.example.camera2.fragment.PictureSettingFragment;
import com.example.camera2.fragment.TakePictureFragment;
import com.example.camera2.fragment.VideoFragment;
import com.example.camera2.fragment.VideoSettingFragment;
import com.example.camera2.surfaceview.Camera2SurfaceView;
import com.example.camera2.utils.ImageUtils;

import java.nio.ByteBuffer;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Camera2SurfaceView mCameraView;
    public Camera2Proxy mCameraProxy;
    private TextView video;//录像选项
    private TextView pictures;//拍照选项
    private ImageView switchCamera;//切换摄像头
    private ImageView takePicture;//拍照
    private ImageView picture;//相册、缩略图
    private Chronometer time;//计时器
    private ImageView startVideo;//开始录像
    private ImageView stopVideo;//停止录像
    private ImageView video_FlashOff;//打开录像闪光灯
    private ImageView video_FlashOn;//关闭录像闪光灯

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        initView();
    }

    public void initView(){
        mCameraView = findViewById(R.id.camera_view);
        mCameraProxy = mCameraView.getCameraProxy();
        //录像选项
        video = findViewById(R.id.video);
        video.setOnClickListener(this);
        //拍照选项
        pictures = findViewById(R.id.pictures);
        pictures.setOnClickListener(this);
        //切换摄像头
        switchCamera = findViewById(R.id.switch_camera);
        switchCamera.setOnClickListener(this);
        //拍照
        takePicture = findViewById(R.id.take_picture_iv);
        takePicture.setOnClickListener(this);
        //相册、缩略图
        picture = findViewById(R.id.picture_iv);
        picture.setOnClickListener(this);
        picture.setImageBitmap(ImageUtils.getLatestThumbBitmap());//设置缩略图
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video: {
                addFragment(new VideoSettingFragment());
                addFragment2(new VideoFragment());
                break;
            }
            case R.id.pictures:{
                addFragment(new PictureSettingFragment());
                addFragment2(new TakePictureFragment());
                break;
            }

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


    private void addFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.PictureSettingFragment, fragment);
        transaction.commit();
    }

    private void addFragment2(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.TakePictureFragment, fragment);
        transaction.commit();
    }

    /**
     * 回调界面，用于通知新图像可用
     *可以在这里处理拍照得到的临时照片 例如，写入本地
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onImageAvailable(ImageReader reader) {
            new ImageSaveTask().execute(reader.acquireNextImage()); // 保存图片
        }
    };
    /**
     * 图片保存
     */
    public class ImageSaveTask extends AsyncTask<Image, Void, Bitmap> {

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
            picture.setImageBitmap(bitmap);
        }

    }

    /**
     * 检查权限
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, 200);//请求授予此应用程序的权限。
                    return;
                }
            }
        }
    }

    /**
     * 请求权限的结果的回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestCode == 200) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "请在设置中打开摄像头和存储权限", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 200);
                    return;
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 200) {
            checkPermission();
        }
    }

}