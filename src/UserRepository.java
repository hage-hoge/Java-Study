package reservation.repository;

import reservation.model.User;
import java.util.List;

public interface UserRepository {
    void add(User u) throws Exception;
    User find(String name);
    List<User> findAll();
    void flush() throws Exception;
}
