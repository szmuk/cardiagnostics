package com.example.szmuk.cardiagnostics;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.DistanceMILOnCommand;
import com.github.pires.obd.commands.control.ModuleVoltageCommand;
import com.github.pires.obd.commands.control.VinCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.OilTempCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.pressure.FuelPressureCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.UUID;

public class ObdInterface extends AppCompatActivity
{
    Button b;

    String speed;
    String distance;
    String rpm;
    String fuelLevel;
    String consumption;

    String vin;
    String voltage;
    String oilTemp;
    String fuelPress;
    String intakePress;
    String massAirflow;
    String throttlePos;

    TextView speedV;
    TextView distanceV;
    TextView rpmV;
    TextView fuelLevelV;
    TextView consumptionV;

    TextView vinV;
    TextView voltageV;
    TextView oilTempV;
    TextView fuelPressV;
    TextView intakePressV;
    TextView massAirflowV;
    TextView throttlePosV;

    String address;

    int tries = 0;

    boolean connected = false;

    BluetoothAdapter btAdapter;
    BluetoothSocket socket;
    BluetoothDevice device;

    UUID uuid;

    SpeedCommand speedCommand;
    DistanceMILOnCommand distCommand;
    RPMCommand engineRpmCommand;
    FuelLevelCommand fuelCommand;
    ConsumptionRateCommand consumptionCommand;

    VinCommand vinCommand;
    ModuleVoltageCommand voltageCommand;
    OilTempCommand oilTempCommand;
    FuelPressureCommand fuelPressureCommand;
    IntakeManifoldPressureCommand intakePressureCommand;
    MassAirFlowCommand massAirflowCommand;
    ThrottlePositionCommand throttlePosCommand;

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

        uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        speedV = (TextView) findViewById(R.id.speed);
        distanceV = (TextView) findViewById(R.id.distance);
        rpmV = (TextView) findViewById(R.id.rpm);
        fuelLevelV = (TextView) findViewById(R.id.fuel_lev);
        consumptionV = (TextView) findViewById(R.id.consumption);

        vinV = (TextView) findViewById(R.id.vin);
        voltageV = (TextView) findViewById(R.id.voltage);
        oilTempV = (TextView) findViewById(R.id.oil_temp);
        fuelPressV = (TextView) findViewById(R.id.fuel_pres);
        intakePressV = (TextView) findViewById(R.id.intake_pres);
        massAirflowV = (TextView) findViewById(R.id.mass_air);
        throttlePosV = (TextView) findViewById(R.id.throttle_pos);

        b = (Button) findViewById(R.id.errors);

        b.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                updateView();
                Toast.makeText(getBaseContext(), "Function not implemented.", Toast.LENGTH_SHORT).show();
            }
        });

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

            try {
                new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new TimeoutCommand(600).run(socket.getInputStream(), socket.getOutputStream());
                new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
                new AmbientAirTemperatureCommand().run(socket.getInputStream(), socket.getOutputStream());

                speedCommand = new SpeedCommand();
                distCommand = new DistanceMILOnCommand();
                engineRpmCommand = new RPMCommand();
                fuelCommand = new FuelLevelCommand();
                consumptionCommand = new ConsumptionRateCommand();

                vinCommand = new VinCommand();
                voltageCommand = new ModuleVoltageCommand();
                oilTempCommand = new OilTempCommand();
                fuelPressureCommand = new FuelPressureCommand();
                intakePressureCommand = new IntakeManifoldPressureCommand();
                massAirflowCommand = new MassAirFlowCommand();
                throttlePosCommand = new ThrottlePositionCommand();
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

                speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                distCommand.run(socket.getInputStream(), socket.getOutputStream());
                engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                fuelCommand.run(socket.getInputStream(), socket.getOutputStream());
                consumptionCommand.run(socket.getInputStream(), socket.getOutputStream());

                vinCommand.run(socket.getInputStream(), socket.getOutputStream());
                voltageCommand.run(socket.getInputStream(), socket.getOutputStream());
                oilTempCommand.run(socket.getInputStream(), socket.getOutputStream());
                fuelPressureCommand.run(socket.getInputStream(), socket.getOutputStream());
                intakePressureCommand.run(socket.getInputStream(), socket.getOutputStream());
                massAirflowCommand.run(socket.getInputStream(), socket.getOutputStream());
                throttlePosCommand.run(socket.getInputStream(), socket.getOutputStream());

                try
                {
                    Thread.sleep(1 * 1000);
                }
                catch(Exception exc)
                {}

                speed = speedCommand.getFormattedResult();
                distance = distCommand.getFormattedResult();
                rpm = engineRpmCommand.getFormattedResult();
                fuelLevel = fuelCommand.getFormattedResult();
                consumption = consumptionCommand.getFormattedResult();

                vin = vinCommand.getFormattedResult();
                voltage = voltageCommand.getFormattedResult();
                oilTemp = oilTempCommand.getFormattedResult();
                fuelPress = fuelPressureCommand.getFormattedResult();
                intakePress = intakePressureCommand.getFormattedResult();
                massAirflow = massAirflowCommand.getFormattedResult();
                throttlePos = throttlePosCommand.getFormattedResult();



                updateView();

                connected = true;
            }
        }
        catch(Exception ex)
        {
            Log.d("[OBD]", "Failed to send OBD commands: " + ex.getMessage());

            connected = false;
            tries++;

            try
            {
                Thread.sleep(10 * 1000);
            }
            catch(Exception exc)
            {}
        }
    }

    private void updateView()
    {
        runOnUiThread(new Runnable() {
        @Override
        public void run()
        {
            speedV.setText("Speed: " + speed);
            distanceV.setText("Distance: " + distance);
            rpmV.setText("RPM: " + rpm);
            fuelLevelV.setText("Fuel level: " + fuelLevel);
            consumptionV.setText("Constumption: " + consumption);

            vinV.setText("VIN: " + vin);
            voltageV.setText("Voltage: " + voltage);
            oilTempV.setText("Oil temperature: " + oilTemp);
            fuelPressV.setText("Fuel pressure: " + fuelPress);
            intakePressV.setText("Intake pressure: " + intakePress);
            massAirflowV.setText("Mass Airflow: " + massAirflow);
            throttlePosV.setText("Throttle position: " + throttlePos);
        }
    });
    }
}
