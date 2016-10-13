package go.upsseriallogger;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsbService extends Service {

    private final static String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private final static String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private final static String ACTION_FOUND = "android.bluetooth.device.action.FOUND";
    private final static String ACTION_DISCOVERY_FINISHED = "android.bluetooth.adapter.action.DISCOVERY_FINISHED";

    public final static String SET_DEVLIST_USB = "go.action.upsserialcontroller_setdevlistusb_servicebackbroadcast";
    public final static String CLEAR_DEVLIST_USB = "go.action.upsserialcontroller_clearusbdevlistusb_servicebackbroadcast";
    public final static String GET_ACTION_DEVlIST_USB = "go.action.upsserialcontroller_get_action_servicebackbroadcast";
    public final static String SET_DEVLIST_BT = "go.action.upsserialcontroller_setdevlistbt_servicebackbroadcast";
    public final static String CLEAR_DEVLIST_BT = "go.action.upsserialcontroller_clearusbdevlistbt_servicebackbroadcast";
    public final static String GET_ACTION_DEVlIST_BT = "go.action.upsserialcontroller_get_action_bt_servicebackbroadcast";
    public final static String STOPPORT_ACTION= "go.action.upsserialcontroller_stop.portservicebackbroadcast";
    public final static String STARTPORT_ACTION= "go.action.upsserialcontroller_start.portservicebackbroadcast";
    public final static String ACTION_LOG_LIST= "go.action.upsserialcontroller_log.listportservicebackbroadcast";
    public final static String ESCL_ACTION= "go.action.upsserialcontroller_ESCL_portservicebackbroadcast";
    public final static String ESCH_ACTION= "go.action.upsserialcontroller_ESCH_portservicebackbroadcast";
    public final static String ESCB_ACTION= "go.action.upsserialcontroller_ESCB_portservicebackbroadcast";
    public final static String ESCN_ACTION= "go.action.upsserialcontroller_ESCN_portservicebackbroadcast";
    public final static String END_DISCOVERY= "go.action.upsserialcontroller_enddiscovery_servicebackbroadcast";

   // public static final int MESSAGE_FROM_SERIAL_PORT = 0;
    public static boolean SERVICE_CONNECTED = false;
    private static final String HEX_L = "4C";
    private static final String HEX_ESC = "1B";
    private static final String HEX_H = "48";
    private static final String HEX_B = "42";
    private static final String HEX_N = "4E";
    private final IBinder binder = new UsbBinder();

    private Context context;
    private Handler mHandler;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;

    private BluetoothAdapter BA;

    private  boolean serialPortConnected;
    private String fullstring="";
    private  ArrayList<Item> devListUsb = new ArrayList<>();
    private  ArrayList<Item> devListBT = new ArrayList<>();
    //====================
    private final UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
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

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private void hexwrite(String str){
     write(hexStringToByteArray(str));
 }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (arg1.getAction()) {
                case ESCL_ACTION:
                    hexwrite(HEX_ESC);
                    hexwrite(HEX_L);
                    break;
                case ESCH_ACTION:
                    hexwrite(HEX_ESC);
                    hexwrite(HEX_H);
                    break;
                case ESCB_ACTION:
                    hexwrite(HEX_ESC);
                    hexwrite(HEX_B);
                    break;
                case ESCN_ACTION:
                    hexwrite(HEX_ESC);
                    hexwrite(HEX_N);
                    break;
                case GET_ACTION_DEVlIST_USB:
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
                case ACTION_FOUND:
                  foundbtlist(arg1);
                    break;
                case GET_ACTION_DEVlIST_BT:
                    startfindbtdevices();
                    break;
                case ACTION_DISCOVERY_FINISHED:
                    Intent intent = new Intent(END_DISCOVERY);
                    sendBroadcast(intent);
                    break;
                          }
        }
    };

    //=======================================
    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        filter.addAction(GET_ACTION_DEVlIST_USB);
        filter.addAction(GET_ACTION_DEVlIST_BT);
        filter.addAction(STOPPORT_ACTION);
        filter.addAction(STARTPORT_ACTION);
        filter.addAction(ESCL_ACTION);
        filter.addAction(ESCH_ACTION);
        filter.addAction(ESCB_ACTION);
        filter.addAction(ESCN_ACTION);
        filter.addAction(ACTION_FOUND);
        filter.addAction(ACTION_DISCOVERY_FINISHED);

        registerReceiver(usbReceiver, filter);


    }

    //=================================


    @Override
    public void onCreate() {
        this.context = this;
        serialPortConnected = false;
        UsbService.SERVICE_CONNECTED = true;
        setFilter();
        BA = BluetoothAdapter.getDefaultAdapter();
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


    private void write(byte[] data) {
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

            devListUsb.clear();
            Item item=new Item("","",false,0,"000000");
            int i = 0;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();
                UsbSerialDevice.isSupported(device);

                item.header="Serial device: ("+ Integer.toString(deviceVID) + "/" + Integer.toString(devicePID)+")";
                item.subheader=serialPort.adapter_name;
                item.mac="0000000";
                item.btconnection=false;
                item.absolute_index_fromSerialDevList=i;
               devListUsb.add(item);
                i += 1;
                  }

            Intent intent = new Intent(SET_DEVLIST_USB);
            Bundle bundle=new Bundle();
            bundle.putSerializable("usbdevlist", devListUsb);
            intent.putExtras(bundle);
                sendBroadcast(intent);
        } else {
            Intent intent = new Intent(CLEAR_DEVLIST_USB);
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
                if (counter.equals(position)) {
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

    // ===============блюпуп

 /*   public void boundedbtlist(){
        pairedDevices = BA.getBondedDevices();
        devListBT.clear();
        Item item=new Item("","",false,0,"000000");
        int i = 0;

           for(BluetoothDevice bt : pairedDevices) {
            item.header = bt.getName();
            item.subheader = bt.getAddress();
            item.mac = bt.getAddress();
            item.btconnection = true;
            item.absolute_index_fromSerialDevList = 0;
               devListBT.add(item);
            i += 1;
                  }
        Intent intent = new Intent(SET_DEVLIST_BT);
        Bundle bundle=new Bundle();
        bundle.putSerializable("btdevlist", devListBT);
        intent.putExtras(bundle);
        sendBroadcast(intent);
      }*/

    public void foundbtlist(Intent intent) {
        BluetoothDevice device= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        //devListBT.clear();
        //здесь нужна проверка на одинаковые девайсы
        Integer list_lines_count=devListBT.size();
        Boolean flag=false;

        if (list_lines_count!=0)
        for (int  k = 0; k < list_lines_count; ++k)
        if (devListBT.get(k).mac.equals(device.getAddress()))flag=true;

        if (!flag)
           {
            Item item = new Item("", "", false, 0, "000000");
            item.header = device.getName();
            item.subheader = device.getAddress();
            item.mac = device.getAddress();
            item.btconnection = true;
            item.absolute_index_fromSerialDevList = 0;
            devListBT.add(item);

            Intent intent2 = new Intent(SET_DEVLIST_BT);
            Bundle bundle = new Bundle();
            bundle.putSerializable("btdevlist", devListBT);
            intent2.putExtras(bundle);
            sendBroadcast(intent2);
        }
     }

    public void startfindbtdevices(){
        if (BA.isEnabled()) {
            devListBT.clear();
            BA.startDiscovery();
        }

    }


    public void cancelfindbtdevices(){
        BA.cancelDiscovery();
    }
}
