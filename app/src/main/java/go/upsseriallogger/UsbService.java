package go.upsseriallogger;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsbService extends Service {

    public static final String ACTION_USB_READY = "com.felhr.connectivityservices.USB_READY";
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_NOT_SUPPORTED = "com.felhr.usbservice.USB_NOT_SUPPORTED";
    public static final String ACTION_NO_USB = "com.felhr.usbservice.NO_USB";
    public static final String ACTION_USB_PERMISSION_GRANTED = "com.felhr.usbservice.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "com.felhr.usbservice.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_DISCONNECTED = "com.felhr.usbservice.USB_DISCONNECTED";
    public static final String ACTION_CDC_DRIVER_NOT_WORKING = "com.felhr.connectivityservices.ACTION_CDC_DRIVER_NOT_WORKING";
    public static final String ACTION_USB_DEVICE_NOT_WORKING = "com.felhr.connectivityservices.ACTION_USB_DEVICE_NOT_WORKING";
    public final static String BROADCAST_ACTION_DEVlIST = "ru.startandroid.develop.p0961servicebackbroadcast";
    public final static String GET_ACTION_DEVlIST = "ru.startandroid.develop.p0961getservicebackbroadcast";
    public final static String STOPPORT_ACTION= "ru.startandroid.develop.p0961stopportservicebackbroadcast";
    public final static String STARTPORT_ACTION= "ru.startandroid.develop.p0961startportservicebackbroadcast";
    public final static String ACTION_LOG_LIST= "ru.startandroid.develop.p0961loglistportservicebackbroadcast";
    public final static String ESCL_ACTION= "ru.startandroid.develop.p0961_ESCL_portservicebackbroadcast";
    public final static String ESCH_ACTION= "ru.startandroid.develop.p0961_ESCH_portservicebackbroadcast";
    public final static String ESCB_ACTION= "ru.startandroid.develop.p0961_ESCB_portservicebackbroadcast";
    public final static String ESCN_ACTION= "ru.startandroid.develop.p0961_ESCN_portservicebackbroadcast";
    public static final int MESSAGE_FROM_SERIAL_PORT = 0;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private static final int BAUD_RATE = 115200; // BaudRate. Change this value if you need
    public static boolean SERVICE_CONNECTED = false;

    private IBinder binder = new UsbBinder();

    private Context context;
    private Handler mHandler;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;

    public boolean serialPortConnected;
    private String fullstring="";
    final ArrayList<String> logstrings = new ArrayList<String>();

    //====================
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                String data = new String(arg0, "UTF-8");
                if (mHandler != null){
                   if(!data.contains("\n"))fullstring=fullstring+data;
                    else {
                    Intent intent = new Intent(ACTION_LOG_LIST);
                    intent.putExtra("data",fullstring);
                    context.sendBroadcast(intent);
                       fullstring="";
                   }
                }
                  //  mHandler.obtainMessage(MESSAGE_FROM_SERIAL_PORT, data).sendToTarget();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

 void hexwrite(String str){
     write(hexStringToByteArray(str));
 }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (arg1.getAction()) {
                case ESCL_ACTION:
                    hexwrite("1B");
                    hexwrite("4C");
                    break;
                case ESCH_ACTION:
                    hexwrite("1B");
                    hexwrite("48");
                    break;
                case ESCB_ACTION:
                    hexwrite("1B");
                    hexwrite("42");
                    break;
                case ESCN_ACTION:
                    hexwrite("1B");
                    hexwrite("4E");
                    break;
                case GET_ACTION_DEVlIST:
                    comportlist();
                    break;

                case ACTION_USB_ATTACHED:
                    comportlist();
                    break;
                case ACTION_USB_DETACHED:
                    comportlist();

                    if (serialPortConnected) {
                        stopport(); }
                    break;
                case STARTPORT_ACTION:
                    startport(arg1.getExtras().getInt("position"));
                    break;
                case STOPPORT_ACTION:
                    stopport();
                    break;
                case ACTION_USB_PERMISSION:
                    boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    if (granted) {
                        Intent intent = new Intent(ACTION_USB_PERMISSION_GRANTED);
                        arg0.sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                        arg0.sendBroadcast(intent);
                    }
                    break;
            }
        }
    };

    //=======================================
    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        filter.addAction(GET_ACTION_DEVlIST);
        filter.addAction(STOPPORT_ACTION);
        filter.addAction(STARTPORT_ACTION);
        filter.addAction(ESCL_ACTION);
        filter.addAction(ESCH_ACTION);
        filter.addAction(ESCB_ACTION);
        filter.addAction(ESCN_ACTION);
        registerReceiver(usbReceiver, filter);
    }

    //=================================


    @Override
    public void onCreate() {
        this.context = this;
        serialPortConnected = false;
        UsbService.SERVICE_CONNECTED = true;
        setFilter();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UsbService.SERVICE_CONNECTED = false;
    }

    //========================================================================


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }


    public void write(byte[] data) {
        if (serialPort != null)
            serialPort.write(data);
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    //================чтение списка ком портов и отправка =====================
    private void comportlist() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;

            logstrings.clear();
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();
                logstrings.add("Device: " + Integer.toString(deviceVID) + "/" + Integer.toString(devicePID));

                if (!keep)
                    break;
            }

            Intent intent = new Intent(BROADCAST_ACTION_DEVlIST);
            intent.putStringArrayListExtra("arraylist", logstrings);
            intent.putExtra("clearlist", false);
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent(BROADCAST_ACTION_DEVlIST);
            intent.putExtra("clearlist", true);
            sendBroadcast(intent);
        }

    }
//=============================================================================

    public class UsbBinder extends Binder {
        public UsbService getService() {
            return UsbService.this;
        }
    }

    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
            if (serialPort != null) {
                if (serialPort.open()) {
                    serialPortConnected = true;
                    serialPort.setBaudRate(Global_data.Gd_BAUDRATE);
                    serialPort.setDataBits(Global_data.Gd_DATABITS);
                    serialPort.setStopBits(Global_data.Gd_STOPBITS);
                    serialPort.setParity(Global_data.Gd_PARITY);
                    serialPort.setFlowControl(Global_data.Gd_FLOWCONTROL);
                    serialPort.read(mCallback);
                    Intent intent = new Intent(ACTION_USB_READY);
                    context.sendBroadcast(intent);
                }
            }
        }
    }

    private void startport(Integer position) {
            HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            Integer counter = 0;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                if (counter == position) {
                    break;
                } else {
                    counter += 1;
                }
            }

            connection = usbManager.openDevice(device);
            new ConnectionThread().start();
        }
     }

    private void stopport() {

        if (serialPortConnected) {
            serialPort.close();
        }
        serialPortConnected = false;
    }



}
