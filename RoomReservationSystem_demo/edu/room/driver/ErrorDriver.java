package driver;

import reservation.util.TroubleShooting;
import java.util.*;
public class ErrorDriver {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in, "MS932");
        String normal = "2025/6/27";
        String error1 = "２０２５/０６/２７";
        //String error2 = "あいうえお"; 
        String error2;
        error2 = sc.nextLine(); // 例外スロー失敗

        System.out.println(Arrays.toString(error2.codePoints().toArray()));
        try{
            System.out.println(TroubleShooting.checkString(normal, "DateError"));
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        try{
            System.out.println(TroubleShooting.checkString(error1, "DateError"));
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        try{
            System.out.println(TroubleShooting.checkString(error2, "DateError"));
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        try{
            System.out.println(TroubleShooting.checkString(error2, "StringError"));
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
