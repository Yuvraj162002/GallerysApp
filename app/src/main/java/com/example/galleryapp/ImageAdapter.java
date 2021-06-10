package com.example.galleryapp;

import android.content.Context;
import android.text.Layout;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.galleryapp.Model.Item;
import com.example.galleryapp.databinding.ItemCardBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder>implements ImageTouchHelperAdapter {
    private Context context;
   private  List<Item> items, VisiblelabelItem;
    public ItemTouchHelper mainitemTouchHelper;
    public String imageUrl;
    public int index;
    public ItemCardBinding itemCardBinding;
   // ItemCardBinding binding =  ItemCardBinding.inflate((getLayoutInflater()));


    public int mode;
    public List<ImageViewHolder> holderList = new ArrayList<>();
    public static final String MODE = "mode";


    public ImageAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
        VisiblelabelItem = items;

    }

    @NonNull
    @Override
    // Image view Holder..
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(LayoutInflater.from(context)
                , parent, false);
        return new ImageViewHolder(binding);

    }
/// Bind view Holder...
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holderList.add(holder);

        holder.b.Title.setText(VisiblelabelItem.get(position).label);
        holder.b.Title.setBackgroundColor(VisiblelabelItem.get(position).color);
        // Item itemList = labelItem.get(position);

        Glide.with(context)
                .asBitmap()
                .load(VisiblelabelItem.get(position).image)
                .into(holder.b.imageview);

    }
///getCount..
    @Override
    public int getItemCount() {

        return VisiblelabelItem.size();
    }



//// filter method..
    public void filter(String query) {
        if (query.trim().isEmpty()) {
            VisiblelabelItem = items;
            notifyDataSetChanged();
            return;
        }
        else if (items.contains(query))
        query = query.toLowerCase();

        List<Item> filterdata = new ArrayList<>();

        for (Item item : items) {
            if (item.label.toLowerCase().contains(query)) {
                filterdata.add(item);
            }
            else{
               // GalleryActivity b = GalleryActivity.
                ItemCardBinding b = ItemCardBinding.inflate(LayoutInflater.from(context));
                b.title.setVisibility(View.VISIBLE);

            }
        }
        VisiblelabelItem = filterdata;
        notifyDataSetChanged();
    }

    public void setImageAdapter(ItemTouchHelper itemTouchHelper) {
        mainitemTouchHelper = itemTouchHelper;
    }
//// Want to move the item..
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Item formItem = items.get(fromPosition);
        items.remove(formItem);
        items.add(toPosition, formItem);
        VisiblelabelItem = items;
        notifyItemMoved(fromPosition, toPosition);
    }
/// Wantb to dismiss the item..
    @Override
    public void onItemDis(int position) {
        return;
    }

    public void SortData() {
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.label.toLowerCase().compareTo(o2.label.toLowerCase());
            }


        });
        VisiblelabelItem = items;
        notifyDataSetChanged();

    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, View.OnTouchListener, GestureDetector.OnGestureListener {
        ItemCardBinding b;
        GestureDetector gestureDetector;

        public ImageViewHolder(@NonNull ItemCardBinding b) {
            super(b.getRoot());
            this.b = b;
            gestureDetector = new GestureDetector(b.getRoot().getContext(), this);
            eventListenerHandler();

        }

        void eventListenerHandler() {
            if (mode == 0) {
                b.imageview.setOnTouchListener(null);
                b.Title.setOnTouchListener(null);
                b.Title.setOnCreateContextMenuListener(this);
                b.imageview.setOnCreateContextMenuListener(this);
            } else if (mode == 1) {
                b.Title.setOnCreateContextMenuListener(null);
                b.imageview.setOnCreateContextMenuListener(null);
                b.Title.setOnTouchListener(this);
                b.imageview.setOnTouchListener(this);
            }
        }

//        @Override
//        public void onCreateContextMenu(ContextMenu menu,View v,ContextMenu.ContextMenuInfo menuInfo){
//
//        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mode == 1) {
                mainitemTouchHelper.startDrag(this);
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select ");
            menu.add(this.getAdapterPosition(), R.id.editMenuItem, 0, "Edit");
            menu.add(this.getAdapterPosition(), R.id.shareImage, 0, "Share");
            imageUrl = items.get(this.getAdapterPosition()).image;
            index = this.getAdapterPosition();
            itemCardBinding = b;

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }
    }



}