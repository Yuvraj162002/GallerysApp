package com.example.galleryapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.galleryapp.databinding.ChipColorBinding;
import com.example.galleryapp.databinding.ChipLabelBinding;
import com.example.galleryapp.databinding.DialogAddImageBinding;
import com.example.galleryapp.model.Item;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


import java.io.OutputStream;
import java.util.List;
import java.util.Set;


public class AddImageDialog implements ItemHelper.OnCompleteListener {
    private Context context;
    private OnCompleteListener listener;
    private LayoutInflater inflater;
    private DialogAddImageBinding b;
    private boolean isCustomLabel;
    private Bitmap image;
    private AlertDialog dialog;
    private String url;
    private Item item;
    private boolean isAlreadyChecked;

    // Show Dialog..
    public void showDialog(Context context, OnCompleteListener listener) {
        if (!initializeDialog(context, listener))
            return;

        //Handle dimensions
        handelDimensions();

        //Handle cancel event
        handelCancelButton();


    }


     // Edit Image which is come from caches..


    public void editFetchImage(Context context, Item item, OnCompleteListener listener) {
        this.url = item.url;
        this.item = item;
        if (!initializeDialog(context, listener))
            return;

        b.title.setText("Edit image");
        b.addbutton.setText("Edit");
        b.ProgressSubTitle.setText("Please wait...");

        editImage(url);

        //Handle cancel event
        handelCancelButton();
    }


    // Check Dialog Initialize or not...

    private boolean initializeDialog(Context context, OnCompleteListener listener) {
        this.context = context;
        this.listener = listener;

       // Inflate Dialog Layout...
        if (context instanceof GalleryActivity) {
            //Initialize inflater
            inflater = ((GalleryActivity) context).getLayoutInflater();

            //Initialize binding
            b = DialogAddImageBinding.inflate(inflater);

        } else {
            dialog.dismiss();
            //call listener
            listener.onError("Cast Exception");
            return false;
        }

        //Create and show Dialog...
        dialog = new MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
                .setCancelable(false)
                .setView(b.getRoot())
                .show();
        return true;
    }


    /// Edit image...
    private void editImage(String url) {
        b.InputDimensionRoot.setVisibility(View.GONE);
        b.ProgressIndiacatorRoot.setVisibility(View.VISIBLE);

        new ItemHelper().editImage(url, context, this);
    }

    ///Handle cancel button..

    private void handelCancelButton() {
        //click event on Cancel button
        b.CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

   /// Step 1... Input Dimension...
    private void handelDimensions() {
        //click event on FetchImage button
        b.FetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get text from edit text...
                String heightstr = b.height.getText().toString().trim(),
                        widthstr = b.width.getText().toString().trim();

                // Guard Code...
                if (heightstr.isEmpty() && widthstr.isEmpty()) {
                    b.height.setError("Please enter at least one dimension");
                    return;
                }
                /// Update UI
                b.InputDimensionRoot.setVisibility(View.GONE);
                b.ProgressIndiacatorRoot.setVisibility(View.VISIBLE);


                     // Square Image..
                if (widthstr.isEmpty()) {
                    fetchImage(Integer.parseInt(heightstr));
                } else if (heightstr.isEmpty()) {
                    fetchImage(Integer.parseInt(widthstr));
                } //Rectangular Image..
                else {
                    fetchImage(Integer.parseInt(widthstr), Integer.parseInt(heightstr));
                }

                //hide keyboard
                hideKeyboard();
            }
        });

    }

  //  Hide keyBoard when add button click

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(b.addbutton.getWindowToken(), 0);
    }



          /// Step 2...Fetch Random Image..
    //Rectangular Image..
    private void fetchImage(int a, int b) {

        new ItemHelper().
                fetchData(context,a,b, this);
    }

   //Square Image..
    private void fetchImage(int x) {

        new ItemHelper().fetchData(context,x,  this);
    }



    //  Call when image all data fetch completely

    @Override
    public void onFetch(Bitmap bitmap, Set<Integer> colorPalette, List<String> labels, String url) {
        //call function
        this.url = url;
        showData(bitmap, colorPalette, labels);
    }

    @Override
    public void onError(String exception) {
        dialog.dismiss();
        listener.onError(exception);
    }

    ///Step 3 : Show Data..
    private void showData(Bitmap bitmap, Set<Integer> colorPalette, List<String> labels) {
        this.image = bitmap;
        b.fetchImage.setImageBitmap(bitmap);

        //Inflate colorChip layout
        inflatePaletteChips(colorPalette);

        //Inflate labelChip layout
        inflateLabelChips(labels);

     //   handleShareImageEvent();

        //update views
        b.ProgressIndiacatorRoot.setVisibility(View.GONE);
        b.MainRoot.setVisibility(View.VISIBLE);
        b.CustomLabel.setVisibility(View.GONE);
        b.CancelButton.setVisibility(View.VISIBLE);

        //Handel Custom label
        handelCustomLabel();


        //Handel add Button
        handelAddImageEvent();
    }

