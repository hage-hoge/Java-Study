package reservation.repository;

import reservation.model.*;
import reservation.util.CryptoUtil;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;

/**
 * ユーザ情報を AES(ECB) 暗号化 CSV で保存・復元する実装。
 * CSV フォーマット: name,hash,role,failed,locked
 * 起動時にファイルが無ければ admin/admin を自動作成。
 */
public class EncryptedCsvUserRepository implements UserRepository {

    private final File file;
    private final Map<String, User> map = new LinkedHashMap<String, User>();

    public EncryptedCsvUserRepository(String path) throws Exception {
        file = new File(path);
        if (file.exists()) load();
        else createMasterAccount();   // admin / admin
    }

    /* ---------------- CRUD ---------------- */
    @Override
    public void add(User u) { map.put(u.getName(), u); }

    @Override
    public User find(String name) { return map.get(name); }

    @Override
    public List<User> findAll() { return new ArrayList<User>(map.values()); }

    @Override
    public void flush() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (User u : map.values()) {
            sb.append(u.getName()).append(',')
              .append(u.getHash()).append(',')
              .append(u.getRole()).append(',')
              .append(u.getFailed()).append(',')
              .append(u.isLocked()).append('\n');
        }
        byte[] enc = CryptoUtil.encrypt(sb.toString().getBytes("UTF-8"));
        FileOutputStream out = new FileOutputStream(file);
        out.write(enc);
        out.close();
    }

    /* ---------------- private ---------------- */
    private void load() throws Exception {
        byte[] dec = CryptoUtil.decrypt(readAllBytes(file));
        String csv = new String(dec, "UTF-8");
        for (String line : csv.split("\n")) {
            if (line.trim().isEmpty()) continue;
            String[] p = line.split(",", 5);
            User u = new User(
                    p[0],
                    p[1],
                    UserRole.valueOf(p[2]),
                    Integer.parseInt(p[3]),
                    Boolean.parseBoolean(p[4]));
            map.put(u.getName(), u);
        }
    }

    /** 初回起動時: admin / admin を生成 */
    private void createMasterAccount() throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String hash = bytesToHex(md.digest("admin".getBytes("UTF-8")));
        map.put("admin", new User("admin", hash, UserRole.ADMIN));
        flush();
    }

    private static byte[] readAllBytes(File f) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileInputStream in = new FileInputStream(f);
        byte[] buf = new byte[4096];
        int len;
        while ((len = in.read(buf)) != -1) bos.write(buf, 0, len);
        in.close();
        return bos.toByteArray();
    }

    private static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}
