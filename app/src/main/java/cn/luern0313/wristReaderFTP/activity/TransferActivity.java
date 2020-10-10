package cn.luern0313.wristReaderFTP.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import cn.luern0313.wristReaderFTP.R;
import cn.luern0313.wristReaderFTP.adapter.TransferAdapter;
import cn.luern0313.wristReaderFTP.model.TransferModel;
import cn.luern0313.wristReaderFTP.util.DataProcessUtil;
import cn.luern0313.wristReaderFTP.util.FTPUtil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TransferActivity extends AppCompatActivity
{
    private Context ctx;
    private Intent intent;
    private String ip, port, name;
    private FTPClient ftpClient;

    private ListView uiListView;
    private ArrayList<TransferModel> transferModelArrayList;
    private TransferAdapter transferAdapter;
    private TransferAdapter.TransferListener transferListener;
    private int progress;
    private int success;

    private Handler handler = new Handler();
    private Runnable runnableProgress, runnableFinish;

    private FilePickerDialog dialog;

    String[] PERMISSIONS_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        ctx = this;
        intent = getIntent();
        ip = intent.getStringExtra("ip");
        port = intent.getStringExtra("port");
        name = intent.getStringExtra("name");

        transferListener = new TransferAdapter.TransferListener()
        {
            @Override
            public void onClick(int viewId, int position)
            {
                onViewClick(viewId, position);
            }
        };

        uiListView = findViewById(R.id.transfer_list);
        transferModelArrayList = new ArrayList<>();
        transferAdapter = new TransferAdapter(getLayoutInflater(), transferModelArrayList,
                                              transferListener);

        uiListView.setEmptyView(findViewById(R.id.transfer_empty));
        uiListView.setAdapter(transferAdapter);

        PERMISSIONS_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        final DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.MULTI_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = Environment.getExternalStorageDirectory();
        properties.error_dir = Environment.getExternalStorageDirectory();
        properties.offset = Environment.getExternalStorageDirectory();
        properties.extensions = null;
        dialog = new FilePickerDialog(ctx, properties);
        dialog.setNegativeBtnName("取消");
        dialog.setPositiveBtnName("选择");
        dialog.setTitle("选择文件");

        dialog.setDialogSelectionListener(new DialogSelectionListener()
        {
            @Override
            public void onSelectedFilePaths(String[] files)
            {
                for (String path : files)
                {
                    String[] paths = path.split("/");
                    transferModelArrayList.add(new TransferModel(paths[paths.length - 1], path, new File(path)));
                    transferAdapter.notifyDataSetChanged();
                }
            }
        });

        runnableProgress = new Runnable()
        {
            @Override
            public void run()
            {
                ((ProgressBar) findViewById(R.id.transfer_progress)).setProgress(progress);
            }
        };

        runnableFinish = new Runnable()
        {
            @Override
            public void run()
            {
                ((ProgressBar) findViewById(R.id.transfer_progress)).setProgress(0);
                findViewById(R.id.transfer_loading).setVisibility(View.GONE);
                new AlertDialog.Builder(ctx)
                    .setTitle(getString(R.string.transfer_finish_title))
                    .setMessage(String.format(getString(R.string.transfer_finish_message), transferModelArrayList.size(),
                                              success, transferModelArrayList.size() - success))
                    .setPositiveButton("确定", null).show();
                transferModelArrayList.clear();
                transferAdapter.notifyDataSetChanged();
            }
        };

        findViewById(R.id.transfer_title_select).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(DataProcessUtil.checkSelfPermission(PERMISSIONS_STORAGE))
                    ActivityCompat.requestPermissions((Activity) ctx, PERMISSIONS_STORAGE, 0);
                else
                    dialog.show();
            }
        });

        findViewById(R.id.transfer_empty_add).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(DataProcessUtil.checkSelfPermission(PERMISSIONS_STORAGE))
                    ActivityCompat.requestPermissions((Activity) ctx, PERMISSIONS_STORAGE, 0);
                else
                    dialog.show();
            }
        });

        findViewById(R.id.transfer_title_transfer).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(transferModelArrayList.size() != 0)
                {
                    ((ProgressBar) findViewById(R.id.transfer_progress)).setMax(transferModelArrayList.size());
                    ((ProgressBar) findViewById(R.id.transfer_progress)).setProgress(0);
                    findViewById(R.id.transfer_loading).setVisibility(View.VISIBLE);
                    progress = 0;
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            success = 0;
                            for (int i = 0; i < transferModelArrayList.size(); i++)
                            {
                                String result = FTPUtil.uploadFiles(ftpClient, transferModelArrayList.get(i).getFileFile());
                                transferModelArrayList.get(i).setFileSuccess(result);
                                if(result.equals("")) success++;
                                progress++;
                                handler.post(runnableProgress);
                            }
                            handler.post(runnableFinish);
                        }
                    }).start();
                }
                else
                    new AlertDialog.Builder(ctx)
                            .setMessage("你还未选择任何文件！")
                            .setPositiveButton("确定", null).show();
            }
        });

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ftpClient = new FTPClient();
                    ftpClient.setControlEncoding("UTF-8");
                    ftpClient.connect(ip, port != null ? Integer.parseInt(port) : 2221);
                    if(ftpClient.login(name, ""))
                    {
                        ftpClient.makeDirectory("FTP接收文件");
                        Log.w("ftp", ftpClient.printWorkingDirectory());
                        boolean a = ftpClient.changeWorkingDirectory("FTP接收文件");
                        Log.w("ftp", "a:" + a);
                    }
                    else
                    {
                        Looper.prepare();
                        Toast.makeText(ctx, "连接失败！请检查两个设备是否在同一网络下，或稍后再试", Toast.LENGTH_LONG).show();
                        finish();
                        Looper.loop();
                        Log.w("ftp", "login fail");
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(ctx, "连接失败！请检查两个设备是否在同一网络下，或稍后再试", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        }).start();
    }

    private void onViewClick(int viewId, int position)
    {
        if(viewId == R.id.item_transfer_delete)
        {
            transferModelArrayList.remove(position);
            transferAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0)
        {
            if(grantResults.length <= 0 || grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(ctx, "获取储存权限失败！", Toast.LENGTH_SHORT).show();
            else
                dialog.show();
        }
    }
}