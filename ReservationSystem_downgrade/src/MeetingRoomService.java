package reservation.service;

import reservation.model.MeetingRoom;
import reservation.repository.MeetingRoomRepository;
import reservation.repository.ReservationRepository;
import reservation.util.ErrorCatalog;

import java.util.List;

/**
 * 会議室に関する業務ロジックを集約したサービスクラス。
 *
 * - ID は Repository 側の nextId() で自動採番
 * - 削除時には、その会議室に紐づく予約が残っていないかをチェック
 */
public class MeetingRoomService {

    private final MeetingRoomRepository roomRepo;
    private final ReservationRepository resRepo;

    /** コンストラクタで Repository を注入 */
    public MeetingRoomService(MeetingRoomRepository roomRepo,
                              ReservationRepository resRepo) {
        this.roomRepo = roomRepo;
        this.resRepo  = resRepo;
    }

    /* =====================================================================
       CRUD
       ===================================================================== */

    /** 会議室登録（ID 自動採番） */
    public MeetingRoom register(String name, int capacity) throws Exception {
        /* 同名チェック (大文字小文字区別なし) */
        for (MeetingRoom m : roomRepo.findAll()) {
            if (m.getName().equalsIgnoreCase(name))
                throw new IllegalArgumentException(ErrorCatalog.msg("E010", name));
        }
        int id = roomRepo.nextId();
        MeetingRoom r = new MeetingRoom(id, name, capacity);
        roomRepo.add(r); roomRepo.flush();
        return r;
    }

    /** 会議室更新（名称・定員変更） */
    public void update(MeetingRoom room) throws Exception {
        if (roomRepo.findById(room.getId()) == null)
            throw new IllegalArgumentException("会議室 ID が存在しません");
        roomRepo.update(room);
        roomRepo.flush();
    }

    /** 会議室削除（予約が残っている場合はエラー） */
    public void delete(int id) throws Exception {
        if (!resRepo.findByRoom(id).isEmpty())
            throw new IllegalStateException("予約が残っているため削除できません");
        if (roomRepo.findById(id) == null)
            throw new IllegalArgumentException("会議室 ID が存在しません");
        roomRepo.delete(id);
        roomRepo.flush();
    }

    /* =====================================================================
       参照系
       ===================================================================== */

    public MeetingRoom findById(int id) { return roomRepo.findById(id); }

    public List<MeetingRoom> list() { return roomRepo.findAll(); }
}
