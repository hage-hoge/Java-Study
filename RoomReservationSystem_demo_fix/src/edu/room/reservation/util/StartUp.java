package edu.room.reservation.util;

import java.util.*;

import edu.room.reservation.service.*;
import edu.room.reservation.model.*;
import edu.room.reservation.repository.*;

public class StartUp {
    private static final UserRepository userRepo = new UserRepository();
    private static final RoomRepository roomRepo = new RoomRepository();
    private static final ReservationRepository resRepo = new ReservationRepository();
    private static final UserService userService = new UserService(userRepo);
    private static final RoomService roomService = new RoomService(roomRepo);
    private static final ReservationService reservationService = new ReservationService(resRepo, roomRepo);
    private static final Scanner sc = new Scanner(System.in);
    //private static final
    //private static final
    private static User currentUser;
    private static boolean exeMode;
    private StartUp(){}

    public static void menu(){
        exeMode = true;

        while(exeMode){
            try{
                if(currentUser == null) login();
                switch(currentUser.getType()){
                    case "ADMIN":
                    execADMINmenu();
                    break;

                    case "USER":
                    execUSERmenu();
                    break;

                    default:
                    System.out.println("no defined menu");
                }
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    private static void login() throws Exception{ 
        exeMode = true;
        currentUser = userService.authorize();
        if(currentUser==null) throw new Exception(TroubleShooting.auto(3));
        roomService.authorize(currentUser);
        reservationService.authorize(currentUser);
    }

    private static void logout() { 
        exeMode = true;
        currentUser = null;
        roomService.authorize(currentUser);
        reservationService.authorize(currentUser);
    }

    private static void execADMINmenu() throws Exception{
        String select;
        while(true){
            System.out.println("1 > ユーザ一覧");
            System.out.println("2 > ユーザ登録");
            System.out.println("3 > ユーザ削除");
            System.out.println("4 > 会議室一覧");
            System.out.println("5 > 会議室登録");
            System.out.println("6 > 会議室削除");
            System.out.println("7 > 予約一覧");
            System.out.println("8 > 予約登録");
            System.out.println("9 > 予約削除");
            System.out.println("11 > ロック解除");
            System.out.println("0 > ログアウト");
            System.out.print(">>"); select = sc.nextLine();
            try{
                int num = TroubleShooting.parseMenu(select);
                switch(num){
                    case 1: userService.showList(); break;
                    case 2: userService.register(); break;
                    case 3: userService.delete(); break;
                    case 4: roomService.showList(); break;
                    case 5: roomService.register(); break;
                    case 6: roomService.delete(); break;
                    case 7: reservationService.showList(); break;
                    case 8: reservationService.register(); break;
                    case 9: reservationService.delete(); break;
                    case 11: userService.unLocker(); break;
                    case 0: logout(); return;
                    default: throw new IllegalAccessException(TroubleShooting.auto(2));
                }
            }catch(IllegalAccessException e){
                System.out.println(e.getMessage());
                continue;
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    private static void execUSERmenu(){
        String select;
            while(true){
                System.out.println("1 > 会議室一覧");
                System.out.println("2 > 予約一覧");
                System.out.println("3 > 予約登録");
                System.out.println("4 > 予約削除");
                System.out.println("0 > ログアウト");
                System.out.print(">>"); select = sc.nextLine();
                try{
                    int num = TroubleShooting.parseMenu(select); 
                    switch(num){
                        case 1: roomService.showList(); break;
                        case 2: reservationService.showList(); break;
                        case 3: reservationService.register(); break;
                        case 4: reservationService.delete(); break;
                        case 0: logout(); return;
                        default: throw new IllegalAccessError(TroubleShooting.auto(2));
                    }
                }catch(IllegalAccessError e){
                    System.out.println(e.getMessage());
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
        }
    }
}
