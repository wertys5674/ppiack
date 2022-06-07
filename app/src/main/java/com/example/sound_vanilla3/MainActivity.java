package com.example.sound_vanilla3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import me.relex.circleindicator.CircleIndicator3;

public class MainActivity extends AppCompatActivity {

    ViewPager2 mPager;
    FragmentStateAdapter pagerAdapter;
    int num_page = 4;
    CircleIndicator3 mIndicator;
    Button btn_goscale, btn_logout;

    public static int pickValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPager = findViewById(R.id.viewpager);

        pagerAdapter = new MyAdapter(this, num_page);
        mPager.setAdapter(pagerAdapter);

        mIndicator = findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.createIndicators(num_page,0);

        mPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        btn_goscale = findViewById(R.id.btn_goScale);
        btn_logout =findViewById(R.id.logout);

        mPager.setCurrentItem(1000);
        mPager.setOffscreenPageLimit(4);

        mPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if(positionOffsetPixels == 0){
                    mPager.setCurrentItem(position);
                }
            }

            @Override
            public void onPageSelected(int position){
                super.onPageSelected(position);
                mIndicator.animatePageSelected(position%num_page);
            }
        });

        btn_goscale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, scaleDetector.class);
                startActivity(intent);
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });



    }
}