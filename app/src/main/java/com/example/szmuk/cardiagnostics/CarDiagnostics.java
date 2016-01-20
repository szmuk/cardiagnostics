package com.example.szmuk.cardiagnostics;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class CarDiagnostics extends AppCompatActivity
{
    Button b;
    Button refresh;
    Switch enable_bt;
    ListView paired_list;

    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_diagnostics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ENABLE BLUETOOTH BUTTON

        enable_bt = (Switch) findViewById(R.id.enable_bluetooth);

        enable_bt.setChecked(btAdapter.isEnabled());

        enable_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    if (btAdapter.isEnabled() == false)
                    {
                        btAdapter.enable();
                    }
                }
                else
                {
                    if (btAdapter.isEnabled())
                    {
                        btAdapter.disable();
                    }
                }
            }
        });

        // PAIRED DEVICES LIST

        ArrayList deviceStrs = new ArrayList();
        final ArrayList devices = new ArrayList();

        Set <BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        //LIST
        paired_list = (ListView) findViewById(R.id.bt_list);
        paired_list.setAdapter(adapter);

        paired_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {

                String deviceAddress = devices.get(position).toString();

                Intent i = new Intent(view.getContext(), ObdInterface.class);
                i.putExtra("address", deviceAddress);

                if (btAdapter.isEnabled())
                {
                    startActivity(i);
                } else
                {
                    Toast.makeText(getBaseContext(), "YOU HAVE TO ENABLE BLUETOOTH FIRST!", Toast.LENGTH_LONG).show();
                }
            }
        });

        refresh = (Button) findViewById(R.id.button_refresh);

        refresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // todo: nie dziala
                adapter.notifyDataSetChanged();
            }
        });

        b = (Button) findViewById(R.id.button_pair_new);

        b.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS), 0);
            }
        });

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_car_diagnostics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }
        else if (id == R.id.action_authors)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
