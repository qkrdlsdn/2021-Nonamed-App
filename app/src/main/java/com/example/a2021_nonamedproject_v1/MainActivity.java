package com.example.a2021_nonamedproject_v1;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter btAdapter = null;
    private final static int REQUEST_ENABLE_BT = 1;

    TextView textStatus;
    Button btnParied, btnSearch, btnSend;
    ListView listView;

    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    BluetoothSocket btSocket = null;
    String TAG = "MainActivity";
    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    ConnectedThread connectedThread;

    View bluetoothLayout, controlLayout, streamLayout;
    FloatingActionButton drive, back, left, right;

    //mjpg-streaming
    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings; //웹뷰세팅
    private String StreamUrl = "http://localhost:8090/?action=stream";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get permission
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        ActivityCompat.requestPermissions(MainActivity.this, permission_list,  1);

        // Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        InitViewId();

        btnSend.setBackgroundColor(Color.rgb(102,102,102));
        btnSend.setClickable(false);
        Streaming();

        drive.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        onClickButtonSend(v);
                        return true;
                    case MotionEvent.ACTION_UP:
                        onClickButtonSend(controlLayout);
                        return true;
                    default:
                        return false;
                }
            }
        });

        back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        onClickButtonSend(v);
                        return true;
                    case MotionEvent.ACTION_UP:
                        onClickButtonSend(controlLayout);
                        return true;
                    default:
                        return false;
                }
            }
        });

        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        onClickButtonSend(v);
                        return true;
                    case MotionEvent.ACTION_UP:
                        onClickButtonSend(controlLayout);
                        return true;
                    default:
                        return false;
                }
            }
        });

        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        onClickButtonSend(v);
                        return true;
                    case MotionEvent.ACTION_UP:
                        onClickButtonSend(controlLayout);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    public void InitViewId() {
        // variables
        textStatus = (TextView) findViewById(R.id.text_status);
        btnParied = (Button) findViewById(R.id.btn_paired);
        btnSearch = (Button) findViewById(R.id.btn_search);
        btnSend = (Button) findViewById(R.id.btn_send);
        listView = (ListView) findViewById(R.id.listview);

        // show paired devices
        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();
        listView.setAdapter(btArrayAdapter);

        //select device
        listView.setOnItemClickListener(new myOnItemClickListener());

        //Layout Change
        bluetoothLayout = (View)findViewById(R.id.bluetooth);
        controlLayout = (View) findViewById(R.id.control);
        streamLayout = (View) findViewById(R.id.streaming);

        //bluetooth control
        drive = (FloatingActionButton) findViewById(R.id.drive);
        back = (FloatingActionButton) findViewById(R.id.back);
        left = (FloatingActionButton) findViewById(R.id.left);
        right = (FloatingActionButton) findViewById(R.id.right);

        //webView
        mWebView = (WebView) findViewById(R.id.webView);
    }

    public void onClickButtonPaired(View view){
        btArrayAdapter.clear();
        if(deviceAddressArray!=null && !deviceAddressArray.isEmpty()){ deviceAddressArray.clear(); }
        pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                btArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
            }
        }
    }

    public void onClickButtonSearch(View view){
        // Check if the device is already discovering
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        } else {
            if (btAdapter.isEnabled()) {
                btAdapter.startDiscovery();
                btArrayAdapter.clear();
                if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
                    deviceAddressArray.clear();
                }
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, filter);
            } else {
                Toast.makeText(getApplicationContext(), "bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onClickButtonSend(View view){
        String command = null;
        if(connectedThread!=null){
            if (view.getId() == R.id.drive) command = "d";
            else if (view.getId() == R.id.back) command = "b";
            else if (view.getId() == R.id.left) command = "l";
            else if (view.getId() == R.id.right) command = "r";
            else command = "p";

            connectedThread.write(command);
            //Toast.makeText(getApplicationContext(), "send" + command, Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "connect fail", Toast.LENGTH_SHORT).show();
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                btArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
                btArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    public class myOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplicationContext(), btArrayAdapter.getItem(position), Toast.LENGTH_SHORT).show();

            textStatus.setText("try...");
            //선택된 기기의 이름과 주소를 가져옴
            final String name = btArrayAdapter.getItem(position); // get name
            final String address = deviceAddressArray.get(position); // get address
            boolean flag = true;
            // 가져온 주소로 BluetoothDevice를 만듬
            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            // 그 기기의 소켓 생성 및 연결 시도
            try {
                btSocket = createBluetoothSocket(device);
                btSocket.connect();
            } catch (IOException e) {
                flag = false;
                textStatus.setText("connection failed!");
                e.printStackTrace();
                btnSend.setBackgroundColor(Color.rgb(102,102,102));
                btnSend.setClickable(false);
            }

            // 블루투스 통신 시작
            if(flag){
                textStatus.setText("connected to "+name);
                connectedThread = new ConnectedThread(btSocket);
                connectedThread.start();
                btnSend.setClickable(true);
                btnSend.setBackgroundColor(Color.rgb(50,150,50));
            }

        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }

    public void ChangeMod(View v) {
        if (bluetoothLayout.getVisibility() == View.VISIBLE) { //bluetooth -> control
            getSupportActionBar().hide();
            bluetoothLayout.setVisibility(View.INVISIBLE);
            streamLayout.setVisibility(View.INVISIBLE);
            controlLayout.setVisibility(View.VISIBLE);
        } else if (bluetoothLayout.getVisibility() == View.INVISIBLE) { //control to bluetooth
            getSupportActionBar().show();
            controlLayout.setVisibility(View.INVISIBLE);
            bluetoothLayout.setVisibility(View.VISIBLE);
            streamLayout.setVisibility(View.VISIBLE);
            mWebView.loadUrl(StreamUrl);
        }
    }

    @Override
    public void onBackPressed() {
        if (bluetoothLayout.getVisibility() == View.INVISIBLE) {
            getSupportActionBar().show();
            controlLayout.setVisibility(View.INVISIBLE);
            bluetoothLayout.setVisibility(View.VISIBLE);
        }
        else super.onBackPressed();
    }

    public void Streaming() {
        mWebView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
        mWebSettings = mWebView.getSettings(); //세부 세팅 등록
        mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
        mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부
    }
}
