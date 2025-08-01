package coffeeshout.minigame.ui;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import coffeeshout.fixture.RoomFixture;
import coffeeshout.fixture.TestStompSession;
import coffeeshout.fixture.TestStompSession.MessageCollector;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.executor.CardGameTaskInfo;
import coffeeshout.minigame.domain.executor.TaskExecutor;
import coffeeshout.minigame.ui.request.CommandType;
import coffeeshout.minigame.ui.request.MiniGameMessage;
import coffeeshout.minigame.ui.request.command.SelectCardCommand;
import coffeeshout.minigame.ui.request.command.StartMiniGameCommand;
import coffeeshout.minigame.ui.response.MinIGameStartMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage.CardInfoMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.RoomRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CardGameIntegrationTest extends WebSocketIntegrationTestSupport {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CardGameTaskExecutors cardGameTaskExecutors;

    JoinCode joinCode;

    Player host;

    @BeforeEach
    void setUp() {
        joinCode = new JoinCode("A4B2C");

        Room room = RoomFixture.호스트_꾹이();
        host = room.getHost();
        room.addMiniGame(host.getName(), MiniGameType.CARD_GAME.createMiniGame());

        roomRepository.save(room);
    }

    @AfterEach
    void tearDown() {
        // 실행 중인 모든 태스크 취소 및 스레드 정리
        TaskExecutor<CardGameTaskInfo> executor = cardGameTaskExecutors.get(joinCode);
        if (executor != null) {
            executor.cancelAll();
            executor.getExecutor().shutdown();
            try {
                if (!executor.getExecutor().awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.getExecutor().shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.getExecutor().shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    void 카드_게임을_시작한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";
        String gameStartUrlFormat = "/topic/room/%s/round";

        MiniGameMessage startGameRequest = new MiniGameMessage(
                CommandType.START_CARD_GAME,
                objectMapper.valueToTree(new StartMiniGameCommand(host.getName().value()))
        );

        MessageCollector<WebSocketResponse<MiniGameStateMessage>> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                new TypeReference<>() {
                }
        );

        MessageCollector<WebSocketResponse<MinIGameStartMessage>> startResponses = session.subscribe(
                String.format(gameStartUrlFormat, joinCode.value()),
                new TypeReference<>() {
                }
        );

        // when
        session.send(String.format(requestUrlFormat, joinCode.value()), startGameRequest);

        // then
        SoftAssertions.assertSoftly(softly -> {
            MinIGameStartMessage startResponse = startResponses.get().data();
            softly.assertThat(startResponse.miniGameType()).isEqualTo(MiniGameType.CARD_GAME);

            MiniGameStateMessage loadingStateResponse = responses.get().data();
            softly.assertThat(loadingStateResponse.cardGameState()).isEqualTo(CardGameState.LOADING.name());
            softly.assertThat(loadingStateResponse.currentRound()).isEqualTo(CardGameRound.READY.name());
        });
    }

    @Test
    void 카드를_선택한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";

        MessageCollector<WebSocketResponse<MiniGameStateMessage>> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                new TypeReference<WebSocketResponse<MiniGameStateMessage>>() {
                }
        );

        sendStartGame(session, joinCode, host.getName().value());

        MiniGameStateMessage loadingState = responses.get().data(); // 게임 로딩 state 응답 (LOADING)
        assertThat(loadingState.cardGameState()).isEqualTo(CardGameState.LOADING.name());

        MiniGameStateMessage playingState = responses.get().data(); // 게임 시작 state 응답 (PLAYING)
        assertThat(playingState.cardGameState()).isEqualTo(CardGameState.PLAYING.name());

        String playerName = "꾹이";
        int cardIndex = 0;

        MiniGameMessage request = new MiniGameMessage(
                CommandType.SELECT_CARD,
                objectMapper.valueToTree(new SelectCardCommand(playerName, cardIndex))
        );

        // when
        session.send(String.format(requestUrlFormat, joinCode.value()), request);

        // then
        MiniGameStateMessage result = responses.get().data();

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

        List<Player> players = roomRepository.findByJoinCode(joinCode).get().getPlayers();

        MessageCollector<WebSocketResponse<MiniGameStateMessage>> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                new TypeReference<>() {
                }
        );

        // when & then
        sendStartGame(session, joinCode, host.getName().value());

        // LOADING 상태 확인
        MiniGameStateMessage loadingState = responses.get().data();
        assertThat(loadingState.cardGameState()).isEqualTo(CardGameState.LOADING.name());

        // PLAYING 상태 확인
        MiniGameStateMessage playingState = responses.get().data();
        assertThat(playingState.cardGameState()).isEqualTo(CardGameState.PLAYING.name());
        assertThat(playingState.currentRound()).isEqualTo(CardGameRound.FIRST.name());

        // 모든 플레이어가 카드 선택
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            MiniGameMessage request = new MiniGameMessage(
                    CommandType.SELECT_CARD,
                    objectMapper.valueToTree(new SelectCardCommand(player.getName().value(), i))
            );
            session.send(String.format(requestUrlFormat, joinCode.value()), request);
            responses.get();
        }

        // 첫 번째 라운드 완료 후 SCORE_BOARD 상태로 변경
        MiniGameStateMessage scoreBoardState = responses.get(10, TimeUnit.SECONDS).data();
        assertThat(scoreBoardState.cardGameState()).isEqualTo(CardGameState.SCORE_BOARD.name());

        // 두 번째 라운드 시작
        MiniGameStateMessage secondLoadingState = responses.get().data();
        assertThat(secondLoadingState.cardGameState()).isEqualTo(CardGameState.LOADING.name());

        // 두 번째 라운드 시작
        MiniGameStateMessage secondRoundState = responses.get().data();
        assertThat(secondRoundState.cardGameState()).isEqualTo(CardGameState.PLAYING.name());
        assertThat(secondRoundState.currentRound()).isEqualTo(CardGameRound.SECOND.name());
    }

    @Test
    void 시간제한이_끝나면_라운드가_종료된다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";

        MessageCollector<WebSocketResponse<MiniGameStateMessage>> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                new TypeReference<>() {
                }
        );

        sendStartGame(session, joinCode, host.getName().value());
        responses.get(); // LOADING
        responses.get(); // PLAYING

        // when

        // then
        MiniGameStateMessage result = responses.get(15, TimeUnit.SECONDS).data();
        assertThat(result.allSelected()).isTrue(); // 단일 플레이어이므로 모든 선택 완료
    }

    @Test
    void 게임_상태_메시지에_카드_정보가_포함된다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";

        MessageCollector<WebSocketResponse<MiniGameStateMessage>> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                new TypeReference<>() {
                }
        );

        // when
        sendStartGame(session, joinCode, host.getName().value());
        responses.get(); // LOADING

        // then
        MiniGameStateMessage playingState = responses.get().data(); // PLAYING

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(playingState.cardInfoMessages()).isNotEmpty();
            softly.assertThat(playingState.cardInfoMessages()).hasSize(9); // 6개 덧셈카드 + 3개 곱셈카드
            softly.assertThat(playingState.cardInfoMessages())
                    .allMatch(cardInfo -> cardInfo.cardType().equals("ADDITION") || cardInfo.cardType()
                            .equals("MULTIPLIER"));
            softly.assertThat(playingState.cardInfoMessages())
                    .allMatch(cardInfo -> !cardInfo.selected());
        });
    }

    @Test
    void 잘못된_카드_인덱스로_선택을_시도하면_예외가_발생한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";

        MessageCollector<WebSocketResponse<MiniGameStateMessage>> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                new TypeReference<>() {
                }
        );

        sendStartGame(session, joinCode, host.getName().value());
        responses.get(); // LOADING
        responses.get(); // PLAYING

        // when
        MiniGameMessage request = new MiniGameMessage(
                CommandType.SELECT_CARD,
                objectMapper.valueToTree(new SelectCardCommand("꾹이", 999)) // 잘못된 인덱스
        );
        session.send(String.format(requestUrlFormat, joinCode.value()), request);

        // then
        assertThatThrownBy(() -> responses.get())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("메시지 수신 대기 시간을 초과했습니다");
    }

    @Test
    void 게임이_진행중이_아닐때_카드_선택을_시도하면_예외가_발생한다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";

        MessageCollector<WebSocketResponse<MiniGameStateMessage>> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                new TypeReference<>() {
                }
        );

        // 게임을 시작하지 않고 카드 선택 시도
        MiniGameMessage request = new MiniGameMessage(
                CommandType.SELECT_CARD,
                objectMapper.valueToTree(new SelectCardCommand("꾹이", 0))
        );

        // when
        session.send(String.format(requestUrlFormat, joinCode.value()), request);

        // then
        assertThatThrownBy(() -> responses.get())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("메시지 수신 대기 시간을 초과했습니다");
    }

    @Test
    void 카드_선택_후_상태_메시지에_선택된_카드_정보가_반영된다() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        TestStompSession session = createSession();

        String subscribeUrlFormat = "/topic/room/%s/gameState";
        String requestUrlFormat = "/app/room/%s/minigame/command";

        MessageCollector<WebSocketResponse<MiniGameStateMessage>> responses = session.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                new TypeReference<>() {
                }
        );

        sendStartGame(session, joinCode, host.getName().value());
        responses.get(); // LOADING
        responses.get(); // PLAYING

        String playerName = "꾹이";
        int cardIndex = 0;

        MiniGameMessage request = new MiniGameMessage(
                CommandType.SELECT_CARD,
                objectMapper.valueToTree(new SelectCardCommand(playerName, cardIndex))
        );

        // when
        session.send(String.format(requestUrlFormat, joinCode.value()), request);

        // then
        MiniGameStateMessage result = responses.get().data();

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

        joinCode = new JoinCode("ABCDE");
        Room room = new Room(joinCode, new PlayerName("플레이어1"), null);
        room.joinGuest(new PlayerName("플레이어2"), null);
        room.addMiniGame(new PlayerName("플레이어1"), MiniGameType.CARD_GAME.createMiniGame());
        roomRepository.save(room);

        MessageCollector<WebSocketResponse<MiniGameStateMessage>> responses1 = session1.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                new TypeReference<>() {
                }
        );
        MessageCollector<WebSocketResponse<MiniGameStateMessage>> responses2 = session2.subscribe(
                String.format(subscribeUrlFormat, joinCode.value()),
                new TypeReference<>() {
                }
        );

        sendStartGame(session1, joinCode, room.getHost().getName().value());
        responses1.get();
        responses2.get();

        responses1.get();
        responses2.get();

        // when
        String[] playerNames = {"플레이어1", "플레이어2"};
        int[] cardIndices = {0, 1};
        TestStompSession[] sessions = {session1, session2};
        MessageCollector[] responses = {responses1, responses2};

        for (int i = 0; i < playerNames.length; i++) {
            MiniGameMessage request = new MiniGameMessage(
                    CommandType.SELECT_CARD,
                    objectMapper.valueToTree(new SelectCardCommand(playerNames[i], cardIndices[i]))
            );
            sessions[i].send(String.format(requestUrlFormat, this.joinCode.value()), request);
            responses[i].get();
        }

        // then
        MiniGameStateMessage finalState = responses1.get().data();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(finalState.allSelected()).isTrue();

            long selectedCount = finalState.cardInfoMessages().stream()
                    .mapToLong(cardInfo -> cardInfo.selected() ? 1 : 0)
                    .sum();
            softly.assertThat(selectedCount).isEqualTo(2);
        });
    }

    private void sendStartGame(TestStompSession session, JoinCode joinCode, String hostName) {
        String requestUrlFormat = "/app/room/%s/minigame/command";
        MiniGameMessage startGameRequest = new MiniGameMessage(
                CommandType.START_CARD_GAME,
                objectMapper.valueToTree(new StartMiniGameCommand(hostName))
        );
        session.send(String.format(requestUrlFormat, joinCode.value()), startGameRequest);
    }
}
