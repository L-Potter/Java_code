import java.nio.charset.Charset;

public class Test {
    public static void main(String[] args) throws Exception {
        String str = "中文";
        byte[] defaultByte = str.getBytes();
        byte[] GBKByte = str.getBytes("GBK");
        byte[] ISOByte = str.getBytes("ISO-8859-1");
        byte[] UTF8Byte = str.getBytes("UTF-8");
        
        System.out.println("System default charset: " + Charset.defaultCharset());
        System.out.println("System default bytes length: " + defaultByte.length);
        System.out.println("GBK bytes length: " + GBKByte.length);
        System.out.println("ISO-8859-1 bytes length: " + ISOByte.length);
        System.out.println("UTF-8 bytes length: " + UTF8Byte.length);
    }
}

