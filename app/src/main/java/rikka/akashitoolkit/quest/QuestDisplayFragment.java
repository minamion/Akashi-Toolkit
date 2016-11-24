package rikka.akashitoolkit.quest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;

import rikka.akashitoolkit.R;
import rikka.akashitoolkit.adapter.RecycledViewPoolViewPagerAdapter;
import rikka.akashitoolkit.adapter.RecycledViewPoolViewPagerStateAdapter;
import rikka.akashitoolkit.adapter.ViewPagerAdapter;
import rikka.akashitoolkit.adapter.ViewPagerStateAdapter;
import rikka.akashitoolkit.otto.BookmarkAction;
import rikka.akashitoolkit.otto.BusProvider;
import rikka.akashitoolkit.otto.QuestAction;
import rikka.akashitoolkit.support.Settings;
import rikka.akashitoolkit.support.Statistics;
import rikka.akashitoolkit.MainActivity;
import rikka.akashitoolkit.ui.fragments.BaseSearchFragment;
import rikka.akashitoolkit.ui.fragments.ISwitchFragment;
import rikka.akashitoolkit.ui.widget.IconSwitchCompat;
import rikka.akashitoolkit.ui.widget.SimpleDrawerView;
import rikka.akashitoolkit.utils.Utils;
import rikka.akashitoolkit.ui.widget.CheckBoxGroup;

/**
 * Created by Rikka on 2016/3/6.
 */
public class QuestDisplayFragment extends BaseSearchFragment implements CheckBoxGroup.OnCheckedChangeListener, ISwitchFragment {
    private ViewPager mViewPager;
    private ViewPagerAdapter[] mViewPagerStateAdapter;
    private int mType;
    private int mFlag = -1;
    private int mJumpToQuestIndex = -1;
    private int mJumpToQuestType = -1;

    private boolean mBookmarked;

    @Override
    public void onHide() {
        super.onHide();

        Statistics.onFragmentEnd("QuestDisplayFragment");
    }

    @Override
    protected View onCreateRightDrawerContentView(@Nullable Bundle savedInstanceState) {
        SimpleDrawerView drawer = new SimpleDrawerView(getContext());
        drawer.setOrientation(LinearLayout.VERTICAL);
        drawer.addTitle(getString(R.string.action_filter));
        drawer.addDividerHead();

        CheckBoxGroup cbg = new CheckBoxGroup(getContext());
        cbg.addItem(getString(R.string.quest_normal));
        cbg.addItem(getString(R.string.quest_daily));
        cbg.addItem(getString(R.string.quest_weekly));
        cbg.addItem(getString(R.string.quest_monthly));
        cbg.setOnCheckedChangeListener(this);
        cbg.setChecked(mFlag);
        cbg.setPadding(0, Utils.dpToPx(4), 0, Utils.dpToPx(4));

        drawer.addView(cbg);
        return drawer;
    }

    @Override
    protected boolean onSetTabLayout(TabLayout tabLayout) {
        tabLayout.setupWithViewPager(mViewPager);
        return true;
    }

    @Override
    protected boolean onSetSwitch(IconSwitchCompat switchView) {
        return true;
    }


    @Override
    public void onSwitchCheckedChanged(boolean isChecked) {
        mBookmarked = isChecked;

        BusProvider.instance().post(
                new BookmarkAction.Changed(QuestFragment.TAG, mBookmarked));
    }

    @Override
    public void onShow() {
        super.onShow();

        mFlag = Settings
                .instance(getContext())
                .getInt(Settings.QUEST_FILTER, 1 + 2 + 4 + 8);

        setUpViewPager(isSearching() ? 1 : 0);

        setToolbarTitle(getString(R.string.quest));

        Statistics.onFragmentStart("QuestDisplayFragment");
    }

    @Override
    public void onCheckedChanged(View view, int checked) {
        if (mType == 0) {
            mFlag = checked;
            Settings
                    .instance(getContext())
                    .putInt(Settings.QUEST_FILTER, mFlag);

            BusProvider.instance().post(new QuestAction.FilterChanged(checked));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.quest, menu);

        super.onCreateOptionsMenu(menu, inflater);
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
        return inflater.inflate(R.layout.content_viewpager, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mViewPager.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.cardBackground));
        mViewPagerStateAdapter = new ViewPagerAdapter[2];
        mViewPagerStateAdapter[0] = getAdapter(0);
        mViewPagerStateAdapter[1] = getAdapter(1);

        super.onViewCreated(view, savedInstanceState);
    }

    private void setUpViewPager(int type) {
        mViewPager.setAdapter(mViewPagerStateAdapter[type]);
        mViewPager.setCurrentItem(type);
        mActivity.getTabLayout().setupWithViewPager(mViewPager);
        mType = type;
    }

    private ViewPagerAdapter getAdapter(int type) {
        ViewPagerAdapter adapter;

        if (type == 0) {
            adapter = new RecycledViewPoolViewPagerAdapter(getChildFragmentManager()) {
                @Override
                public Bundle getArgs(int position) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("TYPE", position);
                    bundle.putInt("FLAG", mFlag);
                    bundle.putBoolean("SEARCHING", false);
                    bundle.putInt("JUMP_INDEX", mJumpToQuestIndex);
                    bundle.putInt("JUMP_TYPE", mJumpToQuestType);
                    bundle.putBoolean("LATEST_ONLY", position == 0);
                    bundle.putBoolean("BOOKMARKED", mBookmarked);
                    return bundle;
                }
            };
            adapter.addFragment(QuestFragment.class, getString(R.string.quest_latest));

            adapter.addFragment(QuestFragment.class, "編成");
            adapter.addFragment(QuestFragment.class, "出擊");
            adapter.addFragment(QuestFragment.class, "演習");
            adapter.addFragment(QuestFragment.class, "遠征");
            adapter.addFragment(QuestFragment.class, "補給/入渠");
            adapter.addFragment(QuestFragment.class, "工廠");
            adapter.addFragment(QuestFragment.class, "改裝");
        } else {
            adapter = new RecycledViewPoolViewPagerAdapter(getChildFragmentManager()) {
                @Override
                public Bundle getArgs(int position) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("TYPE", 0);
                    bundle.putBoolean("SEARCHING", position == 1);
                    bundle.putInt("FLAG", mFlag);
                    bundle.putInt("JUMP_INDEX", mJumpToQuestIndex);
                    bundle.putInt("JUMP_TYPE", mJumpToQuestType);
                    bundle.putBoolean("LATEST_ONLY", false);
                    return bundle;
                }
            };
            adapter.addFragment(QuestFragment.class, getString(R.string.all));
            adapter.addFragment(QuestFragment.class, getString(R.string.search_result));
        }

        return adapter;
    }

    @Subscribe
    public void jumpTo(QuestAction.JumpToQuest action) {
        ((MainActivity) getActivity()).getSwitch().setChecked(false);

        mJumpToQuestType = action.getType();
        mJumpToQuestIndex = action.getIndex();

        mViewPager.setCurrentItem(isSearching() ? 0 : action.getType());
    }

    @Subscribe
    public void jumpedTo(QuestAction.JumpedToQuest action) {
        mJumpToQuestIndex = -1;
        mJumpToQuestType = -1;
    }

    public void onSearchExpand() {
        setUpViewPager(1);
    }

    public void onSearchCollapse() {
        setUpViewPager(0);
    }

    public void onSearchTextChange(String newText) {
        BusProvider.instance().post(new QuestAction.KeywordChanged(newText));
    }

    @Override
    public String getSearchHint() {
        return getString(R.string.search_hint_quest);
    }
}