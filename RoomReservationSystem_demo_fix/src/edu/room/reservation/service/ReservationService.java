package edu.room.reservation.service;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

import edu.room.reservation.model.*;
import edu.room.reservation.repository.*;
import edu.room.reservation.util.TroubleShooting;

/**
 * ReservationService – 会議室予約の一覧 / 登録 / 削除 / 空き枠照会を担当するサービス層。
 * CALLER_ID = 4  （TroubleShooting 側で 4xx エラーコードを生成）
 */
public class ReservationService {

    private static final int MAX_RESERVE = 100;     
    /* ---------- Repository ---------- */
    private final ReservationRepository resRepo;
    private final RoomRepository roomRepo;    

    /* ---------- Session / I/O ---------- */
    private User currentUser;
    private final Scanner sc = new Scanner(System.in, "MS932");

    /* ---------- Date / Time Format ---------- */
    private final SimpleDateFormat DF = new SimpleDateFormat("yyyy/MM/dd");
    private final SimpleDateFormat TF = new SimpleDateFormat("HH:mm");

    public ReservationService(ReservationRepository resRepo, RoomRepository roomRepo) {
        this.resRepo = resRepo;
        this.roomRepo = roomRepo;
        DF.setLenient(false);
        TF.setLenient(false);
    }

    /* ==================== 認証 ==================== */
    public void authorize(User loginUser) { this.currentUser = loginUser; }

