package edu.room.driver;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.room.reservation.model.Room;
import edu.room.reservation.repository.*;
import edu.room.reservation.service.ReservationService;
import edu.room.user.model.User;
import edu.room.user.repository.UserRepository;

public class ReservationServiceDriver {

    public static void main(String[] args) {

        /* -------------------------------
         * 0) テスト用ユーザ / 会議室を準備
         * ----------------------------- */
        UserRepository userRepo = new UserRepository();
        userRepo.add("u001", "pass", "USER");
        User tester = userRepo.getMap().get("u001");

        RoomRepository roomRepo = new RoomRepository();
        roomRepo.add(1, "Room-A", 10);
        roomRepo.add(2, "Room-B", 12);
        roomRepo.add(3, "Room-C",  8);

        /* -------------------------------
         * 1) Service 初期化
         * ----------------------------- */
        ReservationRepository reservRepo = new ReservationRepository();
        ReservationService   reservSvc  = new ReservationService(reservRepo, roomRepo);
        reservSvc.authorize(tester);

        /* -------------------------------
         * 2) 上限(MAX_RESERVE)まで自動登録
         * ----------------------------- */
        final int LIMIT = 30;                          // ReservationService.MAX_RESERVE に合わせる
        Calendar cal   = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);                     // 明日から登録開始
        SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");

        int seq = 0;
        while (seq < LIMIT) {
            /* 2-1) 日付を作成（9 件ごとに翌日） */
            if (seq != 0 && seq % 9 == 0) cal.add(Calendar.DATE, 1);
            String dateStr = DF.format(cal.getTime());  // 例: 2025-07-01

            /* 2-2) 会議室をローテーション */
            int roomIndex = (seq % 3) + 1;              // 1,2,3 → Room-A/B/C
            Room room = roomRepo.getMap().get(roomIndex);

            /* 2-3) 開始・終了時間 09:00〜17:00 を 1h 刻みで割振り */
            int hour = 9 + (seq % 9);                   // 09,10,…17
            String start = String.format("%02d:00", hour);
            String end   = String.format("%02d:00", hour + 1);

            /* 2-4) registerAuto() で登録 */
            try {
                reservSvc.registerAuto(dateStr, room, start, end, tester);
            } catch (Exception ex) {
                System.err.println("登録失敗: " + ex.getMessage());
                return;
            }
            seq++;
        }

        /* -------------------------------
         * 3) 上限到達時点の一覧表示
         * ----------------------------- */
        System.out.println("\n--- 上限到達時点 ---");
        reservSvc.showList();

        /* -------------------------------
         * 4) さらに 1 件登録して上限エラー確認
         * ----------------------------- */
        System.out.println("\n--- 上限 +1 件登録テスト ---");
        try {
            // 上で使わなかった 18:00–19:00 を適当な未来日に入れてみる
            cal.add(Calendar.DATE, 2);
            String future = DF.format(cal.getTime());
            reservSvc.registerAuto(future,
                                   roomRepo.getMap().get(1),
                                   "18:00", "19:00",
                                   tester);
        } catch (Exception ex) {
            System.out.println("期待通り例外捕捉: " + ex.getMessage());
        }

        /* -------------------------------
         * 5) 最終一覧（件数変わらず）
         * ----------------------------- */
        System.out.println("\n--- 最終一覧 ---");
        reservSvc.showList();
    }
}
