package com.start.crypto.android;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.support.RouterPagerAdapter;
import com.start.crypto.android.notification.NotificationsController;
import com.start.crypto.android.portfolio.PortfoliosController;
import com.start.crypto.android.views.PreventSwipeViewPager;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

public class PagerController extends BaseController {

    @BindView(R.id.navigation) BottomNavigationView mNavigation;
    @BindView(R.id.view_pager) PreventSwipeViewPager mPager;

    private final RouterPagerAdapter mPagerAdapter;
    private MenuItem mPrevMenuItem;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
            switch (item.getItemId()) {
                    case R.id.navigation_home:
                        mPager.setCurrentItem(0);
                        break;
                    case R.id.navigation_dashboard:
                        mPager.setCurrentItem(1);
                        break;
            }
            return false;
    };

    public PagerController() {

        List<Controller> controllers = Arrays.asList(new PortfoliosController(), new HomeController(), new NotificationsController());

        mPagerAdapter = new RouterPagerAdapter(this) {
            @Override
            public void configureRouter(@NonNull Router router, int position) {
                if (!router.hasRootController()) {
                    Controller page = controllers.get(position);
                    router.setRoot(RouterTransaction.with(page));
                }
            }
            @Override
            public int getCount() {
                return controllers.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                ControllerPageTitle page = (ControllerPageTitle)controllers.get(position);
                return page.getPageTitle(getActivity());
            }

        };
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.main_pager_controller, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mPrevMenuItem != null) {
                    mPrevMenuItem.setChecked(false);
                } else {
                    mNavigation.getMenu().getItem(0).setChecked(false);
                }

                mNavigation.getMenu().getItem(position).setChecked(true);
                mPrevMenuItem = mNavigation.getMenu().getItem(position);

                getActionBar().setTitle(mPagerAdapter.getPageTitle(position));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mPager.setCurrentItem(1);
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        if (!getActivity().isChangingConfigurations()) {
            mPager.setAdapter(null);
        }
        super.onDestroyView(view);
    }

}