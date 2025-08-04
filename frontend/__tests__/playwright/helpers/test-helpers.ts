import { Page, expect } from '@playwright/test';

/**
 * 페이지 네비게이션 헬퍼 함수들
 */
export class NavigationHelper {
  constructor(private page: Page) {}

  async goToHomePage() {
    await this.page.goto('/');
    await expect(this.page).toHaveTitle(/Coffee Shout|커피빵|2025-coffee-shout/i);
  }

  async waitForPageLoad(expectedUrl?: string | RegExp) {
    await this.page.waitForLoadState('networkidle');
    if (expectedUrl) {
      await expect(this.page).toHaveURL(expectedUrl);
    }
  }
}

/**
 * 입력 폼 관련 헬퍼 함수들
 */
export class FormHelper {
  constructor(private page: Page) {}

  async fillNickname(nickname: string) {
    const nicknameInput = this.page.getByPlaceholder('닉네임을 입력해주세요');
    await nicknameInput.fill(nickname);
    await expect(nicknameInput).toHaveValue(nickname);
  }

  async fillJoinCode(joinCode: string) {
    const joinCodeInput = this.page.getByPlaceholder('ex) ABCDE');
    await joinCodeInput.fill(joinCode);
    await expect(joinCodeInput).toHaveValue(joinCode);
  }

  async selectMenu(menuIndex: number = 0) {
    // 셀렉트박스 클릭 (role="combobox" 사용)
    await this.page.getByRole('combobox').click();

    // 옵션 선택 (role="option" 사용)
    const options = this.page.getByRole('option');
    await options.nth(menuIndex).click();
  }
}

/**
 * 버튼 클릭 헬퍼 함수들
 */
export class ButtonHelper {
  constructor(private page: Page) {}

  async clickCreateRoom() {
    await this.page.getByRole('button', { name: '방 만들기' }).click();
  }

  async clickJoinRoomFromHome() {
    // 메인 페이지에서 "방 참가하러 가기" 버튼 클릭
    await this.page.getByRole('button', { name: /방.*참가|참가.*방/ }).click();
  }

  async clickJoinRoomFromModal() {
    // 모달에서 "입장" 버튼 클릭
    await this.page.getByRole('button', { name: '입장' }).click();
  }

  async clickGoToMenuSelection() {
    await this.page.getByRole('button', { name: '메뉴 선택하러 가기' }).click();
  }

  async clickGoToCreateRoom() {
    await this.page.getByRole('button', { name: '방 만들러 가기' }).click();
  }

  async clickEnterRoom() {
    await this.page.getByRole('button', { name: '방 참가하기' }).click();
  }
}

/**
 * 페이지 검증 헬퍼 함수들
 */
export class AssertionHelper {
  constructor(private page: Page) {}

  async expectToBeOnNicknamePage() {
    await expect(this.page).toHaveURL(/\/entry\/name/);
    await expect(this.page.getByPlaceholder('닉네임을 입력해주세요')).toBeVisible();
  }

  async expectToBeOnMenuSelectionPage() {
    await expect(this.page).toHaveURL(/\/entry\/menu/);
    await expect(this.page.getByRole('combobox')).toBeVisible();
  }

  async expectToBeOnLobbyPage() {
    await expect(this.page).toHaveURL(/\/room\/.*\/lobby/);
    await expect(this.page.getByRole('heading', { name: '참가자', level: 2 })).toBeVisible();
  }

  async expectToBeOnJoinCodePage() {
    // 메인 페이지에서 모달이 열린 상태
    await expect(this.page).toHaveURL('/');
    await expect(this.page.getByPlaceholder('ex) ABCDE')).toBeVisible();
  }
}

/**
 * 통합 테스트 헬퍼 클래스
 */
export class TestHelper {
  public navigation: NavigationHelper;
  public form: FormHelper;
  public button: ButtonHelper;
  public assertion: AssertionHelper;

  constructor(public page: Page) {
    this.navigation = new NavigationHelper(page);
    this.form = new FormHelper(page);
    this.button = new ButtonHelper(page);
    this.assertion = new AssertionHelper(page);
  }

  /**
   * 호스트 방 생성 전체 플로우
   */
  async createRoomFlow(hostName: string, menuIndex: number = 0) {
    await this.navigation.goToHomePage();
    await this.button.clickCreateRoom();
    await this.assertion.expectToBeOnNicknamePage();

    await this.form.fillNickname(hostName);
    await this.button.clickGoToMenuSelection();
    await this.assertion.expectToBeOnMenuSelectionPage();

    await this.form.selectMenu(menuIndex);
    await this.button.clickGoToCreateRoom();
    await this.assertion.expectToBeOnLobbyPage();
  }

  /**
   * 게스트 방 참가 전체 플로우
   */
  async joinRoomFlow(joinCode: string, guestName: string, menuIndex: number = 0) {
    await this.navigation.goToHomePage();
    await this.button.clickJoinRoomFromHome();
    await this.assertion.expectToBeOnJoinCodePage();

    await this.form.fillJoinCode(joinCode);
    await this.button.clickJoinRoomFromModal();
    await this.assertion.expectToBeOnNicknamePage();

    await this.form.fillNickname(guestName);
    await this.button.clickGoToMenuSelection();
    await this.assertion.expectToBeOnMenuSelectionPage();

    await this.form.selectMenu(menuIndex);
    await this.button.clickEnterRoom();
    await this.assertion.expectToBeOnLobbyPage();
  }

  /**
   * 호스트가 방 생성 후 초대 코드 가져오기
   */
  async createRoomAndGetJoinCode(hostName: string, menuIndex: number = 0): Promise<string> {
    await this.createRoomFlow(hostName, menuIndex);
    await this.page.getByRole('button', { name: '공유' }).click();
    await this.page.waitForSelector('h4');

    const joinCodeElement = this.page.getByRole('heading', { level: 4 }).last();
    const code = await joinCodeElement.textContent();

    if (!code) {
      throw new Error('방 참가 코드를 찾을 수 없습니다.');
    }

    return code.trim();
  }
}
