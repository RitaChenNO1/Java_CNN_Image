package util;

/**
 * Created by chzhenzh on 5/24/2016.
 */
public  class Log {
    public static void i(String msg){
        System.out.println(msg);
    }
    public  static void i(String tag, String msg){
        System.out.println(tag+"\t"+msg);
    }
    public static void err(String msg){
        throw new RuntimeException(msg);
    }
}
