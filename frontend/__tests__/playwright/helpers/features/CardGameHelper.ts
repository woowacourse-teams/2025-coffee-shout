import { Page, expect } from '@playwright/test';

/**
 * 카드게임 관련 헬퍼 함수들
 */
export class CardGameHelper {
  constructor(private page: Page) {}

  // === 페이지 검증 ===
  async expectToBeOnCardGamePage() {
    await expect(this.page).toHaveURL(/\/room\/.*\/CARD_GAME\/play/);
    await expect(this.page.getByText('카드를 선택해주세요')).toBeVisible();
  }

  async expectToBeOnCardGameResultPage() {
    await expect(this.page).toHaveURL(/\/room\/.*\/CARD_GAME\/result/);
    await expect(this.page.getByText('카드게임 결과')).toBeVisible();
  }

  // === 카드 상태 검증 ===
  async expectCardToBeFlipped(cardIndex: number) {
    const cardElement = this.page.locator(`[data-testid="card-${cardIndex}"]`);
    await expect(cardElement).toHaveAttribute('data-flipped', 'true');
  }

  async expectSelectedCardInTopArea() {
    const topCardArea = this.page.locator('[data-testid="selected-card-area"]');
    await expect(topCardArea).toBeVisible();
    await expect(topCardArea.locator('[data-testid^="card-"]')).toBeVisible();
  }

  async expectOtherCardsToBeDisabled() {
    const allCards = this.page.locator('[data-testid^="card-"]');
    const cardCount = await allCards.count();

    // 선택된 카드 외의 모든 카드가 비활성화되었는지 확인
    for (let i = 0; i < cardCount; i++) {
      const card = allCards.nth(i);
      const isSelected = await card.getAttribute('data-selected');
      if (isSelected !== 'true') {
        await expect(card).toBeDisabled();
      }
    }
  }

  // === 게임 진행 검증 ===
  async expectCurrentRound(roundNumber: number) {
    await expect(this.page.getByText(`라운드 ${roundNumber}`)).toBeVisible();
  }

  // === 카드게임 액션 ===
  async selectCard(cardIndex: number) {
    const cardElement = this.page.locator(`[data-testid="card-${cardIndex}"]`);
    await cardElement.click();
  }

  async waitForRoundTimer() {
    // 10초 타이머 대기 (약간의 여유시간 포함)
    await this.page.waitForTimeout(11000);
  }

  async playAllRounds() {
    // 3라운드 진행 (라운드 수는 실제 게임 규칙에 따라 조정)
    for (let round = 1; round <= 3; round++) {
      // 현재 라운드에서 카드 선택
      const availableCards = this.page.locator(
        '[data-testid^="card-"]:not([data-selected="true"])'
      );
      const cardCount = await availableCards.count();

      if (cardCount > 0) {
        await availableCards.first().click();
      }

      // 다음 라운드로 진행될 때까지 대기
      if (round < 3) {
        await this.page.waitForTimeout(2000);
      }
    }

    // 게임 결과 화면으로 이동 대기
    await this.page.waitForURL(/\/room\/.*\/mini-game\/result/);
  }
}
