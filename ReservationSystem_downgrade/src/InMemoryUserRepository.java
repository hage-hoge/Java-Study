package reservation.repository;

import reservation.model.*;
import reservation.util.CryptoUtil;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;


public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> map = new LinkedHashMap<String, User>();

    public InMemoryUserRepository() throws Exception {
        createMasterAccount();   // admin / admin
    }

    /* ---------------- CRUD ---------------- */
    @Override
    public void add(User u) { map.put(u.getName(), u); }

    @Override
    public User find(String name) { return map.get(name); }

    @Override
    public List<User> findAll() { return new ArrayList<User>(map.values()); }
    
    @Override
    public void flush () throws Exception{
        return;
    }

    /** 初回起動時: admin / admin を生成 */
    private void createMasterAccount() throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String hash = bytesToHex(md.digest("admin".getBytes("UTF-8")));
        map.put("admin", new User("admin", hash, UserRole.ADMIN));
    }

    private static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}
