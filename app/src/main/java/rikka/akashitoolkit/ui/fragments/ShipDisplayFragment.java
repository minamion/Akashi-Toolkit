package rikka.akashitoolkit.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.squareup.otto.Subscribe;

import rikka.akashitoolkit.R;
import rikka.akashitoolkit.adapter.ViewPagerAdapter;
import rikka.akashitoolkit.adapter.ViewPagerStateAdapter;
import rikka.akashitoolkit.otto.BookmarkAction;
import rikka.akashitoolkit.otto.BusProvider;
import rikka.akashitoolkit.otto.ShipAction;
import rikka.akashitoolkit.staticdata.ShipList;
import rikka.akashitoolkit.support.Settings;
import rikka.akashitoolkit.support.Statistics;
import rikka.akashitoolkit.ui.MainActivity;
import rikka.akashitoolkit.utils.Utils;
import rikka.akashitoolkit.widget.CheckBoxGroup;
import rikka.akashitoolkit.widget.MyViewPager;
import rikka.akashitoolkit.widget.RadioButtonGroup;
import rikka.akashitoolkit.widget.SimpleDrawerView;

/**
 * Created by Rikka on 2016/3/30.
 */
public class ShipDisplayFragment extends BaseSearchFragment {
    private MyViewPager mViewPager;
    private MainActivity mActivity;
    private int mFlag;
    private int mFinalVersion;
    private int mSpeed;
    private int mSort;
    private boolean mBookmarked;

    private CheckBoxGroup[] mCheckBoxGroups = new CheckBoxGroup[3];
    private RadioButtonGroup[] mRadioButtonGroups = new RadioButtonGroup[3];
    private Spinner mSpinner;
    private NestedScrollView mScrollView;

