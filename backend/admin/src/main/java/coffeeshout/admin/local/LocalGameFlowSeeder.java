package coffeeshout.admin.local;

import coffeeshout.gamecommon.JoinCode;
import coffeeshout.global.ipblock.Ip;
import coffeeshout.global.ipblock.IpBlockStore;
import coffeeshout.minigame.application.port.MiniGameEntityRepository;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.infra.persistence.MiniGameEntity;
import coffeeshout.minigame.infra.persistence.MiniGameResultEntity;
import coffeeshout.minigame.infra.persistence.MiniGameResultJpaRepository;
import coffeeshout.room.application.port.PlayerEntityRepository;
import coffeeshout.room.application.port.RoomEntityRepository;
import coffeeshout.room.application.port.RouletteResultEntityRepository;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.PlayerType;
import coffeeshout.room.infra.persistence.PlayerEntity;
import coffeeshout.room.infra.persistence.RoomEntity;
import coffeeshout.room.infra.persistence.RouletteResultEntity;
import coffeeshout.user.infra.persistence.UserEntity;
import coffeeshout.user.infra.persistence.UserJpaRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로컬 백오피스에 <b>게임 플로우</b> 더미 데이터를 넣는다.
 *
 * <p>신고({@code ReportMockDataInitializer})와 닉네임 검열
 * ({@code LocalNicknameAuditDataInitializer}) 은 이미 시더가 있었지만 방과 게임은 없었다.
 * 그래서 로컬에서 백오피스를 열면 퍼널, 추이, 게임별 플레이가 전부 0이었고, 화면이
 * 제대로 그려지는지를 확인할 수가 없었다.
 *
 * <h2>실제 유저가 밟는 순서를 그대로 만든다</h2>
 * 방 생성(READY) → 참여자 입장 → 게임 시작(PLAYING) → 미니게임 결과 저장 →
 * 룰렛(ROULETTE) → 당첨자 확정 → 종료(DONE).
 *
 * <p><b>각 방을 그 중 한 지점에서 멈춘다.</b> 전부 DONE 으로 만들면 퍼널이 일직선이 되어
 * 화면이 아무 말도 하지 않는다. 실제로 봐야 하는 것은 어느 단계에서 사람들이 빠지는가다.
 *
 * <h2>과거 시각은 SQL 로 되돌린다</h2>
 * {@code RoomEntity}, {@code PlayerEntity}, {@code MiniGameResultEntity},
 * {@code RouletteResultEntity} 는 생성자 안에서 {@code createdAt} 을 {@code now()} 로
 * 박는다. 도메인 규칙으로는 맞다 - 방이 언제 만들어졌는지를 밖에서 정하게 두면 실제
 * 서비스 코드에서도 그럴 수 있게 된다.
 *
 * <p>그래서 엔티티로 만든 뒤 JDBC 로 날짜만 되돌린다. 시더 하나 때문에 도메인에 생성자를
 * 늘리지 않는다. 되돌리지 않으면 30일 추이 차트가 오늘 하루에 전부 몰려 아무 모양도
 * 나오지 않는다.
 *
 * <p>로컬은 {@code ddl-auto: create} 라 기동마다 스키마가 새로 생긴다. 이 시더도 매번
 * 다시 돈다. 씨앗을 고정해 둔 것은 그래서다 - 재시작할 때마다 숫자가 달라지면 화면을
 * 고치는 중에 무엇이 내 변경 때문인지 알 수 없다.
 */
@Slf4j
@Profile("local")
@Component
// 신고·검열 시더보다 뒤에 둘 이유는 없지만, 로그를 읽을 때 순서가 고정돼 있는 편이 낫다.
@Order(100)
@RequiredArgsConstructor
public class LocalGameFlowSeeder implements ApplicationRunner {

    /** 30일치를 만든다. 백오피스 기간 선택의 기본값이 30일이다. */
    private static final int DAYS = 30;

    /** 고정 씨앗. 기동할 때마다 같은 데이터가 나와야 화면 변경을 비교할 수 있다. */
    private static final long SEED = 20_260_906L;

