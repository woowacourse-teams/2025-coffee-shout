package coffeeshout.profanity.infra.persistence.audit;

import coffeeshout.profanity.domain.audit.AiConfidence;
import coffeeshout.profanity.domain.audit.NicknameAudit;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class NicknameAuditBulkUpdaterImpl implements NicknameAuditBulkUpdater {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private final JdbcTemplate jdbcTemplate;

    /**
     * 준영속 엔티티를 {@code saveAll}로 넘기면 JPA merge가 갱신 전 영속 인스턴스를 얻으려고 건별 SELECT를
     * 먼저 날린다. JDBC 배치 UPDATE로 곧장 쓰면 이 SELECT가 통째로 없어진다.
     *
     * <p>rewriteBatchedStatements=true(운영 설정, {@code database.yml})면 드라이버가 배치를 multi-row
     * UPDATE로 재작성하고 executeBatch가 실제 행수 대신 SUCCESS_NO_INFO(-2)를 반환한다
     * ({@code ProfanityWordRepositoryImpl.bulkInsertIgnore}와 같은 함정). 그래서 반환값으로 갱신 행 수를
     * 세지 않고 void로 둔다.
     */
    @Override
    public void bulkUpdateAuditResults(List<NicknameAudit> entities) {
        if (entities.isEmpty()) {
            return;
        }
        final String sql =
                "UPDATE player_name_audit SET status = ?, confidence = ?, reason = ?, audited_at = ? WHERE id = ?";
        // Calendar 없이 setTimestamp를 쓰면 JVM 기본 타임존(KST 등)으로 해석돼, Hibernate가 Instant 컬럼을
        // 읽고 쓸 때 쓰는 UTC 기준과 어긋나 시간이 몇 시간씩 밀린다. Calendar는 상태를 갖는 mutable 객체라
        // 배치 항목마다 새로 만든다.
        jdbcTemplate.batchUpdate(sql, entities, 500, (ps, entity) -> {
            ps.setString(1, entity.getStatus().name());
            final AiConfidence confidence = entity.getConfidence();
            ps.setBigDecimal(2, confidence != null ? confidence.value() : null);
            ps.setString(3, entity.getReason());
            ps.setTimestamp(4, Timestamp.from(entity.getAuditedAt()), Calendar.getInstance(UTC));
            ps.setLong(5, entity.getId());
        });
    }
}
