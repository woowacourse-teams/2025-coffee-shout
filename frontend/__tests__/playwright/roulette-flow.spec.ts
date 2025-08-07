import { expect, test } from '@playwright/test';
import { TestHelper } from './helpers/TestHelper';

test.describe('룰렛 기능 및 당첨 결과', () => {
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

    // 카드게임 2라운드 진행
    for (let round = 1; round <= 2; round++) {
      await hostHelper.cardGame.expectCurrentRound(round);
      await guestHelper.cardGame.expectCurrentRound(round);

      await hostHelper.cardGame.selectCard(0);
      await guestHelper.cardGame.selectCard(1);
    }

    // 카드게임 결과 페이지에서 룰렛으로 이동
    await hostHelper.cardGame.expectToBeOnCardGameResultPage();
    await hostHelper.button.clickGoToRouletteStatus();

    // 룰렛 페이지 진입 확인
    await hostHelper.roulette.expectToBeOnRoulettePage();
  });

  test.afterEach(async () => {
    if (hostHelper?.page && !hostHelper.page.isClosed()) {
      await hostHelper.page.close();
    }
    if (guestHelper?.page && !guestHelper.page.isClosed()) {
      await guestHelper.page.close();
    }
  });

  test('호스트는 룰렛 돌리기 버튼을 사용할 수 있고, 게스트는 사용할 수 없다.', async () => {
    // 호스트는 룰렛 돌리기 버튼을 사용할 수 있음
    const hostSpinButton = hostHelper.page.getByRole('button', { name: /룰렛 돌리기/ });
    await expect(hostSpinButton).toBeVisible();
    await expect(hostSpinButton).toBeEnabled();

    // 게스트는 룰렛 돌리기 버튼이 비활성화되어 있음
    const guestSpinButton = guestHelper.page.getByRole('button', { name: /룰렛 돌리기/ });
    await expect(guestSpinButton).toBeDisabled();

    // 호스트가 룰렛 돌리기 실행
    await hostSpinButton.click();

    // 룰렛 실행 후 호스트의 버튼이 비활성화됨
    await expect(hostSpinButton).toBeDisabled();

    // 호스트와 게스트 모두 룰렛 결과 페이지로 자동 이동
    await hostHelper.page.waitForURL(/\/room\/.*\/roulette\/result/, { timeout: 5000 });
    await guestHelper.page.waitForURL(/\/room\/.*\/roulette\/result/, { timeout: 5000 });
  });

  test('통계 아이콘 버튼을 누르면 참가자별 룰렛 세부 확률을 확인할 수 있다.', async () => {
    // 통계 아이콘 버튼 찾아 누르기
    const statsButton = hostHelper.page.locator('button:has(img[alt="icon-button"])');
    await expect(statsButton).toBeVisible();
    await statsButton.click();

    // 통계 화면으로 전환되었는지 확인
    await expect(hostHelper.page.getByText('호스트')).toBeVisible();
    await expect(hostHelper.page.getByText('게스트')).toBeVisible();

    // 퍼센트 표시 확인
    await expect(hostHelper.page.getByText(/%/).first()).toBeVisible();

    // 다시 룰렛 화면으로 돌아가기
    await statsButton.click();

    // 룰렛 SVG 확인
    await expect(hostHelper.page.locator('svg')).toBeVisible();
  });

  test('참가자 중에 한 명이 당첨되고 당첨자를 화면에서 확인할 수 있다.', async () => {
    // 호스트가 룰렛을 돌림
    const spinButton = hostHelper.page.getByRole('button', { name: /룰렛 돌리기|시작/ });
    await spinButton.click();

    // 결과 페이지로 이동 대기
    await hostHelper.page.waitForURL(/\/room\/.*\/roulette\/result/, { timeout: 30000 });
    await guestHelper.page.waitForURL(/\/room\/.*\/roulette\/result/, { timeout: 30000 });

    // 당첨자 발표 화면 확인 (실제로는 당첨자 이름이 Headline1으로 표시됨)
    await expect(hostHelper.page.getByText(/님이 당첨되었습니다/)).toBeVisible();
    await expect(guestHelper.page.getByText(/님이 당첨되었습니다/)).toBeVisible();

    // 당첨자가 호스트 또는 게스트 중 한 명인지 확인
    const hostWinnerVisible = await hostHelper.page
      .getByText(/호스트.*님이 당첨되었습니다/)
      .isVisible();
    const guestWinnerVisible = await hostHelper.page
      .getByText(/게스트.*님이 당첨되었습니다/)
      .isVisible();

    expect(hostWinnerVisible || guestWinnerVisible).toBe(true);

    // 양쪽 화면에서 동일한 당첨자가 표시되는지 확인
    if (hostWinnerVisible) {
      await expect(guestHelper.page.getByText(/호스트.*님이 당첨되었습니다/)).toBeVisible();
    } else {
      await expect(guestHelper.page.getByText(/게스트.*님이 당첨되었습니다/)).toBeVisible();
    }
  });

  test.describe('주문 목록 페이지', () => {
    test.beforeEach(async () => {
      // 룰렛 돌린 후 주문 목록 페이지로 이동
      const spinButton = hostHelper.page.getByRole('button', { name: /룰렛 돌리기|시작/ });
      await spinButton.click();

      // 룰렛 결과 대기
      await hostHelper.page.waitForTimeout(5000);
      await hostHelper.page.waitForURL(/\/room\/.*\/roulette\/result/, { timeout: 30000 });

      // 주문 목록 페이지 진입 확인
      await hostHelper.page.waitForURL(/\/room\/.*\/order/, { timeout: 15000 });
    });

    test('주문 리스트를 확인할 수 있다.', async () => {
      // 주문 리스트 페이지 제목 확인
      await expect(hostHelper.page.getByRole('heading', { name: /주문 리스트/ })).toBeVisible();
      await expect(guestHelper.page.getByRole('heading', { name: /주문 리스트/ })).toBeVisible();

      // 메뉴 정보가 표시되는지 확인 (카드게임 결과에 따른 메뉴)
      await expect(
        hostHelper.page.getByText(/아메리카노|라떼|카푸치노|에스프레소/).first()
      ).toBeVisible();
      await expect(
        guestHelper.page.getByText(/아메리카노|라떼|카푸치노|에스프레소/).first()
      ).toBeVisible();
    });

    test('주문 리스트 상세를 확인할 수 있다.', async () => {
      // 상세 보기 토글 버튼 클릭 (DetailIcon 버튼)
      const detailButton = hostHelper.page.locator('button:has(img[alt="icon-button"])');
      await detailButton.click();

      // 상세 보기 제목 확인
      await expect(
        hostHelper.page.getByRole('heading', { name: /주문 리스트 상세/ })
      ).toBeVisible();

      // 참가자별 상세 정보 확인
      await expect(hostHelper.page.getByText('호스트').first()).toBeVisible();
      await expect(hostHelper.page.getByText('게스트').first()).toBeVisible();

      // 메뉴 상세 정보 확인
      await expect(
        hostHelper.page.getByText(/아메리카노|라떼|카푸치노|에스프레소/).first()
      ).toBeVisible();

      // 다시 간단 보기로 전환
      await detailButton.click();
      await expect(hostHelper.page.getByRole('heading', { name: /주문 리스트/ })).toBeVisible();
    });

    test('메인화면으로 가기 버튼을 누르면 메인화면으로 돌아간다.', async () => {
      // 메인화면으로 가기 버튼 클릭 (OrderPage의 실제 버튼 텍스트)
      await hostHelper.button.clickButton('메인 화면으로 가기');

      // 메인화면으로 이동 확인
      await hostHelper.page.waitForURL(/\/$/, { timeout: 15000 });
      await expect(hostHelper.page.getByRole('button', { name: /방.*만들기/ })).toBeVisible();
    });
  });
});
