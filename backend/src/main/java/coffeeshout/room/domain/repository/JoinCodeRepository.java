package coffeeshout.room.domain.repository;

import coffeeshout.room.domain.JoinCode;

public interface JoinCodeRepository {

    boolean existsByJoinCode(JoinCode joinCode);

    void save(JoinCode joinCode);
}
