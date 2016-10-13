package go.upsseriallogger;


import java.io.Serializable;

public class Item  implements Serializable {

    String header;
    String subheader;
    Boolean btconnection;
    Integer absolute_index_fromSerialDevList;
    String mac;


    Item(String h, String s, Boolean b, Integer index, String m){
        this.header=h;
        this.subheader=s;
        this.btconnection=b;
        this.absolute_index_fromSerialDevList=index;
        this.mac=m;
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