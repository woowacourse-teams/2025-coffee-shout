package coffeeshout.repository;

import coffeeshout.domain.Room;
import org.springframework.data.repository.CrudRepository;

public interface RoomRepository extends CrudRepository<Room, String> {

}
