package com.example.galleryapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryapp.Model.Item;
import com.example.galleryapp.databinding.ActivityGalleryBinding;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
     private static final int LOAD_IMAGE = 0;
    private static final int RESULT = 1;
    // Create Binding...
    ActivityGalleryBinding b;
    List<Item> items = new ArrayList<>();
    private static final String  No_Of_Images = "no of images";
    private static final String ITEMS = "items";
    private static final String MODE = "mode";
    private static final String IMAGE = "image";
    private static final String  COLOR = "color";
    private static final String LABEL = "label";
    int mode = 0;
    // Shared preferences
    SharedPreferences preferences;
    ImageAdapter adapter;
    private String imageUrl;
   private  Context context = this;
    ItemTouchHelper.Callback callback2;
    ItemTouchHelper itemTouchHelper1;
    Bitmap bitmap;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 32;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate((getLayoutInflater()));
        setContentView(b.getRoot());
        preferences = getPreferences(MODE_PRIVATE);
        getDataFromSharedPreference();


        if (!items.isEmpty()) {
            showListItems(items);
            } else {
            b.Heading.setVisibility(View.VISIBLE);

        }
        enableandDisableDragoption();
    }

    private void enableandDisableDragoption() {
        b.OnOffDrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode==0){
                    mode = 1;
                    adapter.mode =1;
                    Toast.makeText(context, "Drag Enabled", Toast.LENGTH_SHORT).show();
                    List<ImageAdapter.ImageViewHolder>holders = adapter.holderList;
                    b.OnOffDrag.setBackgroundTintList(getResources().getColorStateList(R.color.purple_200));
                    b.OnOffDrag.setRippleColor(getResources().getColorStateList(R.color.purple_500));

                    b.OnOffDrag.setImageResource(R.drawable.drag);
                    for (int  i =0;i<holders.size();i++){
                        holders.get(i).eventListenerHandler();
                    }
                    itemTouchHelper1.attachToRecyclerView(b.list);
                }
                else{
                    mode=0;
                    adapter.mode=0;
                    Toast.makeText(context, "Drag disabled", Toast.LENGTH_SHORT).show();
                    List<ImageAdapter.ImageViewHolder>holders = adapter.holderList;
                    for (int  i =0;i<holders.size();i++){
                        holders.get(i).eventListenerHandler();
                    }
                    b.OnOffDrag.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
                    b.OnOffDrag.setRippleColor(getResources().getColorStateList(R.color.purple_200));
                    b.OnOffDrag.setImageResource(R.drawable.notdrag);
                    itemTouchHelper1.attachToRecyclerView(null);
                }
            }
        });
    }

    void dragandDropRestore() {

            if (mode == 1) {
                mode = 1;
                adapter.mode = 1;
                List<ImageAdapter.ImageViewHolder> holders = adapter.holderList;
                b.OnOffDrag.setBackgroundTintList(getResources().getColorStateList(R.color.purple_200));
                b.OnOffDrag.setRippleColor(getResources().getColorStateList(R.color.purple_500));

                b.OnOffDrag.setImageResource(R.drawable.drag);
                for (int i = 0; i < holders.size(); i++) {
                    holders.get(i).eventListenerHandler();
                }
                itemTouchHelper1.attachToRecyclerView(b.list);
            } else {
                mode = 0;
                adapter.mode = 0;
                List<ImageAdapter.ImageViewHolder> holders = adapter.holderList;
                for (int i = 0; i < holders.size(); i++) {
                    holders.get(i).eventListenerHandler();
                }
                b.OnOffDrag.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
                b.OnOffDrag.setRippleColor(getResources().getColorStateList(R.color.purple_200));
                b.OnOffDrag.setImageResource(R.drawable.notdrag);
                itemTouchHelper1.attachToRecyclerView(null);
            }
        }



    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //Image Url of Context Menu
        imageUrl = adapter.imageUrl;
        int index = adapter.index;
        ItemCardBinding binding = adapter.itemCardBinding;
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

        if (item.getItemId() == R.id.shareImage)

            shareImage(binding);

            return true;
        }

      //  return super.onContextItemSelected(item);



    /// Method for search....
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery,menu);

        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
        return true;
    }