    private void setDrawerView() {
        mActivity.setRightDrawerLocked(false);

        mActivity.getRightDrawerContent().removeAllViews();
        //mActivity.getRightDrawerContent().addTitle("排序"/*getString(R.string.sort)*/);
        //mActivity.getRightDrawerContent().addDividerHead();

        //mActivity.getRightDrawerContent().addTitle(getString(R.string.action_filter));
        //mActivity.getRightDrawerContent().addDividerHead();

        SimpleDrawerView body = new SimpleDrawerView(getContext());

        body.addTitle(getString(R.string.sort));
        body.addDivider();

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.drawer_item_spinner, body, false);
        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.drawer_item_spinner_item, new String[]{getString(R.string.ship_type), getString(R.string.ship_class)});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mSort == position) {
                    return;
                }

                mSort = position;

                Settings
                        .instance(getContext())
                        .putInt(Settings.SHIP_SORT, mSort);

                BusProvider.instance().post(new ShipAction.SortChangeAction(mSort));
                //BusProvider.instance().post(new DataChangedAction("ShipFragment"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpinner.setAdapter(adapter);
        body.addView(view);

        body.addTitle(getString(R.string.action_filter));
        body.addDivider();

        body.setOrientation(LinearLayout.VERTICAL);
        body.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        /*mCheckBoxGroups[0] = new CheckBoxGroup(getContext());
        mCheckBoxGroups[0].addItem("仅收藏");
        mCheckBoxGroups[0].setOnCheckedChangeListener(new CheckBoxGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, int checked) {
                BusProvider.instance().post(new ShipAction.OnlyBookmarkedChangeAction(checked > 0));

                mBookmarked = checked > 0;
                Settings
                        .instance(getContext())
                        .putBoolean(Settings.SHIP_BOOKMARKED, checked > 0);
            }
        });

        body.addView(mCheckBoxGroups[0]);
        body.addDivider();*/

        /*mRadioButtonGroups[0] = new RadioButtonGroup(getContext());
        mRadioButtonGroups[0].addItem("全部");
        mRadioButtonGroups[0].addItem("未改造");
        mRadioButtonGroups[0].addItem("最终改造");
        mRadioButtonGroups[0].setOnCheckedChangeListener(new RadioButtonGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, int checked) {
                BusProvider.instance().post(new ShipAction.ShowFinalVersionChangeAction(checked));

                mFinalVersion = checked;
                Settings
                        .instance(getContext())
                        .putInt(Settings.SHIP_FINAL_VERSION, checked);
            }
        });

        body.addView(mRadioButtonGroups[0]);
        body.addDivider();*/

        mCheckBoxGroups[1] = new CheckBoxGroup(getContext());
        mCheckBoxGroups[1].addItem("低速");
        mCheckBoxGroups[1].addItem("高速");
        mCheckBoxGroups[1].setOnCheckedChangeListener(new CheckBoxGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, int checked) {
                BusProvider.instance().post(new ShipAction.SpeedChangeAction(checked));

                mSpeed = checked;
                Settings
                        .instance(getContext())
                        .putInt(Settings.SHIP_SPEED, checked);
            }
        });


        body.addView(mCheckBoxGroups[1]);
        body.addDivider();

        mCheckBoxGroups[2] = new CheckBoxGroup(getContext());
        for (int i = 2; i < ShipList.shipType.length; i++) {
            mCheckBoxGroups[2].addItem(ShipList.shipType[i]);
        }
        mCheckBoxGroups[2].setOnCheckedChangeListener(new CheckBoxGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, int checked) {
                BusProvider.instance().post(new ShipAction.TypeChangeAction(checked << 2));

                mFlag = checked << 2;
                Settings
                        .instance(getContext())
                        .putInt(Settings.SHIP_FILTER, checked);
            }
        });

        body.addView(mCheckBoxGroups[2]);

        mScrollView = new NestedScrollView(getContext());
        mScrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mScrollView.setPadding(0, Utils.dpToPx(4), 0, Utils.dpToPx(4));
        mScrollView.setClipToPadding(false);
        mScrollView.addView(body);

        ((MainActivity) getActivity()).getSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int checked = buttonView.isChecked() ? 1 : 0;

                BusProvider.instance().post(new BookmarkAction.Changed(checked > 0));

                mBookmarked = checked > 0;
                Settings
                        .instance(getContext())
                        .putBoolean(Settings.SHIP_BOOKMARKED, checked > 0);

                if (mViewPager.getCurrentItem() == 1 && !mBookmarked) {
                    mViewPager.setCurrentItem(0);
                }
            }
        });
    }

    private void postSetDrawerView() {
        mSort = Settings
                .instance(getContext())
                .getInt(Settings.SHIP_SORT, 0);

        mSpinner.setSelection(mSort);

        mFinalVersion = 1/*Settings
                .instance(getContext())
                .getInt(Settings.SHIP_FINAL_VERSION, 1)*/;

        //mRadioButtonGroups[0].setChecked(mFinalVersion);


        mBookmarked = Settings
                .instance(getContext())
                .getBoolean(Settings.SHIP_BOOKMARKED, false);

        ((MainActivity) getActivity()).getSwitch().setChecked(mBookmarked);
        //mCheckBoxGroups[0].setChecked(mBookmarked ? 1 : 0);

        mSpeed = Settings
                .instance(getContext())
                .getInt(Settings.SHIP_SPEED, 0);

        mCheckBoxGroups[1].setChecked(mSpeed);

        mFlag = Settings
                .instance(getContext())
                .getInt(Settings.SHIP_FILTER, 0);

        mCheckBoxGroups[2].setChecked(mFlag);

        mActivity.getRightDrawerContent().addView(mScrollView);
    }

    @Override
    protected boolean getRightDrawerLock() {
        return true;
    }

    @Override
    protected boolean getSwitchVisible() {
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        BusProvider.instance().register(this);
    }

    @Override
    public void onStop() {
        BusProvider.instance().unregister(this);
        super.onStop();
    }

    @Override
    public void onShow() {
        super.onShow();

        BusProvider.instance().post(new ShipAction.KeywordChanged(null));

        MainActivity activity = ((MainActivity) getActivity());
        activity.getTabLayout().setupWithViewPager(mViewPager);
        activity.getSupportActionBar().setTitle(getString(R.string.ship));

        /*Observable
                .create(new Observable.OnSubscribe<Object>() {
                    @Override
                    public void call(Subscriber<? super Object> subscriber) {
                        setDrawerView();
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                        mFinalVersion = Settings
                                .instance(getContext())
                                .getInt(Settings.SHIP_FINAL_VERSION, 0);

                        mCheckBoxGroups[0].setChecked(mFinalVersion);

                        mSpeed = Settings
                                .instance(getContext())
                                .getInt(Settings.SHIP_SPEED, 0);

                        mCheckBoxGroups[1].setChecked(mSpeed);

                        mFlag = Settings
                                .instance(getContext())
                                .getInt(Settings.SHIP_FILTER, 0);

                        mCheckBoxGroups[2].setChecked(mFlag);

                        mActivity.getRightDrawerContent().addView(mScrollView);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });*/

        setDrawerView();
        postSetDrawerView();

        // may crash
        /*new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                setDrawerView();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                postSetDrawerView();
            }
        }.execute();*/

        Statistics.onFragmentStart("ShipDisplayFragment");
    }

    @Override
    public void onHide() {
        super.onHide();

        Statistics.onFragmentEnd("ShipDisplayFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ship, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSearchExpand() {
        BusProvider.instance().post(new ShipAction.IsSearchingChanged(true));
        BusProvider.instance().post(new ShipAction.KeywordChanged(null));

        if (mViewPager.getCurrentItem() == 1) {
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onSearchCollapse() {
        BusProvider.instance().post(new ShipAction.IsSearchingChanged(false));
        BusProvider.instance().post(new ShipAction.KeywordChanged(null));
    }

    @Override
    public void onSearchTextChange(String newText) {
        BusProvider.instance().post(new ShipAction.KeywordChanged(newText.replace(" ", "")));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                mActivity.getDrawerLayout().openDrawer(GravityCompat.END);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();

        View view = inflater.inflate(R.layout.content_viewpager, container, false);
        mViewPager = (MyViewPager) view.findViewById(R.id.view_pager);
        mViewPager.setAdapter(getAdapter());
        mViewPager.setSwipeEnabled(false);

        if (!isHiddenBeforeSaveInstanceState()) {
            onShow();
        }

        return view;
    }

    private ViewPagerAdapter getAdapter() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager()) {
            @Override
            public Bundle getArgs(int position) {
                Bundle bundle = new Bundle();
                bundle.putInt("FLAG", mFlag);
                bundle.putInt("FINAL_VERSION", mFinalVersion);
                bundle.putInt("SPEED", mSpeed);
                bundle.putInt("SORT", mSort);
                bundle.putBoolean("BOOKMARKED", mBookmarked);
                return bundle;
            }
        };
        adapter.addFragment(ShipFragment.class, "全部");
        adapter.addFragment(BookmarkNoItemFragment.class, "全部");

        return adapter;
    }

    @Override
    public String getSearchHint() {
        return "搜索名称、罗马音、中文名拼音…";
    }

    @Subscribe
    public void onlyBookmarkedChanged(BookmarkAction.NoItem action) {
        mViewPager.setCurrentItem(1);
    }
}
