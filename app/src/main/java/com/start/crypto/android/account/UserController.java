package com.start.crypto.android.account;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxView;
import com.start.crypto.android.BaseController;
import com.start.crypto.android.R;
import com.start.crypto.android.api.MainApiService;
import com.start.crypto.android.api.MainServiceGenerator;
import com.start.crypto.android.api.model.User;
import com.start.crypto.android.imageLoader.GlideApp;

import java.io.File;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UserController extends BaseController {

    private static final int RESULT_LOAD_IMG = 1;

    private static final int AVATAR_IMAGE_WIDTH = 240;
    private static final int AVATAR_IMAGE_HEIGHT = 240;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;


    @BindView(R.id.avatar)                  ImageView mAvatarView;
    @BindView(R.id.avatar_upload_process)   ImageView mAvatarProcessView;
    @BindView(R.id.first_name)              EditText mFirstNameView;
    @BindView(R.id.last_name)               EditText mLastNameView;
    @BindView(R.id.next)                    Button mNextButton;
    @BindView(R.id.logout)                  Button mLogoutButton;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private User mUser;

    private Uri mSelectedImage;
    private File mAvatarFile;

    public UserController() {
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.account_user_controller, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        startProgressDialog();
        compositeDisposable.add(
                MainServiceGenerator.createService(MainApiService.class, getActivity()).userInfo()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                user -> {
                                    mUser = user;
                                    bindUser();
                                    stopProgressDialog();
                                },
                                error -> {
                                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                    stopProgressDialog();
                                }
                        )
        );

        RxView.clicks(mNextButton).subscribe(v -> {
            upload();
            showAlert(R.string.account_was_updated);
            getActivity().finish();
        });

        RxView.clicks(mLogoutButton).subscribe(v -> {
            Intent intent = new Intent();
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        });


        RxView.clicks(mAvatarView).subscribe(v -> {
            requestGalleryPermission();
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK && data != null) {

                if(!isExternalStorageReadable()) {
                    throw new IllegalStateException("permission READ_EXTERNAL_STORAGE denied");
                }
                // Get the Image from data

                mSelectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getActivity().getContentResolver().query(mSelectedImage, filePathColumn, null, null, null);

                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                // Set the Image in ImageView after decoding the String
                mAvatarFile = new File(imgDecodableString);
                Uri imageUri = Uri.fromFile(mAvatarFile);

                //mAvatarView.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
                GlideApp.with(getActivity())
                        .load(imageUri)
                        .centerCrop()
                        .override(AVATAR_IMAGE_WIDTH, AVATAR_IMAGE_HEIGHT)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mAvatarView);



            } else {
                Toast.makeText(getActivity(), "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateAvatar();
            } else {
                showAlert(R.string.account_gallery_denied);
            }
        }
    }

    private void upload() {

        mAvatarProcessView.setVisibility(View.GONE);

        MultipartBody.Part body = null;
        if(mSelectedImage != null) {
            // create RequestBody instance from file
            RequestBody requestFile = RequestBody.create(MediaType.parse(getActivity().getContentResolver().getType(mSelectedImage)), mAvatarFile);

            // MultipartBody.Part is used to send also the actual file name
            body = MultipartBody.Part.createFormData("avatar", mAvatarFile.getName(), requestFile);
        }

        // add another part within the multipart request
        String firstNameString = mFirstNameView.getText().toString().trim();
        RequestBody firstName = RequestBody.create(MultipartBody.FORM, firstNameString);
        String lastNameString = mLastNameView.getText().toString().trim();
        RequestBody lastName = RequestBody.create(MultipartBody.FORM, lastNameString);

        compositeDisposable.add(
                    MainServiceGenerator.createService(MainApiService.class, getActivity()).avatarUpload(firstName, lastName, body)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    user -> {
                                        mAvatarProcessView.setVisibility(View.GONE);
                                    },
                                    error -> {
                                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                        mAvatarProcessView.setVisibility(View.GONE);
                                    }
                            )
        );
    }

    private void updateAvatar() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    private void bindUser() {
        if(mUser.getAvatar() != null) {
            GlideApp.with(getActivity())
                    .load(mUser.getAvatar())
                    .centerCrop()
                    .override(AVATAR_IMAGE_WIDTH, AVATAR_IMAGE_HEIGHT)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mAvatarView);
        }
        mFirstNameView.setText(mUser.getFirstName());
        mLastNameView.setText(mUser.getLastName());
        mNextButton.setEnabled(true);
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showAlert(R.string.account_gallery_explanation);
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                updateAvatar();
            }
        }
    }




}
