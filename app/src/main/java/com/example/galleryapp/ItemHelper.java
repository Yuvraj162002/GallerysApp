package com.example.galleryapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemHelper {

    private Context context;
    private OnCompleteListener listener;
    private String url1;
    private Bitmap bitmap;
    private Set<Integer> colors;
    private List<String> labels;



    public void fetchData( Context context,int a, int b, OnCompleteListener listener) {
        this.context = context;
        this.listener = listener;
        // rectangular image url
        String rectangularImageUrl = "https://picsum.photos/%d/%d";
        //fetch rectangular image
        fetchImage(String.format(rectangularImageUrl, a, b));

    }


    public void fetchData(Context context,int x, OnCompleteListener listener) {
        this.context = context;
        this.listener = listener;
        // square image url
        String squareImageUrl = "https://picsum.photos/%d";

        //fetch square image
        fetchImage(String.format(squareImageUrl, x));
    }


     // Fetch Random image

    private void fetchImage(String url) {
        new RedirectedURL().fetchRedirectedURL(new RedirectedURL.OnCompleteListener(){
            @Override
            public void onFetched(String redirectedUrl) {
                url1 = redirectedUrl;
                //Fetch image using glide
                Glide.with(context)
                        .asBitmap()
                        .load(url1)
                        .into(new CustomTarget<Bitmap>() {
                            //On image successfully fetch
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                bitmap = resource;

                                //Extract color from the image
                                extraPaletteFromBitmap();
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);

                                //call onComplete listener
                                listener.onError("Image load failed");
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }


                        });

            }


        }).execute(url);
    }




    private void extraPaletteFromBitmap() {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {

                //call getColorsFromPalette function
                colors = getColorsFromPalette(p);

                //Get label from image
                labelImage();
            }
        });
    }


    //  Fetch labels from image

    private void labelImage() {
        // creating obj.
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        //Get labels
        labeler.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {


                        ItemHelper.this.labels = new ArrayList<>();

                        //store labels in List<String> labels
                        for (ImageLabel imageLabel : labels) {
                            ItemHelper.this.labels.add(imageLabel.getText());
                        }
                        //call when all the data is fetched
                        listener.onFetch(bitmap, colors, ItemHelper.this.labels, url1);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //call onComplete listener
                        listener.onError(e.toString());
                    }
                });
    }


     // Get the color from the palette p

    private Set<Integer> getColorsFromPalette(Palette p) {
        Set<Integer> colors = new HashSet<>();

        //Store colors in Set<Integer> colors
        //Vibrant colors
        colors.add(p.getVibrantColor(0));
        colors.add(p.getLightVibrantColor(0));
        colors.add(p.getDarkVibrantColor(0));
        //Muted colors
        colors.add(p.getMutedColor(0));
        colors.add(p.getLightMutedColor(0));
        colors.add(p.getDarkMutedColor(0));

        //Remove black color
        colors.remove(0);

        return colors;

    }


     // Interface call when a image is fetch or give some error.

    interface OnCompleteListener {

        void onFetch(Bitmap bitmap, Set<Integer> colorPalette, List<String> labels, String url);


        void onError(String exception);
    }

}

