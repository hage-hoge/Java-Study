package reservation.ui;

import reservation.model.MeetingRoom;
import reservation.service.MeetingRoomService;
import reservation.util.ErrorCatalog;            // ★ 追加

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MeetingRoomMenu implements Menu {

    private final Scanner sc;
    private final MeetingRoomService roomSvc;
    private final boolean admin;

    public MeetingRoomMenu(Scanner sc,
                           MeetingRoomService roomSvc,
                           boolean admin) {
        this.sc   = sc;
        this.roomSvc = roomSvc;
        this.admin  = admin;
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("\n--- 会議室メニュー ---");
            if (admin)
                System.out.println(" 1. 登録  2. 更新  3. 削除  4. 一覧  0. 戻る");
            else
                System.out.println(" 4. 一覧  0. 戻る");

            int sel = readInt("選択: ");
            if (!admin && sel != 4 && sel != 0) {
                System.out.println(ErrorCatalog.msg("E005"));   // 権限なし
                continue;
            }
            switch (sel) {
                case 1: if (admin) register(); break;
                case 2: if (admin) update();   break;
                case 3: if (admin) delete();   break;
                case 4: list(); break;
                case 0: return;
                default: System.out.println("無効な選択肢です");
            }
        }
    }

    /* =================== 登録 =================== */
    private void register() {
        List<MeetingRoom> all = roomSvc.list();
        if (!all.isEmpty()) {
            System.out.println("\n< 最近登録された会議室 >");
            List<String[]> rows = new ArrayList<String[]>();
            int from = Math.max(0, all.size() - 5);
            for (int i = from; i < all.size(); i++) {
                MeetingRoom m = all.get(i);
                rows.add(new String[]{
                        String.valueOf(m.getId()),
                        m.getName(),
                        String.valueOf(m.getCapacity())});
            }
            TablePrinter.print(new String[]{"ID", "名称", "定員"}, rows);
        }

        try {
            String name = readLine("名称: ");
            int cap     = readInt("定員: ");
            System.out.println("登録完了: " +
                    roomSvc.register(name, cap));
        } catch (Exception ex) {
            System.out.println("エラー: " + ex.getMessage());
        }
    }

    /* =================== 更新 =================== */
    private void update() {
        try {
            int id = readInt("更新 ID: ");
            MeetingRoom old = roomSvc.findById(id);
            if (old == null) {
                System.out.println(ErrorCatalog.msg("E001", id)); // 会議室不存在
                return;
            }
            String name = readLine("名称(" + old.getName() + "): ");
            String cs   = readLine("定員(" + old.getCapacity() + "): ");

            roomSvc.update(new MeetingRoom(
                    id,
                    name.trim().isEmpty() ? old.getName() : name,
                    cs.trim().isEmpty() ? old.getCapacity()
                                        : Integer.parseInt(cs)));
            System.out.println("更新完了");
        } catch (Exception ex) {
            System.out.println("エラー: " + ex.getMessage());
        }
    }

    /* =================== 削除 =================== */
    private void delete() {
        try {
            roomSvc.delete(readInt("削除 ID: "));
            System.out.println("削除しました");
        } catch (Exception ex) {
            System.out.println("エラー: " + ex.getMessage());
        }
    }

    /* =================== 一覧 =================== */
    private void list() {
        String inp = readLine("会議室 ID を入力（空欄＝全部）: ").trim();
        if (inp.isEmpty()) {
            List<MeetingRoom> all = roomSvc.list();
            if (all.isEmpty()) { System.out.println("会議室なし"); return; }

            List<String[]> rows = new ArrayList<String[]>();
            for (MeetingRoom m : all)
                rows.add(new String[]{
                        String.valueOf(m.getId()),
                        m.getName(),
                        String.valueOf(m.getCapacity())});

            TablePrinter.print(new String[]{"ID", "名称", "定員"}, rows);
        } else {
            try {
                int id = Integer.parseInt(inp);
                MeetingRoom m = roomSvc.findById(id);
                if (m == null) {
                    System.out.println(ErrorCatalog.msg("E001", id));
                    return;
                }
                List<String[]> rows = java.util.Collections.singletonList(
                        new String[]{
                                String.valueOf(m.getId()),
                                m.getName(),
                                String.valueOf(m.getCapacity())});
                TablePrinter.print(new String[]{"ID", "名称", "定員"}, rows);
            } catch (NumberFormatException ex) {
                System.out.println(ErrorCatalog.msg("E007"));   // 数字入力エラー
            }
        }
    }

    /* ================ 入力ヘルパ ================ */
    private String readLine(String m) {
        System.out.print(m);
        return sc.nextLine();
    }
    private int readInt(String m) {
        while (true) {
            try { return Integer.parseInt(readLine(m)); }
            catch (NumberFormatException e) {
                System.out.println(ErrorCatalog.msg("E007"));
            }
        }
    }
}
