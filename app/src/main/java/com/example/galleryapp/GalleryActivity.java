package com.example.galleryapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.galleryapp.databinding.ActivityGalleryBinding;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.example.galleryapp.model.Item;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    // Create Binding...
    ActivityGalleryBinding b;
    List<Item> itemList = new ArrayList<>();
    int selectedPosition;
    List<Item> removeItem;
    private boolean isEdited;
    private boolean isAdd;
    int noOfImages = 0;
    // Shared preferences
    SharedPreferences preferences;
    private boolean isDialogBoxShowed;
    private ImageView imageview;
    private static final String IMAGE_DIRECTORY = "/demonuts";
    private int GALLERY = 1, CAMERA = 2;
    private Bitmap myBitmap;
    private Intent galleryIntent;
    private final int RESULT_GALLERY = 1;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        PotraitmodeOnly();

        //Load data from sharedPreferences
        loadSharedPreferenceData();
        //  requestMultiplePermissions();
        //requestMultiplePermissions();


    }


//  //  private void showPictureDialog(){
//        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
//        pictureDialog.setTitle("Select Action");
//        String[] pictureDialogItems = {
//                "Select photo from gallery",
//                "Capture photo from camera" };
//        pictureDialog.setItems(pictureDialogItems,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        switch (which) {
//                            case 0:
//                                choosePhotoFromGallary();
//                                break;
//                            case 1:
//                                takePhotoFromCamera();
//                                break;
//                        }
//                    }
//                });
//        pictureDialog.show();
//    }
//
//    private void takePhotoFromCamera() {
//        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, CAMERA);
//    }
//
//    private void choosePhotoFromGallary() {
//        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//        startActivityForResult(galleryIntent, GALLERY);
//
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == this.RESULT_CANCELED) {
//            return;
//        }
//        if (requestCode == GALLERY) {
//            if (data != null) {
//                Uri contentURI = data.getData();
//                try {
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
//                    String path = saveImage(bitmap);
//                    Toast.makeText(GalleryActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
//                    imageview.setImageBitmap(bitmap);
//                }
//               catch (IOException e) {
//                        e.printStackTrace();
//                        Toast.makeText(GalleryActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
//
//                }
//                }
//
//    } else if (requestCode == CAMERA) {
//            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
//            imageview.setImageBitmap(thumbnail);
//            saveImage(thumbnail);
//            Toast.makeText(GalleryActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//private String saveImage(Bitmap bitmap) {
//    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//    myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
//    File wallpaperDirectory = new File(
//            Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
//    // have the object build the directory structure, if needed.
//    if (!wallpaperDirectory.exists()) {
//        wallpaperDirectory.mkdirs();
//    }
//
//    try {
//        File f = new File(wallpaperDirectory, Calendar.getInstance()
//                .getTimeInMillis() + ".jpg");
//        f.createNewFile();
//        FileOutputStream fo = new FileOutputStream(f);
//        fo.write(bytes.toByteArray());
//        MediaScannerConnection.scanFile(this,
//                new String[]{f.getPath()},
//                new String[]{"image/jpeg"}, null);
//        fo.close();
//        Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());
//
//        return f.getAbsolutePath();
//    } catch (IOException e1) {
//        e1.printStackTrace();
//    }
//    return "";
//}
//    private void requestMultiplePermissions() {
//        Glide.with(this)
//                .withPermissions(
//                        Manifest.permission.CAMERA,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        Manifest.permission.READ_EXTERNAL_STORAGE)
//                .withListener(new MultiplePermissionsListener() {
//                    @Override
//                    public void onPermissionsChecked(MultiplePermissionsReport report) {
//                        // check if all permissions are granted
//                        if (report.areAllPermissionsGranted()) {
//                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
//                        }
//
//                        // check for permanent denial of any permission
//                        if (report.isAnyPermissionPermanentlyDenied()) {
//                            // show alert dialog navigating to Settings
//                            //openSettingsDialog();
//                        }
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                        token.continuePermissionRequest();
//                    }
//                }).
//                withErrorListener(new PermissionRequestErrorListener() {
//                    @Override
//                    public void onError(Glide error) {
//                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .onSameThread()
//                .check();
//    }
//
//
//

    // Load Shared Prefrence...
    private void loadSharedPreferenceData() {
        String items = getPreferences(MODE_PRIVATE).getString("ITEMS", null);
        if (items == null || items.equals("[]")) {
            return;
        }
        b.Heading.setVisibility(View.GONE);
        Log.d("Now", "loadSharedPreferenceData: " + items);
        Gson gson = new Gson();
        Type type = new TypeToken<List<Item>>() {
        }.getType();

        itemList = gson.fromJson(items, type);

        //Fetch data from caches
        for (Item item : itemList) {
            ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

            Glide.with(this)
                    .asBitmap()
                    .onlyRetrieveFromCache(true)
                    .load(item.url)
                    .into(binding.imageview);

            binding.Title.setBackgroundColor(item.color);
            binding.Title.setText(item.label);

            Log.d("Now", "onResourceReady: " + item.label);

            b.list.addView(binding.getRoot());


        }

        noOfImages = itemList.size();

    }

/// Menu Option..

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addImage) {
            showAddImageDialog();
            return true;
        } else if(item.getItemId() == R.id.addGallery){
            showAddgalleryDialog();
        }

       return false;
    }

    private void showAddgalleryDialog() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),1 );


    }


    private void PotraitmodeOnly() {
        if (this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            isDialogBoxShowed = true;

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    // Dialog Option...
    private void showAddImageDialog() {

        new AddImageDialog()
                .showDialog(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdd(Item item) {
                        inflateViewForItem(item);
                    }

                    @Override
                    public void onError(String error) {
                        new MaterialAlertDialogBuilder(GalleryActivity.this)
                                .setTitle("Error")
                                .setMessage(error)
                                .show();

                    }
                });
    }


    private void inflateViewForItem(Item item) {

        if (noOfImages == 0) {
            b.Heading.setVisibility(View.GONE);
        }
        //Inflate layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

        //Bind data
        binding.imageview.setImageBitmap(item.image);
        binding.Title.setBackgroundColor(item.color);
        binding.Title.setText(item.label);


        b.list.addView(binding.getRoot());

        //Add Item
        Item newItem = new Item(item.color, item.label, item.url);

        if (itemList == null) {
            itemList = new ArrayList<Item>();
        }

        itemList.add(newItem);
        isAdd = true;
        noOfImages++;
    }

//  //  private void getDataFromSharedPreferences() {
//        int itemCount=preferences.getInt(Constants.NO_OF_IMAGES,0);
//        if(itemCount!=0){
//            b.itemsList.setVisibility(View.GONE);
//        }
//        for (int i=0;i<itemCount;i++){
//            Item item=new Item(preferences.getString(Constants.IMAGE+i,"")
//                    ,preferences.getInt(Constants.COLOR+i,0)
//                    ,preferences.getString(Constants.LABEL+i,""));
//
//            items.add(item);


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData()!=null){
                Uri imageUri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
                String uri = imageUri.toString();

                new AddFromGalleryDialog().show(this, uri, new AddFromGalleryDialog.OnCompleteListener() {
                    @Override
                    public void onAddCompleted(Item item) {
                          itemList.add(item);
                            inflateViewForItem(item);
                            b.Heading.setVisibility(View.GONE);

                        }

                    @Override
                    public void onError(String error) {
                        new MaterialAlertDialogBuilder(GalleryActivity.this)
                                .setTitle("Error")
                                .show();


                    }


                });

                //Do whatever that you desire here. or leave this blank

//                ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
//                Glide.with(this)
//                        .load(imageUri)
//                        .into(binding.imageview);
//                b.list.addView(binding.getRoot());


            }
        }
    @Override
    protected void onPause() {
        super.onPause();

        //Remove Item and save
        if (removeItem != null) {
            itemList.removeAll(removeItem);

            Gson gson = new Gson();
            String json = gson.toJson(itemList);

            getPreferences(MODE_PRIVATE).edit().putString("ITEMS", json).apply();

            finish();
        }

        //save in SharedPreference
        if (isEdited || isAdd) {
            Gson gson = new Gson();
            String json = gson.toJson(itemList);
            getPreferences(MODE_PRIVATE).edit().putString("ITEMS", json).apply();
            isAdd = false;
            isEdited = false;
        }

    }

}











