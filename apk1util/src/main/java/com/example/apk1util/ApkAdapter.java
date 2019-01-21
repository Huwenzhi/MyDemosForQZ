package com.example.apk1util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class ApkAdapter extends BaseQuickAdapter<Apk1Info, BaseViewHolder> {
    public ApkAdapter(int layoutResId, @Nullable List<Apk1Info> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Apk1Info item) {
        helper
                .setText(R.id.apk_name, item.getFileName())
                .setText(R.id.apk_size, "版本号:"+item.getVersionName())
                .setImageDrawable(R.id.apk_logo, item.getIcon())
        ;
    }


}
