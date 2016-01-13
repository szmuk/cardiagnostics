package com.example.szmuk.cardiagnostics;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

public class ObdInterface extends AppCompatActivity
{
    Button b;

    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obd_interface);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Bundle bundle = getIntent().getExtras();

        BluetoothDevice device = btAdapter.getRemoteDevice(bundle.getString("address"));

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try
        {
            BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid);

            socket.connect();
        }
        catch(Exception ex)
        {
            Toast.makeText(getBaseContext(), "Failed to connect to: " + bundle.getString("address"), Toast.LENGTH_SHORT).show();
        }



        new EchoOffObdCommand().run(socket.getInputStream(), socket.getOutputStream());

        new LineFeedOffObdCommand().run(socket.getInputStream(), socket.getOutputStream());

        new TimeoutObdCommand().run(socket.getInputStream(), socket.getOutputStream());

        new SelectProtocolObdCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());



        b = (Button) findViewById(R.id.cokolwiek);

        b.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getBaseContext(), bundle.getString("address"), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
