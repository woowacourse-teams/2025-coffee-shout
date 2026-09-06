package coffeeshout.admin.quality.ui;

import coffeeshout.admin.quality.application.QualityService;
import coffeeshout.admin.quality.ui.response.NicknameAuditQualityResponse;
import coffeeshout.admin.quality.ui.response.ReportSlaResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 운영 품질 지표. 신고 화면과 검열 화면 상단에 각각 붙는다.
 */
@RestController
@RequestMapping("/admin/api/quality")
@Validated
@RequiredArgsConstructor
public class AdminQualityController {

    private final QualityService qualityService;

    /**
     * @param days 조회 기간. 상한을 두는 것은 기간을 늘려 전체 스캔을 유발하는 것을 막기 위해서다.
     */
    @GetMapping("/nickname-audit")
    public NicknameAuditQualityResponse nicknameAudit(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        return NicknameAuditQualityResponse.from(qualityService.nicknameAuditQuality(days));
    }

    @GetMapping("/report-sla")
    public ReportSlaResponse reportSla(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        return ReportSlaResponse.from(qualityService.reportSla(days));
    }
}
