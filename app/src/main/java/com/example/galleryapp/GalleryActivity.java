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


    }




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











