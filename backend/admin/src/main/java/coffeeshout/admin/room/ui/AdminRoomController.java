package coffeeshout.admin.room.ui;

import coffeeshout.admin.room.application.RoomLookupService;
import coffeeshout.admin.room.ui.response.RoomDetailResponse;
import coffeeshout.admin.room.ui.response.RoomSummaryResponse;
import coffeeshout.admin.support.PageResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 방 조회와 드릴다운.
 *
 * <p>상세를 {@code joinCode}가 아니라 {@code id}로 연다. join_code 에는 유니크 제약이 없어
 * 같은 코드가 시간이 지나 다른 방에 다시 쓰인다. 코드로 상세를 열면 어느 방인지 결정되지 않는다.
 * 검색은 코드로 하고, 결과에서 방을 골라 들어간다.
 */
@RestController
@RequestMapping("/admin/api/rooms")
@Validated
@RequiredArgsConstructor
public class AdminRoomController {

    private final RoomLookupService roomLookupService;

    @GetMapping
    public PageResponse<RoomSummaryResponse> search(
            @RequestParam(required = false) String joinCode,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        return PageResponse.of(
                roomLookupService.search(joinCode, page), RoomSummaryResponse::from);
    }

    @GetMapping("/{roomId}")
    public RoomDetailResponse detail(@PathVariable Long roomId) {
        return RoomDetailResponse.from(roomLookupService.findDetail(roomId));
    }
}
