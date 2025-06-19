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
public class EncryptedCsvReservationRepository implements ReservationRepository {
    private final File file;
    private final Map<Integer, Reservation> cache = new LinkedHashMap<Integer, Reservation>();
    private int nextIdCounter = 1;
    private final Queue<Integer> recycledIds = new LinkedList<Integer>();

    public EncryptedCsvReservationRepository(String path) throws Exception {
        this.file = new File(path);
        if (file.exists()) load();
    }

    // ----- 採番 -----
    @Override public synchronized int nextId() { return nextIdCounter++; }

    // ----- CRUD -----
    @Override public void add(Reservation r)    { cache.put(r.getId(), r); }
    @Override public void update(Reservation r) { cache.put(r.getId(), r); }
    @Override public void delete(int id)        { cache.remove(id); }

    @Override public List<Reservation> findAll()  { return new ArrayList<Reservation>(cache.values()); }
    @Override public Reservation findById(int id) { return cache.get(id); }

    @Override public List<Reservation> findByRoom(int roomId) {
        List<Reservation> list = new ArrayList<Reservation>();
        for (Reservation r : cache.values())
            if (r.getRoomId() == roomId) list.add(r);
        return list;
    }

    /* ---- 読み込み (owner 列を追加) ---- */
    private void load() throws Exception {
        byte[] dec = CryptoUtil.decrypt(readAllBytes(file));
        String csv = new String(dec, "UTF-8");
        for (String line : csv.split("\n")) {
            if (line.trim().isEmpty()) continue;
            String[] p = line.split(",", 6); // id,room,start,end,owner
            Reservation r = new Reservation(
                    Integer.parseInt(p[0]),
                    Integer.parseInt(p[1]),
                    Long.parseLong(p[2]),
                    Long.parseLong(p[3]),
                    p.length >= 5 ? p[4] : "unknown");
            cache.put(r.getId(), r);
        }
    }

    /* ---- 書き込み ---- */
    public void flush() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Reservation r : cache.values()) {
            sb.append(r.getId()).append(',')
            .append(r.getRoomId()).append(',')
            .append(r.getStartMillis()).append(',')
            .append(r.getEndMillis()).append(',')
            .append(r.getOwner()).append('\n');
        }
        byte[] enc = CryptoUtil.encrypt(sb.toString().getBytes("UTF-8"));
        FileOutputStream out = new FileOutputStream(file);
        out.write(enc); out.close();
    }
    @Override
    public synchronized void clearAll() throws Exception {
        cache.clear();
        recycledIds.clear();
        nextIdCounter = 1;
        flush();                             // 空ファイルを書き戻す
    }

    private static byte[] readAllBytes(File f) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileInputStream in = null;
        try {
            in = new FileInputStream(f);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) bos.write(buf, 0, len);
            return bos.toByteArray();
        } finally { if (in != null) in.close(); }
    }
}
