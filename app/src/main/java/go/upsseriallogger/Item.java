package go.upsseriallogger;


import java.io.Serializable;

public class Item  implements Serializable {

    String header;
    String subheader;
    Boolean btconnection;
    Integer absolute_index_fromSerialDevList;
    String mac;
    String ip;
    Byte typeconnection;

    public final static Byte wifi_connection = 1;
    public final static Byte bt_connection = 2;
    public final static Byte serial_connection = 3;


    Item(String h, String s,  Integer index, String m,String ip, Byte tc){
        this.header=h;
        this.subheader=s;
       // this.btconnection=b;
        this.absolute_index_fromSerialDevList=index;
        this.mac=m;
        this.ip=ip;
        this.typeconnection=tc;
    }

    public String getIP() {
        return ip;
    }
    public void setIP(String ip) {
        this.ip = ip;
    }

    public Byte geTypeConnection() {
        return typeconnection;
    }
    public void setTypeConnection(Byte header) {
        this.typeconnection = header;
    }

    public String getHeader() {
        return header;
    }
    public void setHeader(String header) {
        this.header = header;
    }

    public String getSubHeader() {return subheader;   }
    public void setSubHeader(String subHeader) {
        this.subheader = subHeader;
    }

    public Boolean getBtconnection() {
        return btconnection;
    }
    public void setBtconnection(Boolean bt) {
        this.btconnection = bt;
    }

    public Integer getIndexPort() {
        return absolute_index_fromSerialDevList;
    }
    public void setIndexPort(Integer index) {
        this.absolute_index_fromSerialDevList = index;
    }

    public String getMac() {return mac;   }
    public void setMac(String MAC) {
        this.mac = MAC;
    }
}