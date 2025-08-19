import { Page } from '@playwright/test';

/**
 * 버튼 상호작용을 위한 베이스 헬퍼
 */
export class BaseButtonHelper {
  constructor(protected page: Page) {}

  // 공통 버튼 상호작용 메서드들
  private async clickButtonByText(name: string | RegExp) {
    await this.page.getByRole('button', { name }).click();
  }

  private getButtonByText(name: string | RegExp) {
    return this.page.getByRole('button', { name });
  }

  // 범용 헬퍼 (급할 때 직접 사용 가능)
  async clickButton(name: string | RegExp) {
    await this.clickButtonByText(name);
  }

  getButton(name: string | RegExp) {
    return this.getButtonByText(name);
  }
}

/**
 * 의미있는 버튼 상호작용 헬퍼 함수들
 */
export class ButtonHelper extends BaseButtonHelper {
  // === 방 생성/참가 관련 ===
  async clickCreateRoom() {
    await this.clickButton('방 만들기');
  }

  async clickJoinRoomFromHome() {
    await this.clickButton('방 참가하러 가기');
  }

  async clickJoinRoomFromModal() {
    await this.clickButton('입장');
  }

  async clickGoToMenuSelection() {
    await this.clickButton('메뉴 선택하러 가기');
  }

  async clickSkipDescriptionIfExists() {
    const skipButton = this.getButton('건너뛰기');
    const isVisible = await skipButton.isVisible();
    if (isVisible) {
      await skipButton.click();
    }
  }

  async clickGoToCreateRoom() {
    await this.clickButton('방 만들러 가기');
  }

  async clickEnterRoom() {
    await this.clickButton('방 참가하기');
  }

  // === 로비 ===
  async clickGameStart() {
    await this.clickButton('게임 시작');
  }

  async clickGameReady() {
    await this.clickButton('준비하기');
  }

  // 게임 상태 버튼들 - 자주 체크하므로 get 메서드로 제공
  getGameWaitingButton() {
    return this.getButton(/게임 대기중/);
  }

  getGameStartButton() {
    return this.getButton('게임 시작');
  }

  getGameReadyButton() {
    return this.getButton('준비하기');
  }

  getGameReadyCompleteButton() {
    return this.getButton('준비 완료!');
  }

  async clickSectionTab(sectionName: '참가자' | '미니게임') {
    await this.clickButton(sectionName);
  }

  async clickMiniGame(gameName: '카드게임') {
    await this.clickButton(gameName);
  }

  // === 모달/공유 ===
  async clickShare() {
    await this.clickButton('공유');
  }

  async clickCopyIcon() {
    await this.clickButton('초대 코드 복사');
  }

  async clickCloseModal() {
    await this.clickButton('모달 닫기');
  }

  // === 룰렛 ===
  async clickGoToRouletteStatus() {
    await this.clickButton('룰렛 현황 보러가기');
  }
}
