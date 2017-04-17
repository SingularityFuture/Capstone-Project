package com.example.blockwatch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by test on 4/16/2017.
 */

public class IntroSlidesClass  extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
/*        addSlide(firstFragment);
        addSlide(secondFragment);
        addSlide(thirdFragment);
        addSlide(fourthFragment);*/

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance("Welcome to Blockwatch", "The only Android watchface based on the Bitcoin Blockchain.", R.mipmap.color_wheel, ContextCompat.getColor(this,R.color.md_red_500)));
        addSlide(AppIntroFragment.newInstance("Welcome to Blockwatch", "The only Android watchface based on the Bitcoin Blockchain.", R.mipmap.color_wheel, ContextCompat.getColor(this,R.color.md_light_green_500)));
        addSlide(AppIntroFragment.newInstance("Welcome to Blockwatch", "The only Android watchface based on the Bitcoin Blockchain.", R.mipmap.color_wheel, ContextCompat.getColor(this,R.color.md_red_500)));

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }
}