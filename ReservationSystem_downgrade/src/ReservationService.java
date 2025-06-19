package reservation.service;

import reservation.model.MeetingRoom;
import reservation.model.Reservation;
import reservation.model.User;
import reservation.repository.MeetingRoomRepository;
import reservation.repository.ReservationRepository;
import reservation.util.ErrorCatalog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 予約ロジック：登録・取消・重複判定・上限・所有者判定
 */
public class ReservationService {

    private final MeetingRoomRepository roomRepo;
    private final ReservationRepository resRepo;
    private static final int MAX_RESERVATIONS = 100;
    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public ReservationService(MeetingRoomRepository roomRepo,
                              ReservationRepository resRepo) {
        this.roomRepo = roomRepo;
        this.resRepo  = resRepo;
    }

    /* ---------------- 予約登録 ---------------- */
    public Reservation reserve(String userName,
                               int roomId,
                               long startMs,
                               long endMs) throws Exception {

        if (resRepo.findAll().size() >= MAX_RESERVATIONS)
            throw new IllegalStateException(ErrorCatalog.msg("E003", MAX_RESERVATIONS));

        MeetingRoom room = roomRepo.findById(roomId);
        if (room == null)
            throw new IllegalArgumentException(ErrorCatalog.msg("E001", roomId));

        for (Reservation r : resRepo.findByRoom(roomId)) {
            if (startMs <= r.getEndMillis() && r.getStartMillis() <= endMs)
                throw new IllegalArgumentException(ErrorCatalog.msg("E002", r.getId()));
        }

        int id = resRepo.nextId();
        Reservation res = new Reservation(id, roomId, startMs, endMs, userName);
        resRepo.add(res);
        resRepo.flush();
        return res;
    }

    /* ---------------- キャンセル ---------------- */
    public void cancel(User requester, int resId) throws Exception {
        Reservation r = resRepo.findById(resId);
        if (r == null)
            throw new IllegalArgumentException(ErrorCatalog.msg("E006", resId));

        /* 所有者または管理者のみ許可 */
        if (!requester.isAdmin() && !requester.getName().equals(r.getOwner()))
            throw new IllegalAccessException(ErrorCatalog.msg("E008"));

        resRepo.delete(resId);
        resRepo.flush();
    }

    /* ---------------- 参照系 ---------------- */
    public List<Reservation> list()                   { return resRepo.findAll(); }
    public List<Reservation> listByRoom(int roomId)   { return resRepo.findByRoom(roomId); }

    /* ---------------- 整形出力 ---------------- */
    public String format(Reservation r) {
        MeetingRoom m = roomRepo.findById(r.getRoomId());
        String label  = (m != null) ? m.getName() + "[" + r.getRoomId() + "]"
                                    : "[" + r.getRoomId() + "]";
        return String.format("%d  %s  %s 〜 %s  by %s",
                r.getId(), label,
                DF.format(new Date(r.getStartMillis())),
                DF.format(new Date(r.getEndMillis())),
                r.getOwner());
    }

    public List<String> listPretty() {
        List<String> out = new ArrayList<String>();
        for (Reservation r : resRepo.findAll()) out.add(format(r));
        return out;
    }
    public List<String> listPrettyByRoom(int roomId) {
        List<String> out = new ArrayList<String>();
        for (Reservation r : resRepo.findByRoom(roomId)) out.add(format(r));
        return out;
    }

    /* ---------------- 一括削除 ---------------- */
    public void clearAll() throws Exception { resRepo.clearAll(); }
}
