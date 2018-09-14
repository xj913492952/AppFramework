package example.album;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.dmcbig.mediapicker.PickerActivity;
import com.dmcbig.mediapicker.PickerConfig;
import com.dmcbig.mediapicker.entity.Media;
import com.style.app.ConfigUtil;
import com.style.app.Skip;
import com.style.base.BaseTitleBarActivity;
import com.style.dialog.SelAvatarDialog;
import com.style.framework.R;
import com.style.framework.databinding.ActivitySelectLocalPictureBinding;
import com.style.utils.DeviceInfoUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;

import example.viewpager.ImageScanActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by xiajun on 2016/10/8.
 */
public class SelectLocalPictureActivity extends BaseTitleBarActivity {

    ActivitySelectLocalPictureBinding bd;

    private Media TAG_ADD;

    private DynamicPublishImageAdapter adapter;
    private ArrayList<Media> paths;
    protected File photoFile;
    private SelAvatarDialog dialog;

    private boolean haveImg;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_select_local_picture;
    }


    @Override
    public void initData() {
        bd = getBinding();
        setToolbarTitle("本地图片选择");
        paths = new ArrayList<>();
        TAG_ADD = new Media();
        TAG_ADD.name = "addTag";
        paths.add(TAG_ADD);
        adapter = new DynamicPublishImageAdapter(getContext(), paths);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        bd.recyclerView.setLayoutManager(gridLayoutManager);
        bd.recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((position, data) -> {
            int count = adapter.getItemCount();
            if (position == count - 1) {
                showSelPicPopupWindow();
            } else {
                ArrayList<Media> cacheList = new ArrayList<>();
                if (count > 1) {
                    for (int i = 0; i < count - 1; i++) {
                        cacheList.add(paths.get(i));
                    }
                }
                Intent intent = new Intent(SelectLocalPictureActivity.this, ImageScanActivity.class);
                intent.putExtra("list", cacheList); // (Optional)
                startActivityForResult(intent, PickerConfig.CODE_TAKE_ALBUM);
            }
        });

        adapter.setOnDeleteClickListener(position -> {
            paths.remove(position);
            adapter.notifyDataSetChanged();
            setHaveDynamic();
        });


    }
    public void selAvatar(View v) {
        showSelPicPopupWindow();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK || resultCode == PickerConfig.RESULT_CODE) {
            switch (requestCode) {
                case PickerConfig.CODE_TAKE_ALBUM:
                    if (data != null) {
                        ArrayList<Media> newPaths = data.getParcelableArrayListExtra(PickerConfig.EXTRA_RESULT);
                        paths.clear();
                        paths.addAll(newPaths);
                        paths.add(TAG_ADD);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case Skip.CODE_TAKE_CAMERA:
                    if (photoFile.exists()) {
                        DeviceInfoUtil.notifyUpdateGallary(this, photoFile);// 通知系统更新相册
                        String filePath = photoFile.getAbsolutePath();// 获取相片的保存路径
                        int size = paths.size();
                        if (size >= 10) {
                            showToast("最多上传9张图片");
                        } else {
                            int location = 0;
                            if (size >= 1)
                                location = size - 1;
                            Media media = new Media();
                            media.path = filePath;
                            media.size = photoFile.length();
                            paths.add(location, media);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        showToast(R.string.File_does_not_exist);
                    }
                    break;
            }
            setHaveDynamic();
        }
    }

    private void setHaveDynamic() {
        int number = adapter.getItemCount();
        if (number > 1)
            haveImg = true;
        else
            haveImg = false;
    }

    private void showSelPicPopupWindow() {
        if (dialog == null) {
            dialog = new SelAvatarDialog(getContext());
            dialog.setOnItemClickListener(new SelAvatarDialog.OnItemClickListener() {
                @Override
                public void OnClickCamera() {
                    initPermission();
                }

                @Override
                public void OnClickPhoto() {
                    selectPhotos();
                }

                @Override
                public void OnClickCancel() {

                }
            });
        }
        dialog.show();
    }

    private void selectPhotos() {
        int newCount = adapter.getItemCount();
        ArrayList<Media> cacheList = new ArrayList<>();
        if (newCount > 1) {
            for (int i = 0; i < newCount - 1; i++) {
                cacheList.add(paths.get(i));
            }
        }

        Intent intent = new Intent(SelectLocalPictureActivity.this, PickerActivity.class);
        intent.putExtra(PickerConfig.SELECT_MODE, PickerConfig.PICKER_IMAGE);//default image and video (Optional)
        long maxSize = 188743680L;//long long long
        intent.putExtra(PickerConfig.MAX_SELECT_SIZE, maxSize); //default 180MB (Optional)
        intent.putExtra(PickerConfig.MAX_SELECT_COUNT, 9);  //default 40 (Optional)
        intent.putExtra(PickerConfig.DEFAULT_SELECTED_LIST, cacheList); // (Optional)
        SelectLocalPictureActivity.this.startActivityForResult(intent, PickerConfig.CODE_TAKE_ALBUM);
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RxPermissions rxPermissions = new RxPermissions(this);
            rxPermissions.request(Manifest.permission.CAMERA)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(grated -> {
                        if (grated) {
                            takePhoto();
                        } else {
                            //onError("请开启相机权限");
                        }
                    }, throwable -> {
                        throwable.printStackTrace();
                    });
        } else {
            takePhoto();
        }
    }

    private void takePhoto() {
        photoFile = Skip.takePhoto(SelectLocalPictureActivity.this, ConfigUtil.DIR_APP_IMAGE_CAMERA, String.valueOf(System.currentTimeMillis()) + ".jpg");

    }
}