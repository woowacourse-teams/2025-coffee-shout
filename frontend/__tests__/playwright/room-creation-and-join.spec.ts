import { expect, test } from '@playwright/test';
import { TestHelper } from './helpers/TestHelper';

test.describe('방 생성 및 참가', () => {
  test.describe('호스트가 방을 생성하면 로비 페이지로 이동해야 한다.', () => {
    let helper: TestHelper;

    test.beforeEach(async ({ page }) => {
      helper = new TestHelper(page);
    });

    test('"방 만들기" 버튼을 클릭하면 닉네임 입력 페이지로 이동해야 한다.', async () => {
      // 메인 페이지로 이동
      await helper.navigation.goToHomePage();

      // "방 만들기" 버튼 클릭
      await helper.button.clickCreateRoom();

      // 닉네임 입력 페이지로 이동했는지 확인
      await helper.assertion.expectToBeOnNicknamePage();
    });

    test('닉네임을 입력하고 "메뉴 선택하러 가기" 버튼 클릭 시 메뉴 선택 페이지로 이동해야 한다.', async () => {
      // 메인 페이지에서 방 만들기 클릭
      await helper.navigation.goToHomePage();
      await helper.button.clickCreateRoom();
      await helper.assertion.expectToBeOnNicknamePage();

      // 닉네임 입력
      const hostName = '호스트';
      await helper.form.fillNickname(hostName);

      // "메뉴 선택하러 가기" 버튼 클릭
      await helper.button.clickGoToMenuSelection();

      // 메뉴 선택 페이지로 이동했는지 확인
      await helper.assertion.expectToBeOnMenuSelectionPage();
    });

    test('메뉴 셀렉트박스를 클릭해 메뉴를 선택하고 "방 만들러가기" 버튼 클릭 시 로비 페이지로 이동해야 한다.', async () => {
      // 닉네임 입력까지 진행
      await helper.navigation.goToHomePage();
      await helper.button.clickCreateRoom();
      await helper.assertion.expectToBeOnNicknamePage();

      const hostName = '호스트';
      await helper.form.fillNickname(hostName);
      await helper.button.clickGoToMenuSelection();
      await helper.assertion.expectToBeOnMenuSelectionPage();

      // 메뉴 선택
      await helper.form.selectMenu(0); // 첫 번째 메뉴 선택

      // "방 만들러 가기" 버튼 클릭
      await helper.button.clickGoToCreateRoom();

      // 로비 페이지로 이동했는지 확인
      await helper.assertion.expectToBeOnLobbyPage();

      // 호스트 이름이 참가자 목록에 표시되는지 확인
      await expect(helper.page.getByText(hostName)).toBeVisible();
    });

    test('전체 방 생성 플로우가 정상적으로 작동해야 한다.', async () => {
      const hostName = '호스트';
      await helper.createRoomFlow(hostName, 0);

      // 로비에 호스트가 표시되는지 확인
      await expect(helper.page.getByText(hostName)).toBeVisible();
      await expect(helper.page.getByRole('heading', { name: '참가자', level: 2 })).toBeVisible();
    });
  });

  test.describe('게스트가 초대 코드를 통해 방에 참가하면 로비 페이지로 이동해야 한다.', () => {
    let helper: TestHelper;
    let joinCode: string;

    test.beforeAll(async ({ browser }) => {
      // 실제 방을 먼저 생성해서 초대 코드 얻기
      const hostPage = await browser.newPage();
      const hostHelper = new TestHelper(hostPage);

      joinCode = await hostHelper.createRoomAndGetJoinCode('호스트', 0);
      await hostPage.close();
    });

    test.beforeEach(async ({ page }) => {
      helper = new TestHelper(page);
    });

    test('초대 코드를 입력하고 입장하면 닉네임 입력 페이지로 이동해야 한다.', async () => {
      // 메인 페이지로 이동
      await helper.navigation.goToHomePage();

      // "방 참가하러 가기" 버튼 클릭
      await helper.button.clickJoinRoomFromHome();

      // 초대 코드 입력 페이지인지 확인
      await helper.assertion.expectToBeOnJoinCodePage();

      // 실제 존재하는 초대 코드 입력
      await helper.form.fillJoinCode(joinCode);

      // 입장 버튼 클릭
      await helper.button.clickJoinRoomFromModal();

      // 닉네임 입력 페이지로 이동했는지 확인
      await helper.assertion.expectToBeOnNicknamePage();
    });

    test('닉네임을 입력하고 "메뉴 선택하러 가기" 버튼 클릭 시 메뉴 선택 페이지로 이동해야 한다.', async () => {
      // 초대 코드 입력 후 닉네임 페이지까지 진행
      await helper.navigation.goToHomePage();
      await helper.button.clickJoinRoomFromHome();
      await helper.assertion.expectToBeOnJoinCodePage();

      await helper.form.fillJoinCode(joinCode);
      await helper.button.clickJoinRoomFromModal();
      await helper.assertion.expectToBeOnNicknamePage();

      // 닉네임 입력
      const guestName = '게스트';
      await helper.form.fillNickname(guestName);

      // "메뉴 선택하러 가기" 버튼 클릭
      await helper.button.clickGoToMenuSelection();

      // 메뉴 선택 페이지로 이동했는지 확인
      await helper.assertion.expectToBeOnMenuSelectionPage();
    });

    test('메뉴를 선택한 뒤 "방 참가하기" 버튼 클릭 시 로비 페이지로 이동해야 한다.', async () => {
      // 메뉴 선택 페이지까지 진행
      await helper.navigation.goToHomePage();
      await helper.button.clickJoinRoomFromHome();
      await helper.assertion.expectToBeOnJoinCodePage();

      const guestName = '게스트';
      await helper.form.fillJoinCode(joinCode);
      await helper.button.clickJoinRoomFromModal();
      await helper.assertion.expectToBeOnNicknamePage();

      await helper.form.fillNickname(guestName);
      await helper.button.clickGoToMenuSelection();
      await helper.assertion.expectToBeOnMenuSelectionPage();

      // 메뉴 선택
      await helper.form.selectMenu(1); // 두 번째 메뉴 선택

      // "방 참가하기" 버튼 클릭
      await helper.button.clickEnterRoom();

      // 로비 페이지로 이동했는지 확인
      await helper.assertion.expectToBeOnLobbyPage();

      // 게스트 이름이 참가자 목록에 표시되는지 확인
      await expect(helper.page.getByText(guestName)).toBeVisible();
    });

    test('전체 방 참가 플로우가 정상적으로 작동해야 한다.', async () => {
      const guestName = '게스트';
      await helper.joinRoomFlow(joinCode, guestName, 1);

      // 로비에 게스트가 표시되는지 확인
      await expect(helper.page.getByText(guestName)).toBeVisible();
      await expect(helper.page.getByRole('heading', { name: '참가자', level: 2 })).toBeVisible();
    });
  });

  test.describe('통합 테스트: 방 생성과 참가', () => {
    test('호스트가 방을 생성하고 게스트가 참가하는 전체 플로우', async ({
      page: hostPage,
      context,
    }) => {
      const helper = new TestHelper(hostPage);

      // 1. 호스트가 방 생성 및 초대 코드 가져오기
      const hostName = '호스트';
      const joinCode = await helper.createRoomAndGetJoinCode(hostName, 0);

      // 3. 새 페이지에서 게스트가 참가
      const guestPage = await context.newPage();
      const guestHelper = new TestHelper(guestPage);

      const guestName = '게스트';
      await guestHelper.joinRoomFlow(joinCode, guestName, 1);

      // 4. 두 페이지 모두에서 참가자 확인
      await expect(hostPage.getByText(hostName)).toBeVisible();
      await expect(hostPage.getByText(guestName)).toBeVisible();

      await expect(guestPage.getByText(hostName)).toBeVisible();
      await expect(guestPage.getByText(guestName)).toBeVisible();

      await guestPage.close();
    });
  });
});
