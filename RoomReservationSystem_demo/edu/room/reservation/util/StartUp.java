package reservation.util;

import java.util.*;

import reservation.service.*;
import reservation.model.*;

public class StartUp {
    private static final UserService userService = new UserService();
    private static final RoomService roomService = new RoomService();
    private static final ReservationService reservationService = new ReservationService();
    private static final Scanner sc = new Scanner(System.in);
    //private static final
    //private static final
    private static User currentUser;
    private static boolean exeMode;
    private StartUp(){}

    public static void menu(){
        exeMode = true;

        while(exeMode){
            if(currentUser == null) login();
            switch(currentUser.getType()){
                case "ADMIN":
                execADMINmenu();
                break;

                case "USER":
                //execUSERmenu();
                break;

                default:
                System.out.println("no defined menu");
            }
        }
    }

    private static void login(){ 
        exeMode = true;
        currentUser = userService.authorize();
        roomService.authorize(currentUser);
        //ReservationService.authorize(currentUser);
    }

    private static void logout() { 
        exeMode = true;
        currentUser = null;
        roomService.authorize(currentUser);
        //ReservationService.authorize(this.currentUser);
    }

    private static void execADMINmenu(){
        String select;
        try{
            while(true){
                System.out.println("1 > ユーザ一覧");
                System.out.println("2 > ユーザ登録");
                System.out.println("3 > ユーザ削除");
                System.out.println("4 > 会議室一覧");
                System.out.println("5 > 会議室登録");
                System.out.println("6 > 会議室削除");
                System.out.println("0 > ログアウト");
                System.out.print(">>"); select = sc.nextLine();
                TroubleShooting.checkString(select);
                int num = Integer.parseInt(select);
                switch(num){
                    case 1: userService.showList(); break;
                    case 2: userService.register(); break;
                    case 3: userService.delete(); break;
                    case 4: roomService.showList(); break;
                    case 5: roomService.register(); break;
                    case 6: roomService.delete(); break;
                    case 0: logout(); return;
                    default: System.out.println("Nothing option"); continue;
                }
            }
        }catch(Exception e){
                System.out.println(e.getMessage());
            }
    }
}
