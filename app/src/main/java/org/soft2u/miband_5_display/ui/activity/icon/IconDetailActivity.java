package org.soft2u.miband_5_display.ui.activity.icon;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;

import org.soft2u.miband_5_display.PrefManager;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.ui.fragment.icon.IconDetailOneFragment;
import org.soft2u.miband_5_display.ui.fragment.icon.IconDetailTwoFragment;
import org.soft2u.miband_5_display.utils.LocaleHelper;

import java.util.Arrays;

public class IconDetailActivity extends AppCompatActivity {
    
    private ViewPager viewPager;
    private int[] layouts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final FragmentPagerAdapter myViewPagerAdapter;
        setAppLanguage();
        setContentView(R.layout.activity_icon_detail);

        // ads banner bottom
        MobileAds.initialize(this, (InitializationStatus initializationStatus) -> { });
        RequestConfiguration requestConfiguration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList(
                "7B21EEA5F5F327CF30F5F208575D0FCD",
                AdRequest.DEVICE_ID_EMULATOR)).build();
        MobileAds.setRequestConfiguration(requestConfiguration);
        AdView mAdView = findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        viewPager = findViewById(R.id.view_pager);
        layouts = new int[] {
            R.layout.fragment_icon_detail_one,
            R.layout.fragment_icon_detail_two };
        myViewPagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(myViewPagerAdapter);
//        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
//
//        btnNext.setOnClickListener((View.OnClickListener) v -> {
//            int current = viewPager.getCurrentItem() + 1;
//            if (current < layouts.length) {
//                // move to next screen
//                viewPager.setCurrentItem(current);
//            }
//        });
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends FragmentPagerAdapter {
        //private LayoutInflater layoutInflater;

        private MyViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    IconDetailOneFragment one = new IconDetailOneFragment();
                    return one;
                case 1:
                    IconDetailTwoFragment two = new IconDetailTwoFragment();
                    return two;
                default:
                    return null;
            }
        }

//        @Override
//        public Object instantiateItem(ViewGroup container, int position) {
//            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//            View view = layoutInflater.inflate(layouts[position], container, false);
//            container.addView(view);
//
//            return view;
//        }
//
        @Override
        public int getCount() {
            return layouts.length;
        }
//
//        @Override
//        public boolean isViewFromObject(View view, Object obj) {
//            return view == obj;
//        }
//
//
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            View view = (View) object;
//            container.removeView(view);
//        }
    }

    private void setAppLanguage() {
        String localeApp = new PrefManager(this).getLocale();
        String localeSys = String.valueOf(getResources().getConfiguration().locale).substring(0, 2);
        // first time: empty
        if(localeApp.equals(""))
            localeApp = localeSys;

        LocaleHelper.setLocale(this, localeApp);
    }

    @Override
    protected void attachBaseContext(Context base) {
        LocaleHelper localeHelper = new LocaleHelper();
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}