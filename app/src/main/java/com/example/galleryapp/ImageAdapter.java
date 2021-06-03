package com.example.galleryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.example.galleryapp.model.Item;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    public String imageUrl;
    public int index;
    public ItemCardBinding itemCardBinding;
    Context context;
    List<Item> labelItem, VisiblelabelItem;
   public LinearLayout viewBackground;

    public ImageAdapter(Context context, List<Item> labelItem) {
        this.context = context;
        this.labelItem = labelItem;
        VisiblelabelItem = new ArrayList<>(labelItem);

    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(LayoutInflater.from(context)
                , parent, false);
        return new ImageViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Item itemList = labelItem.get(position);
        Glide.with(context)
                .load(itemList.url)
                .into(holder.b.imageview);
        // problem aati h toh iss label ko change kr dege...
        holder.b.Title.setText(itemList.label);

        holder.b.Title.setBackgroundColor(itemList.color);
    }

    @Override
    public int getItemCount() {

        return labelItem.size();
    }

//    public void filter(String newText) {
//    }

//    public void SortData() {
//        notifyDataSetChanged();
//
//    }

    public void showSortedItems() {
        notifyDataSetChanged();
    }

    public void SortData() {
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ItemCardBinding b;

        public ImageViewHolder(ItemCardBinding b) {
            super(b.getRoot());
            this.b = b;

        }
    }

  //  @Override
    public Filter getFilter(){
        return filter;
    }
    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Item> filterdata = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filterdata.addAll(VisiblelabelItem);
            } else {
                for (Item item : VisiblelabelItem) {
                    if (item.label.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filterdata.add(item);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filterdata;
            return filterResults;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            labelItem.clear();
            labelItem.addAll((List<Item>) results.values);
            notifyDataSetChanged();
        }
    };
    public void removeItem(Item item, int position) {
        labelItem.remove(position);
        notifyItemRemoved(position);
    }

//    public void restoreItem(String item, int position) {
//        labelItem.add(item,position);
//        notifyItemInserted(position);
//    }

//    public  void removeImage(int position){
//        labelItem.remove(position);
//
//        notifyItemRemoved(position);
//    }
    public void restoreItem(Item item,int position){
        labelItem.add(position,item);

        notifyItemInserted(position);
    }
    public List<Item> getData() {
        return labelItem;
    }
    
}
