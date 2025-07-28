
package coffeeshout.minigame.ui;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import coffeeshout.fixture.RoomFixture;
import coffeeshout.fixture.TestStompSession;
import coffeeshout.fixture.TestStompSession.MessageCollector;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.minigame.ui.request.CommandType;
import coffeeshout.minigame.ui.request.MiniGameMessage;
import coffeeshout.minigame.ui.request.command.SelectCardCommand;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage.CardInfoMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.condition.Join;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSession;

class CardGameIntegrationTest extends WebSocketIntegrationTestSupport {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    CardGameService cardGameService;

    @Autowired
    ObjectMapper objectMapper;

    JoinCode joinCode;

    Player host;

    @BeforeEach
    void setUp() {
        joinCode = new JoinCode("A4B2C");

        Room room = RoomFixture.호스트_꾹이();
        host = room.getHost();
        roomRepository.save(room);
    }

    @Test
    void 카드를_선택한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";

        Room room = roomRepository.findByJoinCode(joinCode).get();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        MessageCollector<MiniGameStateMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                MiniGameStateMessage.class
        );

        cardGameService.startGame(joinCode);

        MiniGameStateMessage loadingState = responses.get(); // 게임 로딩 state 응답 (LOADING)
        assertThat(loadingState.cardGameState()).isEqualTo(CardGameState.LOADING.name());

        MiniGameStateMessage playingState = responses.get(); // 게임 시작 state 응답 (PLAYING)
        assertThat(playingState.cardGameState()).isEqualTo(CardGameState.PLAYING.name());

        String playerName = "꾹이";
        int cardIndex = 0;

        MiniGameMessage request = new MiniGameMessage(
                MiniGameType.CARD_GAME,
                CommandType.SELECT_CARD,
                objectMapper.valueToTree(new SelectCardCommand(playerName, cardIndex))
        );

        // when
        session.send(String.format(requestUrlFormat, joinCode.value()), request);

        // then
        MiniGameStateMessage result = responses.get();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.currentRound()).isEqualTo(CardGameRound.FIRST.name());
            softly.assertThat(result.cardInfoMessages()).extracting(CardInfoMessage::playerName)
                    .contains(playerName);
            softly.assertThat(result.allSelected()).isFalse();
        });
    }

    @Test
    void 전체_게임_플로우를_진행한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";

        Room room = roomRepository.findByJoinCode(joinCode).get();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        MessageCollector<MiniGameStateMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                MiniGameStateMessage.class
        );

        // when & then
        cardGameService.startGame(joinCode);

        // LOADING 상태 확인
        MiniGameStateMessage loadingState = responses.get();
        assertThat(loadingState.cardGameState()).isEqualTo(CardGameState.LOADING.name());

        // PLAYING 상태 확인
        MiniGameStateMessage playingState = responses.get();
        assertThat(playingState.cardGameState()).isEqualTo(CardGameState.PLAYING.name());
        assertThat(playingState.currentRound()).isEqualTo(CardGameRound.FIRST.name());

        // 모든 플레이어가 카드 선택
        for (int i = 0; i < room.getPlayers().size(); i++) {
            Player player = room.getPlayers().get(i);
            MiniGameMessage request = new MiniGameMessage(
                    MiniGameType.CARD_GAME,
                    CommandType.SELECT_CARD,
                    objectMapper.valueToTree(new SelectCardCommand(player.getName().value(), i))
            );
            session.send(String.format(requestUrlFormat, joinCode.value()), request);
            responses.get();
        }

        // 첫 번째 라운드 완료 후 SCORE_BOARD 상태로 변경
        MiniGameStateMessage scoreBoardState = responses.get(10, TimeUnit.SECONDS);
        assertThat(scoreBoardState.cardGameState()).isEqualTo(CardGameState.SCORE_BOARD.name());

        // 두 번째 라운드 시작
        MiniGameStateMessage secondLoadingState = responses.get();
        assertThat(secondLoadingState.cardGameState()).isEqualTo(CardGameState.LOADING.name());

        // 두 번째 라운드 시작
        MiniGameStateMessage secondRoundState = responses.get();
        assertThat(secondRoundState.cardGameState()).isEqualTo(CardGameState.PLAYING.name());
        assertThat(secondRoundState.currentRound()).isEqualTo(CardGameRound.SECOND.name());
    }

    @Test
    void 시간제한이_끝나면_라운드가_종료된다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";

        Room room = roomRepository.findByJoinCode(joinCode).get();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        MessageCollector<MiniGameStateMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                MiniGameStateMessage.class
        );

        cardGameService.startGame(joinCode);
        responses.get(); // LOADING
        responses.get(); // PLAYING

        // when

        // then
        MiniGameStateMessage result = responses.get(15, TimeUnit.SECONDS);
        assertThat(result.allSelected()).isTrue(); // 단일 플레이어이므로 모든 선택 완료
    }

    @Test
    void 게임_상태_메시지에_카드_정보가_포함된다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";

        Room room = roomRepository.findByJoinCode(joinCode).get();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        MessageCollector<MiniGameStateMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                MiniGameStateMessage.class
        );

        // when
        cardGameService.startGame(joinCode);
        responses.get(); // LOADING

        // then
        MiniGameStateMessage playingState = responses.get(); // PLAYING

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(playingState.cardInfoMessages()).isNotEmpty();
            softly.assertThat(playingState.cardInfoMessages()).hasSize(9); // 6개 덧셈카드 + 3개 곱셈카드
            softly.assertThat(playingState.cardInfoMessages())
                    .allMatch(cardInfo -> cardInfo.cardType().equals("ADDITION") || cardInfo.cardType().equals("MULTIPLIER"));
            softly.assertThat(playingState.cardInfoMessages())
                    .allMatch(cardInfo -> !cardInfo.selected());
        });
    }

    @Disabled
    @Test
    void 잘못된_카드_인덱스로_선택을_시도하면_예외가_발생한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";
        String errorUrlFormat = "/queue/errors";

        Room room = roomRepository.findByJoinCode(joinCode).get();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        MessageCollector<MiniGameStateMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                MiniGameStateMessage.class
        );

        MessageCollector<WebSocketResponse> errorResponses = session.subscribe(errorUrlFormat, WebSocketResponse.class);


        cardGameService.startGame(joinCode);
        responses.get(); // LOADING
        responses.get(); // PLAYING

        // when & then
        MiniGameMessage request = new MiniGameMessage(
                MiniGameType.CARD_GAME,
                CommandType.SELECT_CARD,
                objectMapper.valueToTree(new SelectCardCommand("꾹이", 999)) // 잘못된 인덱스
        );
        session.send(String.format(requestUrlFormat, joinCode.value()), request);

        WebSocketResponse errorResponse = errorResponses.get();

        SoftAssertions.assertSoftly(softly -> {
            // 에러 검증 추가하기 (비동기 스레드 예외 핸들러 처리 불가)
        });
    }

    @Disabled
    @Test
    void 게임이_진행중이_아닐때_카드_선택을_시도하면_예외가_발생한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";

        Room room = roomRepository.findByJoinCode(joinCode).get();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        MessageCollector<MiniGameStateMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                MiniGameStateMessage.class
        );

        // 게임을 시작하지 않고 카드 선택 시도
        MiniGameMessage request = new MiniGameMessage(
                MiniGameType.CARD_GAME,
                CommandType.SELECT_CARD,
                objectMapper.valueToTree(new SelectCardCommand("꾹이", 0))
        );

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            session.send(String.format(requestUrlFormat, joinCode.value()), request);
            responses.get(); // 예외가 발생해야 함
        });
    }

    @Test
    void 카드_선택_후_상태_메시지에_선택된_카드_정보가_반영된다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";

        Room room = roomRepository.findByJoinCode(joinCode).get();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        MessageCollector<MiniGameStateMessage> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                MiniGameStateMessage.class
        );

        cardGameService.startGame(joinCode);
        responses.get(); // LOADING
        responses.get(); // PLAYING

        String playerName = "꾹이";
        int cardIndex = 0;

        MiniGameMessage request = new MiniGameMessage(
                MiniGameType.CARD_GAME,
                CommandType.SELECT_CARD,
                objectMapper.valueToTree(new SelectCardCommand(playerName, cardIndex))
        );

        // when
        session.send(String.format(requestUrlFormat, joinCode.value()), request);

        // then
        MiniGameStateMessage result = responses.get();

        SoftAssertions.assertSoftly(softly -> {
            // 선택된 카드가 있는지 확인
            long selectedCount = result.cardInfoMessages().stream()
                    .mapToLong(cardInfo -> cardInfo.selected() ? 1 : 0)
                    .sum();
            softly.assertThat(selectedCount).isEqualTo(1);

            // 선택된 카드의 플레이어 이름이 올바른지 확인
            CardInfoMessage selectedCard = result.cardInfoMessages().stream()
                    .filter(CardInfoMessage::selected)
                    .findFirst()
                    .orElse(null);
            softly.assertThat(selectedCard).isNotNull();
            softly.assertThat(selectedCard.playerName()).isEqualTo(playerName);
        });
    }

    @Test
    void 멀티플레이어_환경에서_각_플레이어가_다른_카드를_선택할_수_있다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session1 = createSession();
        TestStompSession session2 = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";

        JoinCode roomCode = new JoinCode("ABCDE");
        Room room = new Room(roomCode, new PlayerName("플레이어1"), null);
        room.joinGuest(new PlayerName("플레이어2"), null);
        room.addMiniGame(new PlayerName("플레이어1"), MiniGameType.CARD_GAME.createMiniGame());
        roomRepository.save(room);

        room = roomRepository.findByJoinCode(roomCode).get();


        MessageCollector<MiniGameStateMessage> responses1 = session1.subscribe(
                String.format(subscribeUrlFormat, roomCode.value()),
                MiniGameStateMessage.class
        );
        MessageCollector<MiniGameStateMessage> responses2 = session2.subscribe(
                String.format(subscribeUrlFormat, roomCode.value()),
                MiniGameStateMessage.class
        );

        cardGameService.startGame(joinCode);
        responses1.get();
        responses2.get();

        responses1.get();
        responses2.get();

        // when - 각 플레이어가 다른 카드 선택
        String[] playerNames = {"플레이어1", "플레이어2"};
        int[] cardIndices = {0, 1};
        TestStompSession[] sessions = {session1, session2};
        MessageCollector[] responses = {responses1, responses2};

        for (int i = 0; i < playerNames.length; i++) {
            MiniGameMessage request = new MiniGameMessage(
                    MiniGameType.CARD_GAME,
                    CommandType.SELECT_CARD,
                    objectMapper.valueToTree(new SelectCardCommand(playerNames[i], cardIndices[i]))
            );
            sessions[i].send(String.format(requestUrlFormat, joinCode.value()), request);
            responses[i].get();
        }

        // then - 마지막 상태에서 모든 플레이어가 선택했는지 확인
        MiniGameStateMessage finalState = responses1.get();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(finalState.allSelected()).isTrue();

            long selectedCount = finalState.cardInfoMessages().stream()
                    .mapToLong(cardInfo -> cardInfo.selected() ? 1 : 0)
                    .sum();
            softly.assertThat(selectedCount).isEqualTo(3); // 3명의 플레이어가 모두 선택
        });
    }
}
