package rikka.akashitoolkit.fleet_editor;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import rikka.akashitoolkit.R;
import rikka.akashitoolkit.equip_list.EquipAdapter;
import rikka.akashitoolkit.equip_list.EquipListFragment;
import rikka.akashitoolkit.model.Ship;
import rikka.akashitoolkit.otto.BusProvider;
import rikka.akashitoolkit.otto.ItemSelectAction;
import rikka.akashitoolkit.staticdata.ShipList;
import rikka.akashitoolkit.ui.BaseActivity;

/**
 * Created by Rikka on 2016/7/31.
 */
public class EquipSelectActivity extends BaseActivity {

    public static final String EXTRA_SHIP_ID = "EXTRA_SHIP_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.equip_select));

        if (savedInstanceState == null) {

            Ship ship = ShipList.findItemById(getIntent().getIntExtra(EXTRA_SHIP_ID, -1));
            ArrayList<Integer> list = null;
            if (ship != null) {
                list = new ArrayList<>();

                if (ship.getShipType().getEquipType() != null) {
                    int i = 1;
                    for (char c : ship.getShipType().getEquipType().toCharArray()) {
                        if (c == '1') {
                            list.add(i);
                        }
                        i++;
                    }
                }

                if (ship.getExtraEquipType() != null)
                    list.addAll(ship.getExtraEquipType());
            }


            EquipListFragment fragment = new EquipListFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(EquipAdapter.ARG_SELECT_MODE, true);
            bundle.putIntegerArrayList(EquipAdapter.ARG_TYPE_IDS, list);
            //bundle.putInt(EquipAdapter.ARG_SHIP_ID, getIntent().getIntExtra(EXTRA_SHIP_ID, -1));
            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        BusProvider.instance().register(this);
    }

    @Override
    protected void onStop() {
        BusProvider.instance().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onItemSelected(ItemSelectAction.Finish action) {
        Intent intent = new Intent();
        intent.putExtra(FleetEditActivity.EXTRA_ID, action.getId());
        setResult(RESULT_OK, intent);
        finish();
    }
}
