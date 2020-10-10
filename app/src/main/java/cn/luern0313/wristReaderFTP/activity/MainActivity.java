package cn.luern0313.wristReaderFTP.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cn.luern0313.wristReaderFTP.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.opengl.ETC1;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private Context ctx;
    private Handler handler = new Handler();
    private Runnable runnableLoadingFinish;

    private final int RESULT_SCAN = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;

        runnableLoadingFinish = new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.main_loading).setVisibility(View.GONE);
            }
        };

        findViewById(R.id.main_scan).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ctx, ScanActivity.class);
                startActivityForResult(intent, RESULT_SCAN);
            }
        });

        findViewById(R.id.main_connect).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String ip = ((EditText) findViewById(R.id.main_edit_ip)).getText().toString();
                String port = ((EditText) findViewById(R.id.main_edit_port)).getText().toString();
                String name = ((EditText) findViewById(R.id.main_edit_user)).getText().toString();
                if(port.equals("")) port = "2221";
                if(name.equals("")) name = "user";
                if(!ip.equals(""))
                    connect(ip, port, name);
                else
                    Toast.makeText(ctx, "ip地址必须输入！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void connect(final String ip, final String port, final String name)
    {
        findViewById(R.id.main_loading).setVisibility(View.VISIBLE);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    FTPClient ftpClient = new FTPClient();
                    ftpClient.setControlEncoding("UTF-8");
                    ftpClient.connect(ip, port != null ? Integer.parseInt(port) : 2221);
                    if(ftpClient.login(name, ""))
                    {
                        handler.post(runnableLoadingFinish);
                        Intent intent = new Intent(ctx, TransferActivity.class);
                        intent.putExtra("ip", ip);
                        intent.putExtra("port", port);
                        intent.putExtra("name", name);
                        startActivity(intent);
                    }
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "连接失败！请检查两个设备是否在同一网络下，或稍后再试", Toast.LENGTH_LONG).show();
                        handler.post(runnableLoadingFinish);
                        Looper.loop();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "连接失败！请检查两个设备是否在同一网络下，或稍后再试", Toast.LENGTH_LONG).show();
                    handler.post(runnableLoadingFinish);
                    Looper.loop();
                }
                catch (Exception e)
                {
                    e.printStackTrace();Looper.prepare();
                    Toast.makeText(ctx, "连接失败！", Toast.LENGTH_LONG).show();
                    handler.post(runnableLoadingFinish);
                    Looper.loop();
                }
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED) return;
        if(requestCode == RESULT_SCAN)
        {
            String text = data.getStringExtra("text");
            Log.w("ftp", "text:" + text);
            Uri uri = Uri.parse(text);
            String ip = uri.getQueryParameter("ftp_ip");
            String port = uri.getQueryParameter("ftp_port");
            String name = uri.getQueryParameter("ftp_name");
            connect(ip, port, name);
        }
    }
}
