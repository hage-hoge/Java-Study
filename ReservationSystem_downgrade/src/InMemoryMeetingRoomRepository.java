package reservation.repository;

import reservation.model.MeetingRoom;
import reservation.model.Reservation;
import reservation.util.CryptoUtil;

import java.io.*;
import java.util.*;

/**
 * 暗号化された CSV ファイルに会議室情報を保存する実装。
 * AES-128 (ECB) でファイル全体を暗号化。
 */
public class InMemoryMeetingRoomRepository implements MeetingRoomRepository {
    private final Map<Integer, MeetingRoom> map = new LinkedHashMap<Integer, MeetingRoom>();
    private int nextIdCounter = 1;   // 自動採番用

    public InMemoryMeetingRoomRepository() throws Exception {}

    /* ====== MeetingRoomRepository 実装 ====== */

    /** 自動採番 */
    @Override
    public synchronized int nextId() { return nextIdCounter++; }

    @Override
    public void add(MeetingRoom r)    { map.put(r.getId(), r); }
    @Override
    public void update(MeetingRoom r) { map.put(r.getId(), r); }
    @Override
    public void delete(int id)        { map.remove(id); }

    @Override
    public List<MeetingRoom> findAll()  { return new ArrayList<MeetingRoom>(map.values()); }
    @Override
    public MeetingRoom findById(int id) { return map.get(id); }


    @Override
    public void flush() throws Exception {
        return;
    }
}
