package edu.room.driver;

import java.util.HashMap;
import java.util.Map;

import edu.room.reservation.repository.UserRepository;
import edu.room.reservation.service.UserService;
import edu.room.reservation.util.TroubleShooting;

public class UserServiceDriver {

    public static void main(String[] args) {

        /* ① テスト用ユーザ名とパスワード */
        Map<String, String> testUsers = new HashMap<>();
        testUsers.put("u001", "pass");
        testUsers.put("u002", "pass");

        /* ② Repository & Service 準備 */
        UserRepository userRepo = new UserRepository();
        UserService   userSvc   = new UserService(userRepo);

        /* ③ 既存マップ + 上限まで自動登録 */
        int limit = 30;                                     // UserService.MAX_USER と合わせる
        int idSeq = 1;

        // 既存 2 人をまず登録
        for (Map.Entry<String, String> e : testUsers.entrySet()) {
            userRepo.add(e.getKey(), e.getValue(), "USER");
            idSeq++;
        }
        // 残り (limit - 2) 人を自動生成して追加
        while (idSeq <= limit) {
            String id  = String.format("u%03d", idSeq);
            userRepo.add(id, "pass", "USER");
            idSeq++;
        }

        /* ④ 状態確認 */
        System.out.println("--- 登録済みユーザ一覧 (上限に到達) ---");
        userSvc.showList();                // 上限いっぱい表示される

        /* ⑤ さらに 1 人 登録 (=6 人目) を試みて上限エラーを検証 */
        System.out.println("--- もう 1 人登録を試みます ---");
        try {
            userSvc.register();            // checkRepository → IllegalStateException → 204
        } catch (Exception ex) {
            System.out.println("例外捕捉: " + ex.getMessage());
        }

        /* ⑥ 最終的なリスト表示 (件数は変わらないことを確認) */
        System.out.println("--- 最終ユーザ一覧 (件数は変わらず) ---");
        userSvc.showList();
    }
}