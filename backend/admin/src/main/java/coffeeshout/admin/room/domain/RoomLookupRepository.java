package coffeeshout.admin.room.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomLookupRepository {

    /** {@code joinCode}가 비어 있으면 최근 방부터 전체를 돌려준다. */
    Page<RoomSummary> search(String joinCode, Pageable pageable);

    Optional<RoomSummary> findSummaryById(Long roomId);

    List<RoomPlayer> findPlayers(Long roomId);

    List<RoomMiniGameResult> findMiniGameResults(Long roomId);

    Optional<RoomRouletteResult> findRouletteResult(Long roomId);
}
