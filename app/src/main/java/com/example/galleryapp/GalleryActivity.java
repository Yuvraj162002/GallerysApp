package com.example.galleryapp;

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

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
     private static final int LOAD_IMAGE = 0;
    private static final int RESULT = 1001;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ///ya pe krna h kuch...
        b = ActivityGalleryBinding.inflate((getLayoutInflater()));
        setContentView(b.getRoot());
        preferences = getPreferences(MODE_PRIVATE);
        getDataFromSharedPreference();


        if (!items.isEmpty()) {
            showListItems(items);
            } else {
            b.list.setVisibility(View.VISIBLE);

        }
        enableDisableDrag();
    }

    private void enableDisableDrag() {
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

    void dragDropButtonRestore() {

            if (mode == 1) {
                mode = 1;
                adapter.mode = 1;
                List<ImageAdapter.ImageViewHolder> holders = adapter.holderList;
                b.OnOffDrag.setBackgroundTintList(getResources().getColorStateList(R.color.purple_200));
                b.OnOffDrag.setRippleColor(getResources().getColorStateList(R.color.purple_200));

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
                b.OnOffDrag.setBackgroundTintList(getResources().getColorStateList(R.color.purple_200));
                b.OnOffDrag.setRippleColor(getResources().getColorStateList(R.color.purple_200));
                b.OnOffDrag.setImageResource(R.drawable.notdrag);
                itemTouchHelper1.attachToRecyclerView(null);
            }
        }

        //  enableSwipeToDeleteAndUndo();


        //Load data from sharedPreferences
        //   loadSharedPreferenceData();

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
        //// problem toh isme................................................
        if (item.getItemId() == R.id.shareImage) {
            try {
                shareImage(binding);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

/// Menu Option..

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery,menu);

        /// problem toh isme....................................................
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /// ye dehkna h...
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

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
            items.remove(viewHolder.getAdapterPosition());
            Toast.makeText(context, "Image removed", Toast.LENGTH_SHORT).show();
            // jada h toh isse bracket ko hta dege...
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
    private void showListItems(List<Item>items) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        adapter = new ImageAdapter(this, items);
        b.list.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        adapter.setImageAdapter(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(b.list);
        callback2 = new ImageTouchHelperCallback(adapter);
        adapter.setImageAdapter(itemTouchHelper1);
        b.list.setAdapter(adapter);
        dragDropButtonRestore();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    }
    private void getDataFromSharedPreference() {
        int itemCount = preferences.getInt(No_Of_Images,0);

        for (int i = 0; i<=itemCount ; i++){
        Item item = new Item(preferences.getString(IMAGE+i,"")
                            ,preferences.getInt(COLOR+i,0)
                           ,preferences.getString(LABEL+i,""));
        items.add(item);
        }
        mode =  preferences.getInt(MODE,0);
        showListItems(items);
//        if (items==null){
//            b.list.setVisibility(View.VISIBLE);
//        }
//        else {
//            b.list.setVisibility(View.GONE);
//        }
    }

    private String jsonFromItem(Item item){
        Gson json = new Gson();
        return json.toJson(item);
    }

    private String itemFromJson(String string){
        Gson json2 = new Gson();
        return json2.fromJson(string, (Type) Item.class);
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


//    private void enableSwipeToDeleteAndUndo() {
//        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this){
//            @Override
//            public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int i) {
//                final int position = viewHolder.getAdapterPosition();
//                final Item item = adapter.getData().get(position);
//
//                adapter.removeItem(item, position);
//
//                Snackbar snackbar = Snackbar.make(coordinatorLayout,"Item was removed",Snackbar.LENGTH_LONG);
//                snackbar.setAction("UNDO", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        adapter.removeItem(item,position);
//              //          recyclerView.scrollToPosition(position);
//
//
//                    }
//                });
//                snackbar.setActionTextColor(Color.YELLOW);
//                snackbar.show();
//            }
//        };
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
//        itemTouchHelper.attachToRecyclerView(recyclerView);
//    }



//    private void savedInstancedata(Bundle savedInstanceState) {
//     //   b.Heading.setVisibility(View.GONE);
//        String json = savedInstanceState.getString("Items",null);
//        items = gson.fromJson(json,new TypeToken<List<Item>>(){}.getType());
//        if (items !=null){
//            SetUpRecycleView(items);
//        }
//        else {
//            items = new ArrayList<>();
//        }
//    }
//

//    // Load Shared Prefrence...
//    private void loadSharedPreferenceData() {
//      b.Heading.setVisibility(View.GONE);
//        SharedPreferences preferences =  getPreferences(MODE_PRIVATE);
//        String json = preferences.getString("Items",null);
//        items = gson.fromJson(json,new TypeToken<List<Item>>(){}.getType());
//        if (items !=null){
//            SetUpRecycleView(items);
//        }
//        else {
//            items = new ArrayList<>();
//        }
//    }
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


    private void shareImage(ItemCardBinding binding) throws FileNotFoundException {
        String bitmapPath =MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,"palatee", "share palatee");
        Uri bitmapUri = Uri.parse(bitmapPath);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri );
        startActivity(Intent.createChooser(intent,"Share"));
    }




//    public void SortData() {
//        if (!isSorted) {
//            isSorted = true;
//            List<Item> sortedItem = new ArrayList<>(items);
//            Collections.sort(sortedItem, (p1, p2) -> p1.label.compareTo(p2.label));
//            if (adapter != null) {
//                for (int i = 0; i < sortedItem.size(); i++) {
//                    adapter.labelItem = sortedItem;
//                    adapter.showSortedItems();
//                    b.list.setAdapter(adapter);
//                }
//            }
//            }else {
//            isSorted = false;
//        }
//    }
//
     //// problem toh isme.............................
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

//public void showItems(List<Item>items){
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//        adapter = new ImageAdapter(this,items);
//        b.list.
//}

//        @Override
//        public void onSaveInstanceState(@NonNull Bundle outstate){
//        super.onSaveInstanceState(outstate);
//        String json = gson.toJson(items);
//        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
//        outstate.putString("Items",json);
//        }
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
//        String json = gson.toJson(items);
//        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
//        preferences.edit()
//                .putString("Items", json)
//                .apply();
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











