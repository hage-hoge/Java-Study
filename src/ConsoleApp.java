package reservation;

import reservation.model.User;
import reservation.repository.*;
import reservation.service.*;
import reservation.ui.*;

import java.util.Scanner;

/**
 * コンソール版 会議室予約システム
 *  - ログイン / ログアウト
 *  - 会議室・予約・ユーザ管理メニュー
 */
public class ConsoleApp {

    /* 共通メッセージ */
    private static void deny() { System.out.println("権限がありません"); }

    public static void main(String[] args) throws Exception {

        /* ========== Repository / Service ========== */
        MeetingRoomRepository roomRepo = new EncryptedCsvRepository("meetingrooms.dat");
        ReservationRepository resRepo  = new EncryptedCsvReservationRepository("reservations.dat");
        UserRepository        userRepo = new EncryptedCsvUserRepository("users.dat");

        AuthService        authSvc = new AuthService(userRepo);
        ReservationService resSvc  = new ReservationService(roomRepo, resRepo);
        MeetingRoomService roomSvc = new MeetingRoomService(roomRepo, resRepo);

        /* ========== 共通 UI オブジェクト ========== */
        Scanner         sc      = new Scanner(System.in, "UTF-8");
        AuthMenu        authMenu= new AuthMenu(sc, authSvc, userRepo);
        SessionService  session = new SessionService(authSvc);

        /* ========== アプリ全体ループ ========== */
        while (true) {

            /* ----- 1) ログイン ----- */
            session.loginLoop(sc);               // 成功するまで戻らない
            User current = session.getUser();

            /* ----- 2) サブメニュー生成 ----- */
            Menu roomMenu = new MeetingRoomMenu(sc, roomSvc, session.isAdmin());

            /* ★ ReservationMenu に SessionService を渡す */
            Menu resMenu  = new ReservationMenu(sc, resSvc, roomSvc, session);

            /* ----- 3) メインメニュー ----- */
            boolean run = true;
            while (run) {
                System.out.println("\n=== メインメニュー ===");
                System.out.println(" 1. 会議室メニュー");
                System.out.println(" 2. 予約メニュー");
                if (session.isAdmin()) {
                    System.out.println(" 3. ユーザ登録");
                    System.out.println(" 4. ユーザ一覧");
                    System.out.println(" 5. ロック解除");
                }
                System.out.println(" 8. ログアウト");
                System.out.println(" 9. 終了");

                int sel;
                try { sel = Integer.parseInt(sc.nextLine().trim()); }
                catch (NumberFormatException ex) { System.out.println("数字を入力してください"); continue; }

                switch (sel) {
                    case 1: roomMenu.run(); break;
                    case 2: resMenu.run();  break;

                    case 3:                               // ユーザ登録
                        if (session.isAdmin()) authMenu.adminAddUser(current);
                        else deny();
                        break;

                    case 4:                               // ユーザ一覧
                        if (session.isAdmin()) authMenu.adminListUsers(current);
                        else deny();
                        break;

                    case 5:                               // ロック解除
                        if (session.isAdmin()) authMenu.adminUnlockUser(current);
                        else deny();
                        break;

                    case 8:                               // ログアウト
                        session.logout();
                        run = false;                       // 内側ループを抜け→再ログイン
                        break;

                    case 9:                               // 終了
                        System.out.println("終了します");
                        return;

                    default:
                        System.out.println("無効な選択肢です");
                }
            }
        }
    }
}
