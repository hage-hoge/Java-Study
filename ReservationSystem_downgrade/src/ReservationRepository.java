package reservation.repository;

import reservation.model.Reservation;
import java.util.List;

public interface ReservationRepository {
    int nextId();

    void add(Reservation r);
    void update(Reservation r);
    void delete(int id);
    void clearAll() throws Exception;
    
    List<Reservation> findAll();
    Reservation findById(int id);

    /** 指定会議室の予約一覧 */
    List<Reservation> findByRoom(int roomId);

    void flush() throws Exception;
}