    private static final List<String> NICKNAMES =
            List.of("민준", "서연", "도윤", "하은", "지호", "수아", "예준", "채원", "시우", "지우", "건우", "유나", "준서", "다인", "은우", "소율");

    /**
     * 게임 선택 가중치. 앞쪽이 인기 게임이다.
     *
     * <p>고르게 뽑으면 여덟 게임이 전부 12~15% 로 나와 "게임별 플레이" 카드가 아무 말도
     * 하지 않는다. 그 카드를 만든 이유는 <b>아무도 안 고르는 게임을 찾는 것</b>이라,
     * 실제 서비스처럼 상위 둘이 절반을 먹고 꼴찌는 바닥을 기어야 화면이 쓸모를 보인다.
     */
    private static final List<MiniGameType> GAME_POOL = List.of(
            MiniGameType.CARD_GAME,
            MiniGameType.CARD_GAME,
            MiniGameType.CARD_GAME,
            MiniGameType.CARD_GAME,
            MiniGameType.CARD_GAME,
            MiniGameType.RACING_GAME,
            MiniGameType.RACING_GAME,
            MiniGameType.RACING_GAME,
            MiniGameType.RACING_GAME,
            MiniGameType.NUNCHI_GAME,
            MiniGameType.NUNCHI_GAME,
            MiniGameType.NUNCHI_GAME,
            MiniGameType.SPEED_TOUCH,
            MiniGameType.SPEED_TOUCH,
            MiniGameType.LADDER_GAME,
            MiniGameType.LADDER_GAME,
            MiniGameType.WORM_GAME,
            MiniGameType.BLOCK_STACKING,
            MiniGameType.BLIND_TIMER);

    private final RoomEntityRepository rooms;
    private final PlayerEntityRepository players;
    private final MiniGameEntityRepository miniGames;
    private final MiniGameResultJpaRepository miniGameResults;
    private final RouletteResultEntityRepository roulettes;
    private final UserJpaRepository users;
    private final IpBlockStore ipBlockStore;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        final Integer existing = jdbc.queryForObject("SELECT COUNT(*) FROM room_session", Integer.class);
        if (existing != null && existing > 0) {
            log.debug("[LocalGameFlowSeeder] 방 데이터가 이미 있어 건너뜁니다.");
            return;
        }

        final Random random = new Random(SEED);
        final LocalDate today = LocalDate.now(clock);

        seedUsers(random, today);
        seedBlockedIps();
        final int roomCount = seedRooms(random, today);

