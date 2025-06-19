package reservation.model;

import java.io.Serializable;

public class MeetingRoom implements Serializable {
    private final int id;
    private String name;
    private int capacity;

    public MeetingRoom(int id, String name, int capacity) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("name は必須");
        if (capacity <= 0)
            throw new IllegalArgumentException("capacity は正の整数");

        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public int getId()       { return id; }
    public String getName()  { return name; }
    public int getCapacity() { return capacity; }

    public void setName(String n)      { this.name = n; }
    public void setCapacity(int cap)   { this.capacity = cap; }

    @Override
    public String toString() {
        return String.format("[#%d] %s (定員:%d)", id, name, capacity);
    }
}