//    private void handleShareImageEvent() {
//     //   b.shareImgBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try{
//                    Glide.with(context)
//                            .asBitmap()
//                            .load(url)
//                            .into(new CustomTarget<Bitmap>() {
//                                @Override
//                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                                    // Calling the intent to share the bitmap
//                                    Bitmap icon = resource;
//                                    Intent share = new Intent(Intent.ACTION_SEND);
//                                    share.setType("image/jpeg");
//
//                                    ContentValues values = new ContentValues();
//                                    values.put(MediaStore.Images.Media.TITLE, "title");
//                                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//                                    Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                                            values);
//
//
//                                    OutputStream outputStream;
//                                    try {
//                                        outputStream = context.getContentResolver().openOutputStream(uri);
//                                        icon.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//                                        outputStream.close();
//                                    } catch (Exception e) {
//                                        System.err.println(e.toString());
//                                    }
//
//                                    share.putExtra(Intent.EXTRA_STREAM, uri);
//                                    context.startActivity(Intent.createChooser(share, "Share Image"));
//                                }
//
//                                @Override
//                                public void onLoadCleared(@Nullable Drawable placeholder) {
//
//                                }
//                            });
//
//                } catch (Exception e) {
//                    Log.e("Error on sharing", e + " ");
//                    Toast.makeText(context, "App not Installed", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
//
//



    private void inflatePaletteChips(Set<Integer> colors) {
        for (Integer color : colors) {
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            this.b.ColorChips.addView(binding.getRoot());


            //Edit Image
            //select chip if color present
            if (item != null && item.color == color) {
                binding.getRoot().setChecked(true);
                Log.d("Abhi", "inflatePaletteChips: ");
            }
        }
    }


    private void inflateLabelChips(List<String> labels) {
        for (String label : labels) {
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);

            binding.getRoot().setText(label);
            this.b.LabelChips.addView(binding.getRoot());

            //Edit Image
            //Select chip if label present
            if (item != null && item.label.equals(label)) {
                Log.d("Abhi", "inflateLabelChips: ");
                binding.getRoot().setChecked(true);
                isAlreadyChecked = true;
            }
        }
    }


    private void handelCustomLabel() {
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText("Custom");
        b.LabelChips.addView(binding.getRoot());

        //Edit Image

        if (item != null && !isAlreadyChecked) {
            binding.getRoot().setChecked(true);
            b.CustomLabel.setVisibility(View.VISIBLE);
            b.customlabel.setText(item.label);
            isCustomLabel = true;
        }

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                b.CustomLabel.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                isCustomLabel = isChecked;
            }
        });
    }

   //Hanndle Add Image Event...
    private void handelAddImageEvent() {
        //click event on Add Butoon
        b.addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int chipLabelId = b.LabelChips.getCheckedChipId(),
                        chipPaletteId = b.ColorChips.getCheckedChipId();

                //Guard Code..
                if (chipLabelId == -1 || chipPaletteId == -1) {
                    Toast.makeText(context, "Please select color & label", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Get color & label
                String label;
                if (isCustomLabel) {
                    label = b.customlabel.getText().toString().trim();
                    if (label.isEmpty()) {
                        Toast.makeText(context, "Please enter custom label", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    label = ((Chip) b.LabelChips.findViewById(chipLabelId)).getText().toString();
                }


                int color = ((Chip) b.ColorChips.findViewById(chipPaletteId)).getChipBackgroundColor().getDefaultColor();

                //Send callback
                listener.onImageAdd(new Item(image, color, label, url));

                dialog.dismiss();
            }
        });
    }


   /// Interface call when a image is fetch or give some error.
    public interface OnCompleteListener {

        void onImageAdd(Item item);

        void onError(String error);
    }

}