////Choose  option from Action menu bar.....
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addImage) {
            showAddImageDialog();
            return true;
        }
        if (item.getItemId() == R.id.addGallery) {
            showAddgalleryDialog();
        }
        if (item.getItemId() == R.id.Sort_by){
            adapter.SortData();
            return true;
        }

        return false;
    }
/// Callback for swap method....
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
            items.remove(viewHolder.getAdapterPosition());
            Toast.makeText(context, "Image removed", Toast.LENGTH_SHORT).show();
            if (items.isEmpty()) {
                b.Heading.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }

        }
    };
    // Dialog Option...
    private void showAddImageDialog() {

        new AddImageDialog()
                .showDialog(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdd(Item item) {
                        items.add(item);
                        showListItems(items);
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
    /// Method to show list item...
    private void showListItems(List<Item>item) {
        adapter = new ImageAdapter(this,items);
       this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        b.list.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        adapter.setImageAdapter(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(b.list);
      //  itemTouchHelper.attachToRecyclerView(b.list);
        callback2 = new ImageTouchHelperCallback(adapter);
        itemTouchHelper1 = new ItemTouchHelper(callback2);
        adapter.setImageAdapter(itemTouchHelper1);
        b.list.setAdapter(adapter);
        dragandDropRestore();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    }
    private String itemFromJson(String string){
        Gson json2 = new Gson();
        return json2.fromJson(string, (Type) Item.class);
    }
    private void getDataFromSharedPreference() {

        mode =  preferences.getInt(MODE,0);
        showListItems(items);
        if (items==null){
            b.Heading.setVisibility(View.VISIBLE);
        }
        else {
            b.Heading.setVisibility(View.GONE);
        }
    }

    private String jsonFromItem(Item item){
        Gson json = new Gson();
        return json.toJson(item);
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
                    showListItems(items);
                 //   inflateViewForItem(item);
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
    private void shareImage(ItemCardBinding binding){

        Bitmap bitmap = loadBitmapFromView(binding.getRoot());


        // Calling the intent to share the bitmap
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/png");
        //Create the value obj..
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "title");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);


        OutputStream outputStream;
        try {
            outputStream = getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    public Bitmap loadBitmapFromView(View view){
            Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(),view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable !=null){
            bgDrawable.draw(canvas);
        }
        else {
            canvas.drawColor(Color.WHITE);
            view.draw(canvas);
        }
        return returnedBitmap;
    }






   //// Method to show the Dialog  add image...
    private void showAddgalleryDialog() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);


    }


//    private void PotraitmodeOnly() {
//        if (this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//            isDialogBoxShowed = true;
//
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
//    }






//    private void inflateViewForItem(Item item) {
//
//        if (noOfImages == 0) {
//            b.Heading.setVisibility(View.GONE);
//        }
//        //Inflate layout
//        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
//
//        //Bind data
//      //  binding.imageview.setImageBitmap(item.image);
//        binding.Title.setBackgroundColor(item.color);
//        binding.Title.setText(item.label);
//
//
//        b.list.addView(binding.getRoot());
//
//        //Add Item
//        Item newItem = new Item(item.color, item.label, item.url);
//
//
//
//        if (items == null) {
//            items = new ArrayList<Item>();
//        }
//
//        items.add(newItem);
//        isAdd = true;
//        noOfImages++;
//    }


    @Override
    protected void onPause() {
        super.onPause();

        int itemCount = 0;
        for (Item item : items){
            if (item!=null){
                itemCount++;

                preferences.edit()
                        .putString(ITEMS+itemCount,jsonFromItem(item))
                        .apply();
            }
        }
        preferences.edit()
                .putInt(No_Of_Images,itemCount)
                .apply();

        preferences.edit()
                .putInt(MODE,mode);

    }




}











