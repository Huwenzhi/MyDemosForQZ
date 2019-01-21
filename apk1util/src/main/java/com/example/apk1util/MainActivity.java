package com.example.apk1util;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zhy.base.fileprovider.FileProvider7;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView apkNo;
    RecyclerView recycleview;
    ApkAdapter apkAdapter;
    SwipeRefreshLayout my_swf;
    ProgressDialog dialog;
    ImageView about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //授权
        getPermisson();
        initEvent();


    }

    private void initEvent() {
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
        });
        apkAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                checkIsAndroidO(position);

            }
        });

        my_swf.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                apkAdapter.setNewData(null);
                getPermisson();
            }
        });
    }

    int pos = -1;

    private void checkIsAndroidO(int position) {
        pos = position;
        if (Build.VERSION.SDK_INT >= 26) {
            boolean b = getPackageManager().canRequestPackageInstalls();
            if (b) {
                //安装
                install();
            } else {
                new AlertDialog.Builder(this).setTitle("授权通知").setMessage("请找到" + getString(R.string.app_name)
                        + "点击开启“允许安装应用”，如不开启应用无法安装，感谢配合！")
                        .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //请求安装未知应用来源的权限
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, 102);
                    }
                }).setNegativeButton("算了",null).show();
            }
        } else {
            //安装
            install();
        }


    }

    private void initView() {
        about = findViewById(R.id.about);
        dialog = ProgressDialog.show(MainActivity.this, "", "加载中……");
        dialog.dismiss();
        apkNo = findViewById(R.id.apk_no);
        recycleview = findViewById(R.id.my_recycle);
        my_swf = findViewById(R.id.my_swf);
        recycleview.setLayoutManager(new LinearLayoutManager(this));
        apkAdapter = new ApkAdapter(R.layout.item_apk, null);
        recycleview.setAdapter(apkAdapter);
    }

    //获取权限
    private void getPermisson() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            //获取apk的列表
            getListApk1();
        }


    }

    List<Apk1Info> list;

    private void getListApk1() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    if (!dialog.isShowing()) {
                        dialog.show();
                    }
                    ;
                }
            }
        });
        String prentFile = getSDCardPath();
        File ak1File = new File(prentFile);
        // 先判断这个文件是否存在
        if (ak1File.exists()) {
            list = new ArrayList<Apk1Info>();
            GetFilePath(list, ak1File);//开始扫描此文件夹下想要的文件
            apkNo.setText("发现了" + list.size() + "个Apk.1文件");
            apkAdapter.setNewData(list);

        } else {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_LONG).show();
        }
        my_swf.setRefreshing(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "加载完成", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });


    }

    /**
     * 权限申请返回结果
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 申请结果数组，里面都是int类型的数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //同意权限申请
                    getListApk1();
                } else { //拒绝权限申请
                    Toast.makeText(this, "权限被拒绝了", Toast.LENGTH_SHORT).show();
                }
                break;

            case 102:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //安装
                    install();
                } else {
                    //  引导用户手动开启安装权限
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    startActivityForResult(intent, 103);
                }
                break;
            default:
                break;
        }
    }

    private void install() {
        if (pos != -1) {
            Apk1Info apk1Info = list.get(pos);
            String newPath = Environment.getExternalStorageDirectory() + File.separator + "ic_launcher";
            File appDir = new File(newPath);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            String fileName = apk1Info.getName() + "-" + apk1Info.getVersionName() + "-" + apk1Info.getVersionCode() + ".apk";
            if (!fileIsExists(newPath + File.separator + fileName)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null) {
                            if (!dialog.isShowing()) {
                                dialog.show();
                            }
                            ;
                        }
                    }
                });
                copyFile(apk1Info.getPath(), newPath + File.separator + fileName);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            ;
                        }
                    }
                });
            }
            File file = new File(newPath + File.separator + fileName);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // 仅需改变这一行
            FileProvider7.setIntentDataAndType(this,
                    intent, "application/vnd.android.package-archive", file, true);
            startActivity(intent);
        }
    }

    //判断文件是否存在
    public boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public void copyFile(String oldPath, String newPath) {
        if (dialog != null) {
            dialog.show(this, "", "加载中……");
        }
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        } catch (Exception e) {
            if (dialog != null) {
                dialog.dismiss();
            }
            System.out.println("复制单个文件操作出错");
            Toast.makeText(this, "出错了，请联系87683202@qq.com", Toast.LENGTH_SHORT).show();
            e.printStackTrace();


        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 103) {


        }
    }

    //判断sd卡是否存在并返回根目录
    private String getSDCardPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取根目录
        }
        return sdDir.toString();
    }

    private void GetFilePath(final List<Apk1Info> list, File file) {
        file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {

                String name = file.getName();
                int i = name.indexOf('.');
                if (i != -1) {
                    name = name.substring(i);
                    if (name.endsWith(".apk.1")) {
                        //得到文件路径
                        String file_path = file.getAbsolutePath();
                        //得到文件名称
                        String file_name = file.getName();
                        PackageManager pm = getPackageManager();
                        PackageInfo pkgInfo = pm.getPackageArchiveInfo(file_path, PackageManager.GET_ACTIVITIES);
                        Apk1Info info = new Apk1Info();
                        info.setPath(file_path);

                        if (pkgInfo != null) {
                            ApplicationInfo appInfo = pkgInfo.applicationInfo;
                            /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
                            appInfo.sourceDir = file_path;
                            appInfo.publicSourceDir = file_path;
                            info.setFileName(file_name);
                            String appName = pm.getApplicationLabel(appInfo).toString();// 得到应用名
                            info.setName(appName);
                            String packageName = appInfo.packageName; // 得到包名
                            info.setPackageName(packageName);
                            String version = pkgInfo.versionName; // 得到版本信息
                            info.setVersionName(version);
                            int versionCode = pkgInfo.versionCode;
                            info.setVersionCode(versionCode);
                            /* icon1和icon2其实是一样的 */
                            Drawable icon1 = pm.getApplicationIcon(appInfo);// 得到图标信息
                            info.setIcon(icon1);
//                            Drawable icon2 = appInfo.loadIcon(pm);
                        }
                        list.add(info);
                        return true;
                    }
                } else if (file.isDirectory()) {//如果此文件夹存在子目录
                    //继续递归搜索子目录，如果注释，则只搜索当前目录
                    GetFilePath(list, file);
                }
                return false;
            }
        });
    }

    /**
     * 获取apk包的信息：版本号，名称，图标等
     *
     * @param absPath apk包的绝对路径
     * @param context
     */
    public void apkInfo(String absPath, Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(absPath, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
            appInfo.sourceDir = absPath;
            appInfo.publicSourceDir = absPath;
            String appName = pm.getApplicationLabel(appInfo).toString();// 得到应用名
            String packageName = appInfo.packageName; // 得到包名
            String version = pkgInfo.versionName; // 得到版本信息
            /* icon1和icon2其实是一样的 */
            Drawable icon1 = pm.getApplicationIcon(appInfo);// 得到图标信息
            Drawable icon2 = appInfo.loadIcon(pm);
            String pkgInfoStr = String.format("PackageName:%s, Vesion: %s, AppName: %s", packageName, version, appName);

        }
    }


}
