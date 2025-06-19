package reservation.ui;

import reservation.model.MeetingRoom;
import reservation.model.Reservation;
import reservation.service.MeetingRoomService;
import reservation.service.ReservationService;
import reservation.service.SessionService;
import reservation.util.ErrorCatalog;
import reservation.util.TablePrinter;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Scanner;

/**
 * 予約メニュー
 *   1. 予約登録
 *   2. 一覧
 *   3. キャンセル（自分の予約のみ／管理者は全予約可）
 *  10. 全削除（管理者）
 */
public class ReservationMenu implements Menu {

    private final Scanner sc;
    private final ReservationService resSvc;
    private final MeetingRoomService roomSvc;
    private final SessionService session;
    private final boolean admin;

    public ReservationMenu(Scanner sc,
                           ReservationService resSvc,
                           MeetingRoomService roomSvc,
                           SessionService session) {
        this.sc = sc;
        this.resSvc = resSvc;
        this.roomSvc = roomSvc;
        this.session = session;
        this.admin = session.isAdmin();
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("\n--- 予約メニュー ---");
            if (admin)
                System.out.println(" 1. 予約登録  2. 一覧  3. キャンセル  10. 全削除  0. 戻る");
            else
                System.out.println(" 1. 予約登録  2. 一覧  3. キャンセル  0. 戻る");

            int sel = readInt("選択: ");
            switch (sel) {
                case 1: reserve();      break;
                case 2: listAll();      break;
                case 3: cancel();       break;
                case 10: if (admin) clear(); else deny(); break;
                case 0: return;
                default: System.out.println("無効な選択肢です");
            }
        }
    }

    @Override
    public void select_run(int num){
        switch (num) {
            case 2: listAll();      break;
            case 3: reserve();      break;
            case 4: cancel();       break;
            default: System.out.println("無効な選択肢です");
        }
    }

    /* ================================================================
       1) 予約登録
       ================================================================ */
    private void reserve() {
        /* ① 会議室一覧表示 */
        List<MeetingRoom> rooms = roomSvc.list();
        if (rooms.isEmpty()) { System.out.println("登録済み会議室がありません"); return; }

        List<String[]> roomRows = new ArrayList<String[]>();
        for (MeetingRoom m : rooms)
            roomRows.add(new String[]{
                    String.valueOf(m.getId()), m.getName(), String.valueOf(m.getCapacity())
            });
        TablePrinter.print(new String[]{"ID","名称","定員"}, roomRows);

        /* ② 会議室 ID 入力 + 存在チェック */
        int roomId = readInt("予約する会議室 ID: ");
        if (roomSvc.findById(roomId) == null) {
            System.out.println(ErrorCatalog.msg("E001", roomId));
            return;
        }

        /* ③ 対象会議室の直近予約を表示 */
        showRecentReservation(roomId);

        /* ④ 日時入力 */
        String date = readLine("日付 (yyyy/MM/dd): ");
        String from = readLine("開始時刻 (HH:mm): ");
        String to   = readLine("終了時刻 (HH:mm): ");

        long st, ed;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            st = df.parse(date + " " + from).getTime();
            ed = df.parse(date + " " + to  ).getTime();
        } catch (Exception ex) {
            System.out.println(ErrorCatalog.msg("E007"));
            return;
        }

        /* ⑤ 登録 */
        try {
            String owner = session.getUser().getName();
            Reservation r = resSvc.reserve(owner, roomId, st, ed);
            System.out.println("予約完了: " + resSvc.format(r));
        } catch (Exception ex) {
            System.out.println("エラー: " + ex.getMessage());
        }
    }

    /* 会議室ごとの直近 5 件 */
    private void showRecentReservation(int roomId) {
        List<Reservation> list = resSvc.listByRoom(roomId);
        if (list.isEmpty()) { System.out.println("< 直近の予約 > なし"); return; }

        Collections.sort(list, new Comparator<Reservation>() {
            public int compare(Reservation a, Reservation b) {
                return Long.valueOf(b.getStartMillis()).compareTo(a.getStartMillis());
            }
        });

        List<String[]> rows = new ArrayList<String[]>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            Reservation r = list.get(i);
            rows.add(new String[]{
                String.valueOf(r.getId()),
                df.format(new Date(r.getStartMillis())),
                "〜",
                df.format(new Date(r.getEndMillis())),
                r.getOwner()
            });
        }
        System.out.println("\n< 直近の予約 (会議室ID=" + roomId + ") >");
        TablePrinter.print(new String[]{"ID","開始","", "終了","ユーザ"}, rows);
    }

    /* ================================================================
       2) 一覧表示（全件）
       ================================================================ */
    private void listAll() {
        List<String> lines = resSvc.listPretty();
        if (lines.isEmpty()) System.out.println("予約なし");
        else lines.forEach(System.out::println);
    }

    /* ================================================================
       3) キャンセル
       ================================================================ */
    private void cancel() {
        /* 一覧表示：管理者＝全予約、一般＝自分のみ */
        List<Reservation> all = admin ? resSvc.list()
                                      : filterByOwner(resSvc.list(), session.getUser().getName());
        if (all.isEmpty()) {
            System.out.println("キャンセル可能な予約がありません");
            return;
        }
        for (Reservation r : all) System.out.println(resSvc.format(r));

        /* キャンセル ID 入力 */
        try {
            int id = readInt("キャンセルする予約 ID: ");
            resSvc.cancel(session.getUser(), id);
            System.out.println("キャンセルしました");
        } catch (Exception ex) {
            System.out.println("エラー: " + ex.getMessage());
        }
    }

    private List<Reservation> filterByOwner(List<Reservation> src, String owner) {
        List<Reservation> out = new ArrayList<Reservation>();
        for (Reservation r : src)
            if (owner.equals(r.getOwner())) out.add(r);
        return out;
    }

    /* ================================================================
       10) 全削除（管理者）
       ================================================================ */
    private void clear() {
        if (!"y".equalsIgnoreCase(readLine("すべて削除(y/n): "))) return;
        try {
            resSvc.clearAll();
            System.out.println("全予約を削除しました");
        } catch (Exception ex) {
            System.out.println("エラー: " + ex.getMessage());
        }
    }

    /* ================================================================
       入力ヘルパ
       ================================================================ */
    private String readLine(String m) { System.out.print(m); return sc.nextLine(); }
    private int readInt(String m) {
        while (true) {
            try { return Integer.parseInt(readLine(m)); }
            catch (NumberFormatException e) { System.out.println(ErrorCatalog.msg("E007")); }
        }
    }
    private void deny() { System.out.println(ErrorCatalog.msg("E005")); }
}
