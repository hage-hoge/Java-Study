package edu.room.driver;

import edu.room.reservation.util.TroubleShooting;
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
            System.out.println(TroubleShooting.checkDateFormat(normal));
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        try{
            System.out.println(TroubleShooting.checkDateFormat(error1));
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        try{
            System.out.println(TroubleShooting.checkDateFormat(error2));
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        try{
            System.out.println(TroubleShooting.checkDateFormat(error2));
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
