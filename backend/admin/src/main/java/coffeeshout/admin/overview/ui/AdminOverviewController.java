package coffeeshout.admin.overview.ui;

import coffeeshout.admin.overview.application.OverviewService;
import coffeeshout.admin.overview.ui.response.ActionQueueResponse;
import coffeeshout.admin.overview.ui.response.DailySummaryResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 홈 대시보드. 화면 상단부터 순서대로 대응한다.
 *
 * <p>큐와 요약을 한 응답으로 묶지 않았다. 큐는 조치가 있을 때마다 다시 불러야 하고
 * 요약은 하루에 몇 번 안 바뀐다. 갱신 주기가 다른 것을 한 응답에 묶으면
 * 화면이 필요 없는 집계 쿼리를 계속 돌리게 된다.
 */
@RestController
@RequestMapping("/admin/api/overview")
@RequiredArgsConstructor
public class AdminOverviewController {

    private final OverviewService overviewService;

    @GetMapping("/action-queue")
    public ActionQueueResponse actionQueue() {
        return ActionQueueResponse.from(overviewService.actionQueue());
    }

    /** 날짜를 안 주면 오늘(KST)이다. */
    @GetMapping("/summary")
    public DailySummaryResponse summary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return DailySummaryResponse.from(
                date == null ? overviewService.today() : overviewService.summaryOf(date));
    }
}
