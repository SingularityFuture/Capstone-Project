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

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance("Welcome to Blockwatch", "The only Android watchface\nbased on the Bitcoin Blockchain\n\nEach wheel is a current transaction hash\non the blockchain", R.drawable.onlywatch, ContextCompat.getColor(this,R.color.md_red_500)));
        addSlide(AppIntroFragment.newInstance("Price History", "Click on the price to see Bitcoin's price history", R.drawable.price_history, ContextCompat.getColor(this,R.color.md_light_green_500)));
        addSlide(AppIntroFragment.newInstance("Transaction IP Address Map", "Click on the Watch Wheel to see where the transaction hash was relayed", R.drawable.transaction_map, ContextCompat.getColor(this,R.color.md_blue_500)));
        addSlide(AppIntroFragment.newInstance("Price Widget", "Add a widget to your screen to monitor the price", R.drawable.widget_full, ContextCompat.getColor(this,R.color.md_orange_500)));
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }
}