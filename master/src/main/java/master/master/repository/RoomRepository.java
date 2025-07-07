package master.master.repository;

import master.master.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Custom query to find a Room and sort them by ID in descending order
    @Query("SELECT r FROM Room r ORDER BY r.id DESC")
    Room findAllRoom();
}
