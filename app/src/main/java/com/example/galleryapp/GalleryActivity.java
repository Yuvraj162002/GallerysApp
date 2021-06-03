package com.example.galleryapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryapp.databinding.ActivityGalleryBinding;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.example.galleryapp.model.Item;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    // Create Binding...
    ActivityGalleryBinding b;
    List<Item> items = new ArrayList<>();
    int selectedPosition;
    Gson gson = new Gson();
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
    ImageAdapter adapter;
    private String imageUrl;
    private Object collections;
    private boolean isSorted ;
    private String bitmap;
    CoordinatorLayout coordinatorLayout;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        coordinatorLayout = findViewById(R.id.ConstraintLayout);
       // recyclerView = findViewById(R.id.recycleview);
        setContentView(b.getRoot());
        PotraitmodeOnly();

        if (savedInstanceState != null){
            savedInstancedata(savedInstanceState);
        }
        else {
            loadSharedPreferenceData();
        }

        enableSwipeToDeleteAndUndo();



        //Load data from sharedPreferences
     //   loadSharedPreferenceData();


    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this){
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final Item item = adapter.getData().get(position);

                adapter.removeItem(item, position);

                Snackbar snackbar = Snackbar.make(coordinatorLayout,"Item was removed",Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.removeItem(item,position);
                        recyclerView.scrollToPosition(position);


                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void savedInstancedata(Bundle savedInstanceState) {
     //   b.Heading.setVisibility(View.GONE);
        String json = savedInstanceState.getString("Items",null);
        items = gson.fromJson(json,new TypeToken<List<Item>>(){}.getType());
        if (items !=null){
            SetUpRecycleView();
        }
        else {
            items = new ArrayList<>();
        }
    }


    // Load Shared Prefrence...
    private void loadSharedPreferenceData() {
      b.Heading.setVisibility(View.GONE);
        SharedPreferences preferences =  getPreferences(MODE_PRIVATE);
        String json = preferences.getString("Items",null);
        items = gson.fromJson(json,new TypeToken<List<Item>>(){}.getType());
        if (items !=null){
            SetUpRecycleView();
        }
        else {
            items = new ArrayList<>();
        }
    }
//        String items = getPreferences(MODE_PRIVATE).getString("ITEMS", null);
//        if (items!=null) {
//            SetUpRecycleView();
//        }
//        b.Heading.setVisibility(View.GONE);
//        Log.d("Now", "loadSharedPreferenceData: " + items);
//        Gson gson = new Gson();
//        Type type = new TypeToken<List<Item>>() {
//        }.getType();
//
//         // items h isme..
//       itemList = gson.fromJson(items, type);
//
//        //Fetch data from caches
//        for (Item item : itemList) {
//            ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
//
//            Glide.with(this)
//                    .asBitmap()
//                    .onlyRetrieveFromCache(true)
//                    .load(item.url)
//                    .into(binding.imageview);
//
//            binding.Title.setBackgroundColor(item.color);
//            binding.Title.setText(item.label);
//
//            Log.d("Now", "onResourceReady: " + item.label);
//
//            b.list.addView(binding.getRoot());
//
//
//        }
//
//        noOfImages = itemList.size();
//
//    }

//
@Override
public boolean onContextItemSelected(MenuItem item) {
    imageUrl = adapter.imageUrl;    //Image Url of Parent of Context Menu
    int index = adapter.index;      //Index of item for context menu
    ItemCardBinding binding = adapter.itemCardBinding;      //Binding of parent of context menu
    if (item.getItemId() == R.id.editMenuItem) {
        new EditImageDialog()
                .show(this, imageUrl, new EditImageDialog.onCompleteListener() {
                    @Override
                    public void onEditCompleted(Item item) {
//                            int index = b.list.indexOfChild(bindingToRemove.getRoot()) - 1;
                        items.set(index, item);
                        //Inflate Layout
                        adapter.notifyDataSetChanged();
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
    if (item.getItemId() == R.id.shareImage) {
        try {
            shareImage(binding);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    return true;
}

    private void shareImage(ItemCardBinding binding) throws FileNotFoundException {
        String bitmapPath =MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,"palatee", "share palatee");
        Uri bitmapUri = Uri.parse(bitmapPath);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri );
        startActivity(Intent.createChooser(intent,"Share"));
    }


    private void SetUpRecycleView() {
        adapter = new ImageAdapter(this, items);
        b.list.setLayoutManager(new LinearLayoutManager(this));
        b.list.setAdapter(adapter);
    }

/// Menu Option..

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery,menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
             /// ye dehkna h...
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addImage) {
            showAddImageDialog();
            return true;
        }
        if (item.getItemId() == R.id.addGallery) {
            showAddgalleryDialog();
        }
        if (item.getItemId() == R.id.search){
           SortData();
           return true;
        }

        return false;
    }



    public void SortData() {
        if (!isSorted) {
            isSorted = true;
            List<Item> sortedItem = new ArrayList<>(items);
            Collections.sort(sortedItem, (p1, p2) -> p1.label.compareTo(p2.label));
            if (adapter != null) {
                adapter.labelItem = sortedItem;
                adapter.showSortedItems();
                b.list.setAdapter(adapter);
            }
        }else {
                isSorted = false;
            }
        }



    private void showAddgalleryDialog() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);


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
                        items.add(item);
                        SetUpRecycleView();
                      //  inflateViewForItem(item);
                        b.Heading.setVisibility(View.GONE);
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

        if (items == null) {
            items = new ArrayList<Item>();
        }

        items.add(newItem);
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
                          items.add(item);
                          SetUpRecycleView();
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
        public void onSaveInstanceState(@NonNull Bundle outstate){
        super.onSaveInstanceState(outstate);
        String json = gson.toJson(items);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        outstate.putString("Items",json);
        }
    @Override
    protected void onPause() {
        super.onPause();

        String json = gson.toJson(items);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        preferences.edit()
                .putString("Items", json)
                .apply();
    }

        //Remove Item and save
//        if (removeItem != null) {
//            items.removeAll(removeItem);
//
//            Gson gson = new Gson();
//            String json = gson.toJson(items);
//
//            getPreferences(MODE_PRIVATE).edit().putString("ITEMS", json).apply();
//
//            finish();
//        }
//
//        //save in SharedPreference
//        if (isEdited || isAdd) {
//            Gson gson = new Gson();
//            String json = gson.toJson(items);
//            getPreferences(MODE_PRIVATE).edit().putString("ITEMS", json).apply();
//            isAdd = false;
//            isEdited = false;
//        }
//
//    }
//
//

}











