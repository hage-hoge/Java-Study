package reservation.model;

/**
 * 予約エンティティ
 *   owner : 予約を入れたユーザ名
 */
public class Reservation {
    private final int   id;
    private final int   roomId;
    private final long  startMillis;
    private final long  endMillis;
    private final String owner;   // ★追加

    public Reservation(int id, int roomId, long st, long ed, String owner) {
        this.id = id;
        this.roomId = roomId;
        this.startMillis = st;
        this.endMillis = ed;
        this.owner = owner;
    }

    /* ---------- getter ---------- */
    public int    getId()          { return id; }
    public int    getRoomId()      { return roomId; }
    public long   getStartMillis() { return startMillis; }
    public long   getEndMillis()   { return endMillis; }
    public String getOwner()       { return owner; }
}
