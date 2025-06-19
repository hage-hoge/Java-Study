package reservation.repository;

import reservation.model.Reservation;
import reservation.util.CryptoUtil;
import reservation.util.TablePrinter;  

import java.io.*;
import java.util.*;

/**
 * 予約データを暗号化 CSV (AES/ECB) で保存する実装。
 * フォーマット: id,roomId,startMillis,endMillis
 */
public class InMemoryReservationRepository implements ReservationRepository {
    private final Map<Integer, Reservation> map = new LinkedHashMap<Integer, Reservation>();
    private int nextIdCounter = 1;


    public InMemoryReservationRepository() throws Exception {}

    // ----- 採番 -----
    @Override public synchronized int nextId() { return nextIdCounter++; }

    // ----- CRUD -----
    @Override public void add(Reservation r)    { map.put(r.getId(), r); }
    @Override public void update(Reservation r) { map.put(r.getId(), r); }
    @Override public void delete(int id)        { map.remove(id); }

    @Override public List<Reservation> findAll()  { return new ArrayList<Reservation>(map.values()); }
    @Override public Reservation findById(int id) { return map.get(id); }

    @Override public List<Reservation> findByRoom(int roomId) {
        List<Reservation> list = new ArrayList<Reservation>();
        for (Reservation r : map.values())
            if (r.getRoomId() == roomId) list.add(r);
        return list;
    }
    
    @Override
    public void flush () throws Exception{
        return;
    }

    @Override
    public void clearAll () throws Exception{
        return;
    }
}
