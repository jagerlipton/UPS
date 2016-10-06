package go.upsseriallogger;


public class Item {
    String header;
    String subHeader;
    Item(String h, String s){
        this.header=h;
        this.subHeader=s;
    }

    public String getHeader() {
        return header;
    }
    public void setHeader(String header) {
        this.header = header;
    }
    public String getSubHeader() {
        return subHeader;
    }
    public void setSubHeader(String subHeader) {
        this.subHeader = subHeader;
    }

}