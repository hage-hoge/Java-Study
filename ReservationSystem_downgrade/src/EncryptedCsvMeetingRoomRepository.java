package reservation.repository;

import reservation.model.MeetingRoom;
import reservation.util.CryptoUtil;

import java.io.*;
import java.util.*;

/**
 * 暗号化された CSV ファイルに会議室情報を保存する実装。
 * AES-128 (ECB) でファイル全体を暗号化。
 */
public class EncryptedCsvMeetingRoomRepository implements MeetingRoomRepository {
    private final File file;
    private final Map<Integer, MeetingRoom> cache = new LinkedHashMap<Integer, MeetingRoom>();
    private int nextIdCounter = 1;   // 自動採番用

    public EncryptedCsvMeetingRoomRepository(String path) throws Exception {
        this.file = new File(path);
        if (file.exists()) load();
    }

    /* ====== MeetingRoomRepository 実装 ====== */

    /** 自動採番 */
    @Override
    public synchronized int nextId() { return nextIdCounter++; }

    @Override
    public void add(MeetingRoom r)    { cache.put(r.getId(), r); }
    @Override
    public void update(MeetingRoom r) { cache.put(r.getId(), r); }
    @Override
    public void delete(int id)        { cache.remove(id); }

    @Override
    public List<MeetingRoom> findAll()  { return new ArrayList<MeetingRoom>(cache.values()); }
    @Override
    public MeetingRoom findById(int id) { return cache.get(id); }

    @Override
    public void flush() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (MeetingRoom r : cache.values()) {
            sb.append(r.getId()).append(",")
              .append(escape(r.getName())).append(",")
              .append(r.getCapacity()).append("\n");
        }
        byte[] enc = CryptoUtil.encrypt(sb.toString().getBytes("UTF-8"));

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(enc);
        } finally {
            if (out != null) out.close();
        }
    }

    /* ====== 内部メソッド ====== */

    /** 起動時にファイルを読み込み、cache & 採番カウンタを初期化 */
    private void load() throws Exception {
        byte[] data = readAllBytes(file);
        if (data.length == 0) return;

        String csv = new String(CryptoUtil.decrypt(data), "UTF-8");
        for (String line : csv.split("\n")) {
            if (line.trim().isEmpty()) continue;
            String[] p = parse(line);
            int id  = Integer.parseInt(p[0]);
            String name = p[1];
            int cap = Integer.parseInt(p[2]);
            cache.put(id, new MeetingRoom(id, name, cap));

            // 最大 ID を反映
            if (id >= nextIdCounter) nextIdCounter = id + 1;
        }
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
        } finally {
            if (in != null) in.close();
        }
    }

    /* ---------- CSV エスケープ／パース ---------- */
    private static String escape(String s) {
        return s.contains(",") ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }

    /** 非常に簡易的な CSV パーサ（" を考慮） */
    private static String[] parse(String line) {
        List<String> out = new ArrayList<String>();
        boolean inQuote = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuote && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"'); i++; // "" → "
                } else inQuote = !inQuote;
            } else if (c == ',' && !inQuote) {
                out.add(cur.toString()); cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[out.size()]);
    }
}