    /* ==================== 一覧表示 ==================== */
    /* ==================== 一覧表示 ==================== */
/**
 * showList()
 *  ├─ 予約時系列順で一覧
 *  └─ 会議室別一覧
 *
 * 利用者にどちらで表示するか選択させる。
 * ※どちらも Repository が空なら 422 エラーを投げる。
 */
public void showList() {
    try {
        TroubleShooting.checkRepository(resRepo);               // 422

        System.out.print(
            "\n0> 予約順 \n1> 会議室別");
        System.out.print(">> "); String mode = sc.nextLine().trim();
        switch (mode) {
            case "0": showListChronological();   // 予約順
            case "1": showListByRoom();          // 会議室別
            default : throw new Exception(TroubleShooting.auto(14));
        }
    } catch (Exception e) {
        System.out.println(e.getMessage());
    }
}

/* ===== 予約日時順 ===== */
private void showListChronological() {
    List<Reservation> list = new ArrayList<>(resRepo.getMap().values());
    list.sort((a, b) -> {
        int d = a.getDate().compareTo(b.getDate());              // 日付昇順
        if (d != 0) return d;
        d = a.getStartTime().compareTo(b.getStartTime());        // 開始時刻昇順
        if (d != 0) return d;
        return a.getRoom().getName().compareTo(b.getRoom().getName());
    });

    System.out.println("予約ID, 会議室, 日付, 開始 - 終了");
    System.out.println("===================================================");
    list.forEach(r -> System.out.printf(
        "%d, %s, %s, %s - %s%n",
        r.getId(), r.getRoom().getName(),
        r.getDate(), r.getStartTime(), r.getEndTime()));
    System.out.println("===================================================\n");
}

/* ===== 会議室別 ===== */
private void showListByRoom() {
    // Room 名で昇順ソート
    roomRepo.getMap().values().stream()
            .sorted(Comparator.comparing(Room::getName))
            .forEach(room -> {
                // 該当ルームの予約を抽出
                List<Reservation> rs = resRepo.getMap().values().stream()
                        .filter(r -> r.getRoom().getId() == room.getId())
                        .sorted(Comparator
                                .comparing(Reservation::getDate)
                                .thenComparing(Reservation::getStartTime))
                        .toList();
                if (rs.isEmpty()) return;  // 予約が無い部屋はスキップ

                System.out.println("\n■ " + room.getName());
                System.out.println("予約ID, 日付, 開始 - 終了, 予約者");
                rs.forEach(r -> System.out.printf(
                    "%d, %s, %s - %s, %s%n",
                    r.getId(), r.getDate(),
                    r.getStartTime(), r.getEndTime(),
                    r.getOwner().getName()));
            });
    System.out.println();
}
    /* ==================== 予約登録 ==================== */
     public void register() {
        try {
            /* 0) 上限チェック */
            TroubleShooting.checkRepository(resRepo, MAX_RESERVE); // 410

            /* 1) 日付入力 */
            System.out.print("日付(YYYY/MM/DD or YYYY-MM-DD)> ");
            String dateIn = sc.nextLine();
            // 全角数字・ハイフンを許容しつつ正規化 (例: ２０２５−９−７ → 2025/09/07)
            String dateStr = TroubleShooting.checkDateFormat(dateIn); // 401,408 内部で処理
            checkNotPast(dateStr);

            /* 2) 会議室入力 */
            System.out.print("会議室名> ");
            String roomName = sc.nextLine();
            TroubleShooting.checkString(roomName);
            Room room = findRoomByName(roomName);
            if (room == null) throw new Exception(TroubleShooting.auto(5)); // 405

            /* 3) 指定日の空き枠 */
            showVacancyForRoom(dateStr, room);

            /* 4) 開始・終了（柔軟入力を正規化） */
            System.out.print("開始時刻(HH:mm)> "); String stRaw = sc.nextLine();
            System.out.print("終了時刻(HH:mm)> "); String edRaw = sc.nextLine();
            String st = TroubleShooting.normalizeHHmm(stRaw, 2); // 402
            String ed = TroubleShooting.normalizeHHmm(edRaw, 3); // 403
            ensureStartBeforeEnd(st, ed);                        // 407

            /* 5) 重複 */
            if (isOverlap(room.getId(), dateStr, st, ed))
                throw new Exception(TroubleShooting.auto(9));    // 409

            /* 6) 登録 */
            resRepo.add(dateStr, st, ed, room, currentUser);
            System.out.println("予約を登録しました");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void ensureStartBeforeEnd(String st, String ed) throws Exception {
        Date s = TF.parse(st);
        Date e = TF.parse(ed);
        if (!s.before(e)) throw new Exception(TroubleShooting.auto(7));
    }


    /* ==================== 予約削除 ==================== */
    public void delete() {
        try {
            showList();
            System.out.print("予約ID> ");
            String idStr = sc.nextLine();
            int id;
            try { id = Integer.parseInt(idStr); }
            catch (NumberFormatException ex) { throw new Exception(TroubleShooting.auto(11)); }

            Reservation res = resRepo.getMap().get(id);
            if (res == null) throw new Exception(TroubleShooting.auto(12)); // 412
            if (!res.getOwner().equals(currentUser))
                throw new Exception(TroubleShooting.auto(13));             // 413

            if (TroubleShooting.confirmDelete()) {
                resRepo.remove(id);
                System.out.println("予約を取消しました");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
        /* ==================== 内部ユーティリティ ==================== */

    /** 会議室名 → Room (線形探索) */
    private Room findRoomByName(String name) {
        return roomRepo.getMap().values().stream()
                .filter(r -> r.getName().equals(name))
                .findFirst().orElse(null);
    }

    /** 指定日の空き枠(1h単位)を表示 */
    private void showVacancyForRoom(String date, Room room) {
        boolean[] busy = new boolean[24];
        resRepo.getMap().values().stream()
               .filter(r -> r.getRoom().getId()==room.getId() && r.getDate().equals(date))
               .forEach(r -> markBusy(busy, r.getStartTime(), r.getEndTime()));

        List<String> slots = collectVacantSlots(busy);
        System.out.println("\n【" + room.getName() + " の空き枠(" + date + ")】");
        if (slots.isEmpty()) System.out.println("  － 空きなし －");
        else slots.forEach(s -> System.out.println("  " + s));
        System.out.println();
    }

    private void markBusy(boolean[] busy, String st, String ed) {
        Date s = TF.parse(st, new ParsePosition(0));
        Date e = TF.parse(ed, new ParsePosition(0));
        if (s == null || e == null) return;
        Calendar c = Calendar.getInstance();
        c.setTime(s); int hS = c.get(Calendar.HOUR_OF_DAY);
        c.setTime(e); int hE = c.get(Calendar.HOUR_OF_DAY);
        if (c.get(Calendar.MINUTE) != 0) hE++;
        hE = Math.min(hE, 24);
        for (int h = hS; h < hE; h++) busy[h] = true;
    }

    private List<String> collectVacantSlots(boolean[] busy) {
        List<String> out = new ArrayList<>();
        int h = 0;
        while (h < 24) {
            if (busy[h]) { h++; continue; }
            int start = h;
            while (h < 24 && !busy[h]) h++;
            out.add(String.format("%02d:00-%02d:00", start, h));
        }
        return out;
    }

    private boolean isOverlap(int roomId, String date, String st, String ed) {
        Date sN = TF.parse(st, new ParsePosition(0));
        Date eN = TF.parse(ed, new ParsePosition(0));
        return resRepo.getMap().values().stream()
                .filter(r -> r.getRoom().getId()==roomId && r.getDate().equals(date))
                .anyMatch(r -> {
                    Date sO = TF.parse(r.getStartTime(), new ParsePosition(0));
                    Date eO = TF.parse(r.getEndTime(),   new ParsePosition(0));
                    return sN.before(eO) && sO.before(eN);
                });
    }

    private void checkNotPast(String dateStr) throws Exception {
        try {
            Date d = DF.parse(dateStr);
            if (d.before(startOfToday())) throw new Exception(TroubleShooting.auto(8)); // 408
        } catch (ParseException ex) {
            throw new Exception(TroubleShooting.auto(1)); // 401
        }
    }

    private Date startOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private void validateTimePair(String stRaw, String edRaw) throws Exception {
        String st = TroubleShooting.normalizeHHmm(stRaw, 2); // 402
        String ed = TroubleShooting.normalizeHHmm(edRaw, 3); // 403

        Date s = TF.parse(st);
        Date e = TF.parse(ed);
        if (!s.before(e))
            throw new Exception(TroubleShooting.auto(7));    // 407 (開始 >= 終了)
    }

    private int invalidWhich(String st, String ed) {
        return (!st.matches("\\\\d{2}:\\\\d{2}")) ? 2 : 3; // 402 or 403
    }
}
