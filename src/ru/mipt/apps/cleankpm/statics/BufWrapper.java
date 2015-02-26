package ru.mipt.apps.cleankpm.statics;


import ru.mipt.apps.cleankpm.constants.Config;

/**
 * Created by User on 2/23/2015.
 */
public class BufWrapper {
    public static byte[] setSocketPreBuf(){
        return new byte[Config.socketBufSize];
    }
    public static byte[] convertToBuf(int b){
        byte[] buf = new byte[Config.socketBufSize];
        if ((b>=pow(Config.ByteMax,Config.socketBufSize))||(b<0)){
            System.out.println("wrong parameter in convertToBuf()");
            return null;
        }
        int s=b;
        for (int i=Config.socketBufSize-1; i>=0; i--){
            buf[i]=(byte) (s%Config.ByteMax);
            s/=Config.ByteMax;
        }
        return buf;
    }
    private static int pow(int a,int b){ //a to the b
        int p=1;
        for (int i=0; i<b; i++){
            p*=a;
        }
        return p;
    }
    public static int convertToInt(byte[] buf){
        int l = buf.length;
        int a=0;
        for (int i=0; i<l; i++){
            a*=Config.ByteMax;
            a+=buf[i];
        }
        System.out.println("array converted to "+a);
        return a;
    }
}
