package com.example.szmuk.cardiagnostics;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.UUID;

public class ObdInterface extends AppCompatActivity
{
    Button b;
    String rpm;
    String speed;

    String address;

    int tries = 0;

    boolean connected = false;

    BluetoothAdapter btAdapter;

    BluetoothDevice device;

    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    RPMCommand engineRpmCommand;
    SpeedCommand speedCommand;
    BluetoothSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obd_interface);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        address = bundle.getString("address");

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        device = btAdapter.getRemoteDevice(address);


        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!Thread.currentThread().isInterrupted())
                {
                    connectObd();
                }
            }
        }).start();




        b = (Button) findViewById(R.id.cokolwiek);

        b.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getBaseContext(), "Adapter: " + connected + " ; Tries: " + tries + " ; RPM: " + rpm + "; Speed: " + speed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void connectObd()
    {
        try
        {

            if (tries == 0)
            {
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            }
            else
            {
                socket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
            }

            socket.connect();

            try
            {
                Thread.sleep(10 * 1000);
            }
            catch(Exception exc)
            {}

            try {
                new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new TimeoutCommand(255).run(socket.getInputStream(), socket.getOutputStream());
                new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
                new AmbientAirTemperatureCommand().run(socket.getInputStream(), socket.getOutputStream());

                engineRpmCommand = new RPMCommand();
                speedCommand = new SpeedCommand();
            }
            catch (Exception e)
            {
                Log.d("[OBD]", "Failed to initialize adapter." + e.getMessage());
            }

            while (!Thread.currentThread().isInterrupted())
            {
                try
                {
                    Thread.sleep(1 * 1000);
                }
                catch(Exception exc)
                {}

                engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                speedCommand.run(socket.getInputStream(), socket.getOutputStream());

                try
                {
                    Thread.sleep(1 * 1000);
                }
                catch(Exception exc)
                {}

                rpm = engineRpmCommand.getFormattedResult();
                speed = speedCommand.getFormattedResult();

                connected = true;
            }
        }
        catch(Exception ex)
        {
            connected = false;

            try
            {
                Thread.sleep(10 * 1000);
            }
            catch(Exception exc)
            {}

            tries++;
        }
    }

}
