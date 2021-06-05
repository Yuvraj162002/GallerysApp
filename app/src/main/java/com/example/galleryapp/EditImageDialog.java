package com.example.galleryapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.galleryapp.Model.Item;
import com.example.galleryapp.databinding.ChipColorBinding;
import com.example.galleryapp.databinding.ChipLabelBinding;
import com.example.galleryapp.databinding.DialogEditImageBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

    public class EditImageDialog {
        private Context context;
        private EditImageDialog.onCompleteListener listener;
        private DialogEditImageBinding b;
        private LayoutInflater inflater;

        private boolean isCustomLabel;
        public Bitmap bitmap;
        private AlertDialog dialog;
        private Set<Integer> colors;
        private String imageUrl;



          //Show Edit Image Dialog Box





        public void show(Context context, String imageUrl, EditImageDialog.onCompleteListener listener) {
            this.listener = listener;
            this.imageUrl = imageUrl;
            this.context = context;
            if (context instanceof GalleryActivity) {
                inflater = ((GalleryActivity) context).getLayoutInflater();
                b = DialogEditImageBinding.inflate(inflater);
            } else {
                dialog.dismiss();
                listener.onError("Cast Exception");
                return;
            }


            //Create and Show Dialog
            dialog = new MaterialAlertDialogBuilder(context)

                    .setView(b.getRoot())
                    .show();

            fetchImage(imageUrl);
            updateNewColorAndLabel();
        }
        void fetchImage(String url) {

            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull  Bitmap resource, @Nullable  Transition<? super Bitmap> transition) {
                            bitmap = resource;
                            extractPaletteFromBitmap();
                        }

                        @Override
                        public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {

                        }
                    });
        }
        /// Update the image...
        private void updateNewColorAndLabel() {
            b.edImageView.setImageBitmap(bitmap);
            b.updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int colorChipId = b.edColorChips.getCheckedChipId(),
                            labelChipId = b.edLabelChips.getCheckedChipId();

                    if (colorChipId == -1 || labelChipId == -1) {
                        Toast.makeText(context, "Please choose color & label", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String label;
                    if (isCustomLabel) {
                        label = b.edCustomLabelEt.getText().toString().trim();
                        if (label.isEmpty()) {
                            Toast.makeText(context, "Please enter custom label", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        label = ((Chip) b.edLabelChips.findViewById(labelChipId)).getText().toString();
                    }
                    int color = ((Chip) b.edColorChips.findViewById(colorChipId)).getChipBackgroundColor().getDefaultColor();

                    //Send Callback
                    Item item = new Item(imageUrl,color,label);
                    listener.onEditCompleted(item);
                    dialog.dismiss();
                }
            });
        }
        private void extractPaletteFromBitmap() {
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    colors = getColorsFromPalette(palette);
                    extractLabels();
                }
            });
        }




        private void extractLabels() {
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
            labeler.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(@NonNull List<ImageLabel> imageLabels) {
                            List<String> strings = new ArrayList<>();
                            for (ImageLabel label : imageLabels) {
                                strings.add(label.getText());
                            }
                            inflateColorChips(colors);
                            inflateLabelChips(strings);
                            b.edImageView.setImageBitmap(bitmap);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            listener.onError(e.toString());
                        }
                    });
        }



        private Set<Integer> getColorsFromPalette(Palette palette) {
            Set<Integer> colors = new HashSet<>();
            colors.add(palette.getVibrantColor(0));
            colors.add(palette.getLightVibrantColor(0));
            colors.add(palette.getDarkVibrantColor(0));

            colors.add(palette.getMutedColor(0));
            colors.add(palette.getLightMutedColor(0));
            colors.add(palette.getDarkMutedColor(0));

            colors.add(palette.getVibrantColor(0));
            colors.remove(0);

            return colors;
        }


          //inflates color chips to main root

        private void inflateColorChips(Set<Integer> colors) {
            //Inflate color chips to edit dialog
            for (int color : colors) {
                ChipColorBinding binding = ChipColorBinding.inflate(inflater);
                binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
                b.edColorChips.addView(binding.getRoot());
            }

        }



       //   Inflates label chips to main root

        private void inflateLabelChips(List<String> labels) {


            //Inflate label chips to edit dialog
            for (String label : labels) {
                ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
                binding.getRoot().setText(label);
                b.edLabelChips.addView(binding.getRoot());
            }
            handleCustomLabelInput();
        }



         // Takes Custom Label Input

        private void handleCustomLabelInput() {
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText("Custom");
            b.edLabelChips.addView(binding.getRoot());

            binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    b.edCustomLabelInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    isCustomLabel = isChecked;
                }
            });
        }


          //callbacks for edit completion

        public interface onCompleteListener {
            void onEditCompleted(Item item);

            void onError(String error);

        }

    }

