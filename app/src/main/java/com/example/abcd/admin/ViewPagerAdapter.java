package com.example.abcd.admin;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.example.abcd.R;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {
    private final ArrayList<Uri> imageUrls; // Corrected the variable declaration
    private final LayoutInflater layoutInflater;

    public ViewPagerAdapter(Context context, ArrayList<Uri> imageUrls){
        this.imageUrls = imageUrls; // Use "this" to refer to the instance variable
        layoutInflater = LayoutInflater.from(context); // Initialize the layoutInflater
    }

    @Override
    public int getCount(){
        return imageUrls.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@Nullable ViewGroup container, int position){
        View view = layoutInflater.inflate(R.layout.show_images_layout,container,false);
        ImageView imageView = view.findViewById(R.id.uploadImage);
        imageView.setImageURI(imageUrls.get(position));
        container.addView(view);

        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object){
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull View container, int position, @NonNull Object object){
        ((RelativeLayout)object).removeView(container);
    }


}
