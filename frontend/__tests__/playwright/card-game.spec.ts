import { expect, test } from '@playwright/test';
import { TestHelper } from './helpers/TestHelper';

test.describe('카드게임', () => {
  let hostHelper: TestHelper;
  let guestHelper: TestHelper;
  let joinCode: string;

  test.beforeEach(async ({ browser }) => {
    // 호스트 페이지 설정
    const hostPage = await browser.newPage();
    hostHelper = new TestHelper(hostPage);

    // 호스트가 방 생성 및 초대 코드 획득
    joinCode = await hostHelper.createRoomAndGetJoinCode('호스트', 0);

    // 게스트 페이지 설정 및 방 참가
    const guestPage = await browser.newPage();
    guestHelper = new TestHelper(guestPage);
    await guestHelper.joinRoomFlow(joinCode, '게스트', 0);

    // 카드게임 시작: 게스트 준비 → 호스트 게임 시작
    await guestHelper.button.clickGameReady();
    await expect(guestHelper.button.getGameReadyCompleteButton()).toBeVisible();
    await expect(hostHelper.button.getGameStartButton()).toBeVisible();

    await hostHelper.button.clickSectionTab('미니게임');
    await hostHelper.button.clickMiniGame('카드게임');
    await hostHelper.button.clickGameStart();

    await hostHelper.cardGame.expectToBeOnCardGameReadyPage();
    await hostHelper.cardGame.expectToBeOnCardGamePlayPage();
  });

  test.afterEach(async () => {
    // 테스트가 완전히 끝날 때까지 잠시 대기
    await new Promise((resolve) => setTimeout(resolve, 1000));

    if (hostHelper?.page && !hostHelper.page.isClosed()) {
      await hostHelper.page.close();
    }
    if (guestHelper?.page && !guestHelper.page.isClosed()) {
      await guestHelper.page.close();
    }
  });

  test.describe('카드 선택 동작', () => {
    test('호스트가 뒤집힌 카드를 선택하면 앞면이 보인다.', async () => {
      // 뒤집힌 카드 선택
      await hostHelper.cardGame.selectCard(0);

      // 선택한 카드의 앞면이 보이는지 확인
      await hostHelper.cardGame.expectCardToBeFlipped(0);
    });

    test('게스트는 호스트가 선택한 카드의 앞면을 확인할 수 있다.', async () => {
      // 호스트가 카드 선택
      await hostHelper.cardGame.selectCard(0);

      // 게스트 페이지에서도 해당 카드의 앞면이 보이는지 확인
      await guestHelper.cardGame.expectCardToBeFlipped(0);
    });

    test('선택한 카드는 내 상단 카드 영역에 표시된다.', async () => {
      // 호스트가 카드 선택
      await hostHelper.cardGame.selectCard(1);

      // 상단 영역에 선택한 카드가 표시되는지 확인
      await hostHelper.cardGame.expectSelectedCardInTopArea();

      // 게스트도 카드 선택
      await guestHelper.cardGame.selectCard(2);

      // 게스트의 상단 영역에도 선택한 카드가 표시되는지 확인
      await guestHelper.cardGame.expectSelectedCardInTopArea();
    });

    test('한 번 선택한 후에는 다른 카드를 선택할 수 없다.', async () => {
      // 호스트가 첫 번째 카드 선택
      await hostHelper.cardGame.selectCard(0);

      // 다른 카드들이 비활성화되었는지 확인
      await hostHelper.cardGame.expectOtherCardsToBeDisabled();
    });
  });

  test.describe('라운드 진행 조건', () => {
    test('모든 사용자가 카드를 선택하면 자동으로 다음 라운드로 진행된다.', async () => {
      // 현재 라운드 확인
      await hostHelper.cardGame.expectCurrentRound(1);
      await guestHelper.cardGame.expectCurrentRound(1);

      // 호스트와 게스트가 모두 카드 선택
      await hostHelper.cardGame.selectCard(0);
      await guestHelper.cardGame.selectCard(1);

      // 다음 라운드로 자동 진행되는지 확인
      await hostHelper.cardGame.expectCurrentRound(2);
      await guestHelper.cardGame.expectCurrentRound(2);
    });
  });

  test.describe('게임 종료 이후', () => {
    test.beforeEach(() => {
      test.setTimeout(300000);
    });

    test('모든 라운드가 끝나면 카드게임 결과 화면이 표시된다.', async () => {
      // 2라운드 진행 - 호스트와 게스트 모두 카드 선택
      for (let round = 1; round <= 2; round++) {
        // 현재 라운드 확인
        await hostHelper.cardGame.expectCurrentRound(round);
        await guestHelper.cardGame.expectCurrentRound(round);

        // 호스트와 게스트가 각각 카드 선택
        await hostHelper.cardGame.selectCard(0);
        await guestHelper.cardGame.selectCard(1);
      }

      // 결과 화면 표시 확인
      await hostHelper.cardGame.expectToBeOnCardGameResultPage();
      await guestHelper.cardGame.expectToBeOnCardGameResultPage();
    });

    test('호스트는 "룰렛 현황 보러가기" 버튼을 눌러 다음 화면으로 이동할 수 있다.', async () => {
      // 2라운드 진행 - 호스트와 게스트 모두 카드 선택
      for (let round = 1; round <= 2; round++) {
        // 현재 라운드 확인
        await hostHelper.cardGame.expectCurrentRound(round);
        await guestHelper.cardGame.expectCurrentRound(round);

        // 호스트와 게스트가 각각 카드 선택
        await hostHelper.cardGame.selectCard(0);
        await guestHelper.cardGame.selectCard(1);
      }

      // 결과 화면 표시 확인
      await hostHelper.cardGame.expectToBeOnCardGameResultPage();

      // 호스트가 룰렛 현황 보러가기 버튼 클릭
      await hostHelper.button.clickGoToRouletteStatus();

      // 룰렛 페이지로 이동 확인
      await hostHelper.roulette.expectToBeOnRoulettePage();
    });

    test('게스트는 호스트가 이동할 때까지 대기 상태가 된다.', async () => {
      // 2라운드 진행 - 호스트와 게스트 모두 카드 선택
      for (let round = 1; round <= 2; round++) {
        // 현재 라운드 확인
        await hostHelper.cardGame.expectCurrentRound(round);
        await guestHelper.cardGame.expectCurrentRound(round);

        // 호스트와 게스트가 각각 카드 선택
        await hostHelper.cardGame.selectCard(0);
        await guestHelper.cardGame.selectCard(1);
      }

      // 결과 화면 표시 확인
      await hostHelper.cardGame.expectToBeOnCardGameResultPage();

      // 게스트에게는 대기 메시지가 표시되는지 확인
      await guestHelper.roulette.expectWaitingMessage();

      // 호스트가 이동하면 게스트도 함께 이동
      await hostHelper.button.clickGoToRouletteStatus();
      await guestHelper.roulette.expectToBeOnRoulettePage();
    });
  });
});
