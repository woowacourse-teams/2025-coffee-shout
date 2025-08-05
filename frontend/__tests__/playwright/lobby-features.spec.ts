import { expect, test } from '@playwright/test';
import { TestHelper } from './helpers/TestHelper';

test.describe('로비 기능', () => {
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
  });

  test.afterEach(async () => {
    if (hostHelper?.page && !hostHelper.page.isClosed()) {
      await hostHelper.page.close();
    }
    if (guestHelper?.page && !guestHelper.page.isClosed()) {
      await guestHelper.page.close();
    }
  });

  test.describe('게임 시작 및 준비 버튼 접근성', () => {
    test('호스트는 초기에 게임 대기중 버튼이 보이고, 게스트가 준비하면 게임 시작 버튼으로 바뀐다.', async () => {
      // 초기에는 호스트에게 게임 대기중 버튼이 보임
      await expect(hostHelper.button.getGameWaitingButton()).toBeVisible();
      await expect(hostHelper.button.getGameStartButton()).not.toBeVisible();

      // 게스트가 준비하기 버튼을 클릭
      await expect(guestHelper.button.getGameReadyButton()).toBeVisible();
      await guestHelper.button.clickGameReady();

      // 게스트 화면에서 준비 완료 버튼으로 변경됨
      await expect(guestHelper.button.getGameReadyCompleteButton()).toBeVisible();
      await expect(guestHelper.button.getGameReadyButton()).not.toBeVisible();

      // 호스트 화면에서 게임 시작 버튼이 나타남
      await expect(hostHelper.button.getGameStartButton()).toBeVisible();
      await expect(hostHelper.button.getGameWaitingButton()).not.toBeVisible();

      // 미니게임을 먼저 선택해야 게임 시작이 가능할 수 있음
      await hostHelper.button.clickSectionTab('미니게임');
      await hostHelper.button.clickMiniGame('카드게임');

      // 게임 시작 버튼 클릭 가능 확인
      await hostHelper.button.clickGameStart();
    });

    test('게스트는 게임 시작 버튼을 누를 수 없고, 준비하기/준비 완료 버튼만 사용할 수 있다.', async () => {
      // 게스트에게는 게임 시작 버튼이 없고 준비하기 버튼이 표시됨
      await expect(guestHelper.button.getGameStartButton()).not.toBeVisible();
      await expect(guestHelper.button.getGameReadyButton()).toBeVisible();
      await expect(guestHelper.button.getGameReadyButton()).toBeEnabled();

      // 준비하기 버튼 클릭
      await guestHelper.button.clickGameReady();

      // 준비 완료 버튼으로 변경됨
      await expect(guestHelper.button.getGameReadyCompleteButton()).toBeVisible();
      await expect(guestHelper.button.getGameReadyButton()).not.toBeVisible();
    });
  });

  test.describe('참여자 리스트 섹션', () => {
    test('모든 참가자의 이름이 표시된다.', async () => {
      // 참가자 섹션으로 이동
      await hostHelper.button.clickSectionTab('참가자');
      await guestHelper.button.clickSectionTab('참가자');

      // 호스트 페이지에서 참가자 확인
      await expect(hostHelper.page.getByText('호스트')).toBeVisible();
      await expect(hostHelper.page.getByText('게스트')).toBeVisible();

      // 게스트 페이지에서도 참가자 확인
      await expect(guestHelper.page.getByText('호스트')).toBeVisible();
      await expect(guestHelper.page.getByText('게스트')).toBeVisible();
    });

    test('내 정보는 상단에 별도로 구분되어 표시된다.', async () => {
      // 참가자 섹션인지 확인
      await hostHelper.button.clickSectionTab('참가자');
      await guestHelper.button.clickSectionTab('참가자');

      // 호스트 페이지에서 자신의 정보가 상단에 표시되는지 확인
      const hostPlayerCard = hostHelper.page.locator('[data-testid="player-card"]').first();
      await expect(hostPlayerCard).toContainText('호스트');

      // 게스트 페이지에서 자신의 정보가 상단에 표시되는지 확인
      const guestPlayerCard = guestHelper.page.locator('[data-testid="player-card"]').first();
      await expect(guestPlayerCard).toContainText('게스트');
    });

    test('새로운 사용자가 접속하면 실시간으로 리스트에 추가된다.', async ({ browser }) => {
      // 새로운 컨텍스트와 페이지 생성
      const newContext = await browser.newContext();
      const newGuestPage = await newContext.newPage();
      const newGuestHelper = new TestHelper(newGuestPage);

      await newGuestHelper.joinRoomFlow(joinCode, '새게스트', 0);

      // 기존 참가자들 페이지에서 새 게스트 확인
      await expect(hostHelper.page.getByText('새게스트')).toBeVisible();
      await expect(guestHelper.page.getByText('새게스트')).toBeVisible();

      await newContext.close();
    });
  });

  test.describe('미니게임 선택 권한', () => {
    test('호스트는 미니게임을 선택할 수 있다.', async () => {
      // 미니게임 섹션으로 이동
      await hostHelper.button.clickSectionTab('미니게임');

      // 미니게임 선택 버튼들이 활성화되어 있는지 확인
      const cardGameButton = hostHelper.page.getByRole('button', { name: '카드게임' });
      await expect(cardGameButton).toBeVisible();
      await expect(cardGameButton).toBeEnabled();

      // 미니게임 선택
      await hostHelper.button.clickMiniGame('카드게임');

      // 선택된 상태 확인 (aria-pressed 속성)
      await expect(cardGameButton).toHaveAttribute('aria-pressed', 'true');
    });

    test('게스트는 미니게임을 선택할 수 없다.', async () => {
      // 미니게임 섹션으로 이동
      await guestHelper.button.clickSectionTab('미니게임');

      // 미니게임 버튼들이 비활성화되어 있는지 확인
      const cardGameButton = guestHelper.page.getByRole('button', { name: '카드게임' });
      await expect(cardGameButton).toBeVisible();
      await expect(cardGameButton).toBeDisabled();
    });

    test('호스트가 미니게임을 선택하면 게스트 화면에 실시간으로 반영된다.', async () => {
      // 호스트가 미니게임 선택
      await hostHelper.button.clickSectionTab('미니게임');
      await hostHelper.button.clickMiniGame('카드게임');

      // 게스트 화면에서 미니게임 섹션으로 이동
      await guestHelper.button.clickSectionTab('미니게임');

      // 게스트 화면에 선택된 미니게임이 반영되었는지 확인
      const guestCardGameButton = guestHelper.page.getByRole('button', { name: '카드게임' });
      await expect(guestCardGameButton).toHaveAttribute('aria-pressed', 'true');
    });
  });

  test.describe('초대코드 공유 기능', () => {
    test('호스트와 게스트는 초대코드 공유 버튼을 클릭하면 모달이 열린다.', async () => {
      // 호스트 공유 버튼 테스트
      await hostHelper.button.clickShare();
      await expect(hostHelper.page.getByRole('dialog')).toBeVisible();
      await expect(hostHelper.page.getByRole('heading', { name: '초대 코드' })).toBeVisible();
      await hostHelper.button.clickCloseModal();

      // TODO: 게스트 공유 버튼 추가 후 주석 해제
      // 게스트 공유 버튼 테스트
      await guestHelper.button.clickShare();
      await expect(guestHelper.page.getByRole('dialog')).toBeVisible();
      await expect(guestHelper.page.getByRole('heading', { name: '초대 코드' })).toBeVisible();
      await guestHelper.button.clickCloseModal();
    });

    test('모달에는 제목, 내용, 초대코드 번호가 포함되어 있다.', async () => {
      // 공유 모달 열기
      await hostHelper.button.clickShare();

      // 모달 제목 확인
      await expect(hostHelper.page.getByRole('heading', { name: '초대 코드' })).toBeVisible();

      // 모달 내용 확인
      await expect(hostHelper.page.getByText('초대코드를 복사하여')).toBeVisible();
      await expect(hostHelper.page.getByText('친구들을 초대해보아요')).toBeVisible();

      // 초대 코드 번호 확인
      const codeElement = hostHelper.page.getByRole('heading', { level: 4 }).last();
      await expect(codeElement).toBeVisible();
      const displayedCode = await codeElement.textContent();
      expect(displayedCode?.trim()).toBe(joinCode);
    });

    test('클립보드 복사 아이콘 클릭 시 토스트 메시지가 표시된다.', async () => {
      // 공유 모달 열기
      await hostHelper.button.clickShare();

      // 클립보드 API 모킹
      await hostHelper.page.evaluate(() => {
        Object.defineProperty(navigator, 'clipboard', {
          value: {
            writeText: async () => {
              return Promise.resolve();
            },
          },
          writable: true,
        });
      });

      // alert 이벤트 리스너를 미리 설정
      let alertMessage = '';
      const dialogPromise = new Promise<void>((resolve) => {
        hostHelper.page.on('dialog', async (dialog) => {
          alertMessage = dialog.message();
          await dialog.accept();
          resolve();
        });
      });

      // 복사 아이콘 클릭
      await hostHelper.button.clickCopyIcon();

      // alert 메시지가 나타날 때까지 대기
      await dialogPromise;
      expect(alertMessage).toBe('초대 코드가 복사되었습니다.');
    });
  });
});
