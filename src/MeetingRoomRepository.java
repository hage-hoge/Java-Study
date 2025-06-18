package reservation.repository;

import reservation.model.MeetingRoom;
import java.util.List;

/** 永続化層インタフェース */
public interface MeetingRoomRepository {
    /** 次に割り当てる ID（最大 ID + 1）を返す */
    int nextId();

    void add(MeetingRoom r);
    void update(MeetingRoom r);
    void delete(int id);

    List<MeetingRoom> findAll();
    MeetingRoom findById(int id);

    /** メモリ上の変更をストレージに書き出す */
    void flush() throws Exception;
}
