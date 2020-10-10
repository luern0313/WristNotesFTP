package cn.luern0313.wristReaderFTP.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.luern0313.wristReaderFTP.R;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Toast;

public class ScanActivity extends AppCompatActivity implements QRCodeView.Delegate
{
    private Context ctx;
    private QRCodeView mQRCodeView;
    private Intent resultIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ctx = this;
        resultIntent = new Intent();
        setResult(RESULT_CANCELED, resultIntent);

        mQRCodeView = findViewById(R.id.scan_scan);
        mQRCodeView.setDelegate(this);
    }

    @Override
    public void onScanQRCodeSuccess(String result)
    {
        vibrate();
        resultIntent.putExtra("text", result);
        setResult(RESULT_OK, resultIntent);
        finish();
        mQRCodeView.startSpot();
    }

    @Override
    public void onScanQRCodeOpenCameraError()
    {
        String[] PERMISSIONS_STORAGE = {Manifest.permission.CAMERA};
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 0);
        else
            Toast.makeText(ctx, "打开摄像头失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                mQRCodeView.startCamera();
                mQRCodeView.showScanRect();
                mQRCodeView.startSpot();
            }
            else
                Toast.makeText(ctx, "无相机权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mQRCodeView.startCamera();
        mQRCodeView.showScanRect();
        mQRCodeView.startSpot();
        //mQRCodeView.openFlashlight();//开灯
        //mQRCodeView.closeFlashlight();//关灯
    }

    @Override
    protected void onStop()
    {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    private void vibrate()
    {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }
}