        log.info("[LocalGameFlowSeeder] 최근 {}일치 방 {}개 삽입 완료", DAYS, roomCount);
    }

    /**
     * 차단된 IP 몇 개.
     *
     * <p>Redis 에 TTL 로 들어가는 값이라 DB 시더와 수명이 다르다. 기동마다 스키마가
     * 새로 생기는 DB 와 달리 Redis 는 그대로 남으므로, 같은 IP 를 다시 넣어도 TTL 만
     * 갱신되고 중복이 쌓이지는 않는다.
     *
     * <p>비워 두면 홈의 "차단 IP" 칸이 늘 0 이라 그 칸이 제대로 도는지 알 수 없다.
     * 문서 예제용으로 예약된 대역(RFC 5737)을 쓴다. 실제로 존재하는 주소를 적어 두면
     * 나중에 이 목록을 보고 진짜 차단된 IP 로 오해할 수 있다.
     */
    private void seedBlockedIps() {
        List.of("203.0.113.7", "203.0.113.42", "198.51.100.19")
                .forEach(ip -> ipBlockStore.blockImmediately(new Ip(ip)));
    }

    /**
     * 가입자. 비회원도 게임을 할 수 있으므로 참여자 수보다 훨씬 적게 만든다.
     * 이 비율이 실제와 비슷해야 홈의 "신규 가입"이 방 수와 비교됐을 때 이상해 보이지 않는다.
     */
    private void seedUsers(Random random, LocalDate today) {
        for (int i = 0; i < NICKNAMES.size(); i++) {
            final UserEntity saved = users.save(new UserEntity(String.format("U%04d", i + 1), NICKNAMES.get(i)));
            final LocalDateTime joinedAt = randomTimeOn(today.minusDays(random.nextInt(DAYS)), random);
            jdbc.update(
                    "UPDATE app_user SET created_at = ?, updated_at = ? WHERE id = ?",
                    joinedAt,
                    joinedAt,
                    saved.getId());
        }
    }

    private int seedRooms(Random random, LocalDate today) {
        final Set<String> usedCodes = new HashSet<>();
        int created = 0;
        for (int back = DAYS - 1; back >= 0; back--) {
            final LocalDate date = today.minusDays(back);
            for (int n = 0; n < roomsOn(date, random); n++) {
                seedRoom(date, random, usedCodes);
                created++;
            }
        }
        return created;
    }

    /**
     * 하루치 방 개수. 주말을 평일보다 많게 둔다.
     *
     * <p>매일 같은 수로 만들면 추이 차트가 평평한 직선이 되어, 차트가 제대로 그려지는지
     * 아니면 데이터가 안 오는지를 구분할 수 없다.
     */
    private int roomsOn(LocalDate date, Random random) {
        final boolean weekend = date.getDayOfWeek().getValue() >= 6;
        return (weekend ? 6 : 3) + random.nextInt(weekend ? 5 : 4);
    }

    private void seedRoom(LocalDate date, Random random, Set<String> usedCodes) {
        final RoomState stage = pickStage(random);
        final LocalDateTime createdAt = randomTimeOn(date, random);

        final RoomEntity room = rooms.saveAndFlush(new RoomEntity(uniqueJoinCode(usedCodes)));
        if (stage != RoomState.READY) {
            room.updateRoomStatus(stage);
        }
        if (stage == RoomState.DONE) {
            room.finish();
        }
        rooms.saveAndFlush(room);

        final List<PlayerEntity> joined = seedPlayers(room, stage, random);
        seedGames(room, joined, stage, random);
        if (stage == RoomState.ROULETTE || stage == RoomState.DONE) {
            roulettes.save(
                    new RouletteResultEntity(room, joined.get(random.nextInt(joined.size())), 20 + random.nextInt(40)));
        }

        backdate(room.getId(), createdAt, stage);
    }

    /**
     * 방이 어디까지 갔는지. 비율은 실제 서비스에서 그럴듯한 이탈 곡선으로 잡았다.
     *
     * <p>READY 에서 멈춘 방이 있어야 "만들어 놓고 아무도 안 왔다"가 퍼널에 보인다.
     * 그 칸이 늘 0이면 퍼널의 첫 두 단계가 같은 숫자가 되어 볼 이유가 없어진다.
     */
    private RoomState pickStage(Random random) {
        final int roll = random.nextInt(100);
        if (roll < 12) {
            return RoomState.READY;
        }
        if (roll < 22) {
            return RoomState.PLAYING;
        }
        if (roll < 32) {
            return RoomState.ROULETTE;
        }
        return RoomState.DONE;
    }

    /**
     * 참여자. READY 에서 멈춘 방의 절반은 방장 혼자 둔다.
     * 나머지 절반은 사람은 모였는데 시작을 안 한 경우다. 둘 다 실제로 있는 모양이다.
     */
    private List<PlayerEntity> seedPlayers(RoomEntity room, RoomState stage, Random random) {
        final boolean alone = stage == RoomState.READY && random.nextBoolean();
        final int count = alone ? 1 : 2 + random.nextInt(5);

        final List<PlayerEntity> saved = new ArrayList<>();
        final List<String> pool = new ArrayList<>(NICKNAMES);
        for (int i = 0; i < count; i++) {
            final String name = pool.remove(random.nextInt(pool.size()));
            saved.add(players.save(new PlayerEntity(room, name, i == 0 ? PlayerType.HOST : PlayerType.GUEST)));
        }
        return saved;
    }

    /**
     * 미니게임 판과 결과.
     *
     * <p>{@code mini_game_play} 는 게임이 <b>끝날 때</b> 결과와 함께 저장된다. 그래서
     * PLAYING 에서 멈춘 방은 판이 하나도 없는 것이 맞다. 여기서 억지로 넣으면 백오피스의
     * "시작만 하고 만 게임은 잡히지 않는다"는 설명과 데이터가 서로 어긋난다.
     */
    private void seedGames(RoomEntity room, List<PlayerEntity> joined, RoomState stage, Random random) {
        if (stage == RoomState.PLAYING || joined.size() < 2) {
            return;
        }

        final List<MiniGameType> picked = new ArrayList<>();
        final int rounds = 1 + random.nextInt(3);
        for (int r = 0; r < rounds; r++) {
            // 한 방에서 같은 게임을 두 번 하지는 않는다. 가중치 풀에서 뽑되 중복만 거른다.
            MiniGameType type = GAME_POOL.get(random.nextInt(GAME_POOL.size()));
            while (picked.contains(type)) {
                type = GAME_POOL.get(random.nextInt(GAME_POOL.size()));
            }
            picked.add(type);

            final MiniGameEntity play = miniGames.save(new MiniGameEntity(room.getId(), type));

            final List<PlayerEntity> ranked = new ArrayList<>(joined);
            Collections.shuffle(ranked, random);
            for (int rank = 0; rank < ranked.size(); rank++) {
                miniGameResults.save(new MiniGameResultEntity(
                        play, ranked.get(rank).getId(), rank + 1, (long) (100 - rank * 10 - random.nextInt(8))));
            }
        }
    }

    /**
     * 엔티티가 {@code now()} 로 박아 둔 시각을 그날로 되돌린다.
     *
     * <p>방 id 하나로 딸린 행을 전부 잡는다. 행마다 id 를 모아 두면 시더가 데이터가 아니라
     * id 장부를 관리하는 코드가 된다.
     */
    private void backdate(Long roomId, LocalDateTime createdAt, RoomState stage) {
        final LocalDateTime finishedAt = createdAt.plusMinutes(8 + (roomId.intValue() % 20));

        jdbc.update(
                "UPDATE room_session SET created_at = ?, finished_at = ? WHERE id = ?",
                createdAt,
                stage == RoomState.DONE ? finishedAt : null,
                roomId);
        jdbc.update("UPDATE player SET created_at = ? WHERE room_session_id = ?", createdAt.plusMinutes(1), roomId);
        jdbc.update("""
                UPDATE mini_game_result SET created_at = ?
                 WHERE mini_game_play_id IN (SELECT id FROM mini_game_play WHERE room_session_id = ?)
                """, createdAt.plusMinutes(4), roomId);
        jdbc.update("UPDATE roulette_result SET created_at = ? WHERE room_session_id = ?", finishedAt, roomId);
    }

    /** 서비스가 쓰는 시간대가 아니라 사람이 게임하는 시간대에 몰아 준다. */
    private LocalDateTime randomTimeOn(LocalDate date, Random random) {
        return date.atTime(11 + random.nextInt(12), random.nextInt(60), random.nextInt(60));
    }

    /**
     * 참여 코드. <b>실제 생성기를 그대로 쓴다.</b>
     *
     * <p>직접 만들다가 5자짜리 "A0001" 을 넣고 있었다. 실제 코드는 4자이고, 헷갈리는
     * 문자(I, O, 0, 1, 2, 5)를 뺀 전용 charset 을 쓴다. 형식이 다르면 방 조회 화면에서
     * 검색을 시험할 때 실제로는 없는 모양의 코드로 테스트하게 된다.
     *
     * <p>같은 코드가 두 번 나오면 다시 뽑는다. 조회 화면이 joinCode 로 방을 찾으므로
     * 겹치면 엉뚱한 방이 잡힌다.
     */
    private String uniqueJoinCode(Set<String> used) {
        String code = JoinCode.generate().getValue();
        while (!used.add(code)) {
            code = JoinCode.generate().getValue();
        }
        return code;
    }
}
