import { Page, expect } from '@playwright/test';

/**
 * 카드게임 관련 헬퍼 함수들
 */
export class CardGameHelper {
  constructor(private page: Page) {}

  // === 페이지 검증 ===
  async expectToBeOnCardGameReadyPage() {
    await this.page.waitForURL(/\/room\/.*\/CARD_GAME\/ready/);
    await expect(this.page.getByText(/3|2|1/)).toBeVisible();
  }

  async expectToBeOnCardGamePlayPage() {
    await this.page.waitForURL(/\/room\/.*\/CARD_GAME\/play/);
    await expect(this.page.getByText('카드를 골라주세요!')).toBeVisible();
    await expect(this.page.locator('[data-testid^="card-"]').first()).toBeVisible();
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
    await expect(topCardArea.locator('[data-testid="card-selected-round-1"]')).toBeVisible();
  }

  // 특정 라운드의 선택된 카드 확인 (더 정밀한 버전)
  async expectSelectedCardForRound(roundNumber: number) {
    const topCardArea = this.page.locator('[data-testid="selected-card-area"]');
    await expect(topCardArea).toBeVisible();

    const selectedCard = topCardArea.locator(`[data-testid="card-selected-round-${roundNumber}"]`);
    await expect(selectedCard).toBeVisible();

    // 빈 카드가 아닌 실제 선택된 카드인지 확인
    const emptyCard = topCardArea.locator(`[data-testid="card-empty-round-${roundNumber}"]`);
    await expect(emptyCard).not.toBeVisible();
  }

  // 현재 라운드에 따라 동적으로 확인
  async expectSelectedCardForCurrentRound() {
    // 현재 라운드 번호를 가져옴
    const roundText = await this.page.getByText(/Round [12]/).textContent();
    const currentRound = parseInt(roundText?.replace('Round ', '') || '1');

    await this.expectSelectedCardForRound(currentRound);
  }

  // 모든 라운드 상태 확인
  async expectTopAreaCardStates(round1HasCard: boolean, round2HasCard: boolean) {
    const topCardArea = this.page.locator('[data-testid="selected-card-area"]');
    await expect(topCardArea).toBeVisible();

    // 라운드 1 카드 상태 확인
    if (round1HasCard) {
      await expect(topCardArea.locator('[data-testid="card-selected-round-1"]')).toBeVisible();
      await expect(topCardArea.locator('[data-testid="card-empty-round-1"]')).not.toBeVisible();
    } else {
      await expect(topCardArea.locator('[data-testid="card-selected-round-1"]')).not.toBeVisible();
      await expect(topCardArea.locator('[data-testid="card-empty-round-1"]')).toBeVisible();
    }

    // 라운드 2 카드 상태 확인
    if (round2HasCard) {
      await expect(topCardArea.locator('[data-testid="card-selected-round-2"]')).toBeVisible();
      await expect(topCardArea.locator('[data-testid="card-empty-round-2"]')).not.toBeVisible();
    } else {
      await expect(topCardArea.locator('[data-testid="card-selected-round-2"]')).not.toBeVisible();
      await expect(topCardArea.locator('[data-testid="card-empty-round-2"]')).toBeVisible();
    }
  }

  async expectOtherCardsToBeDisabled() {
    // 상단 영역에 선택된 카드가 나타날 때까지 기다림 (확실한 선택 완료 확인)
    const selectedCardArea = this.page.locator('[data-testid="selected-card-area"]');
    const selectedCardInTop = selectedCardArea.locator('[data-testid^="card-selected-round"]');
    await expect(selectedCardInTop.first()).toBeVisible();

    // 게임 카드들만 선택 (card-0, card-1, card-2... 형태의 숫자 인덱스)
    const gameCards = this.page.locator(
      '[data-testid^="card-"]:not([data-testid*="selected"]):not([data-testid*="empty"])'
    );

    // 각각의 선택되지 않은 카드가 비활성화되었는지 확인
    const cardCount = await gameCards.count();
    for (let i = 0; i < cardCount; i++) {
      const card = gameCards.nth(i);
      const isSelected = await card.getAttribute('data-selected');
      if (isSelected === 'false') {
        await expect(card).toBeDisabled();
      }
    }
  }

  // === 게임 진행 검증 ===
  async expectCurrentRound(roundNumber: number) {
    await expect(this.page.getByText(`Round ${roundNumber}`)).toBeVisible({ timeout: 15000 });
  }

  async waitForRoundTimer() {
    // 10초 타이머 + 여유시간 2초
    await this.page.waitForTimeout(12000);
  }

  // === 카드게임 액션 ===
  async selectCard(cardIndex: number) {
    const cardElement = this.page.locator(`[data-testid="card-${cardIndex}"]`);

    // 카드가 로드되고 클릭 가능할 때까지 대기
    await expect(cardElement).toBeVisible();
    await expect(cardElement).toBeEnabled();

    // 아직 선택되지 않은 카드인지 확인
    await expect(cardElement).toHaveAttribute('data-selected', 'false');
    await expect(cardElement).toHaveAttribute('data-flipped', 'false');

    // 카드 클릭
    await cardElement.click();

    // 클릭 후 카드가 선택되었는지 확인
    await expect(cardElement).toHaveAttribute('data-selected', 'true');
    await expect(cardElement).toHaveAttribute('data-flipped', 'true');
  }

  // 더 안전한 카드 선택 (현재 라운드 고려)
  async selectCardSafely(cardIndex: number, expectedRound: number) {
    // 현재 라운드 확인
    await this.expectCurrentRound(expectedRound);

    // 해당 라운드에 이미 선택된 카드가 없는지 확인
    const topCardArea = this.page.locator('[data-testid="selected-card-area"]');
    const emptyCard = topCardArea.locator(`[data-testid="card-empty-round-${expectedRound}"]`);
    await expect(emptyCard).toBeVisible();

    // 카드 선택
    await this.selectCard(cardIndex);

    // 선택 후 상단 영역에 카드가 나타났는지 확인
    await this.expectSelectedCardForRound(expectedRound);
  }

  // 사용할 수 없는 카드 클릭 시도 (에러 테스트용)
  async expectCardNotClickable(cardIndex: number) {
    const cardElement = this.page.locator(`[data-testid="card-${cardIndex}"]`);
    await expect(cardElement).toBeVisible();
    await expect(cardElement).toBeDisabled();
  }

  // 이미 선택된 카드 확인
  async expectCardAlreadySelected(cardIndex: number) {
    const cardElement = this.page.locator(`[data-testid="card-${cardIndex}"]`);
    await expect(cardElement).toBeVisible();
    await expect(cardElement).toHaveAttribute('data-selected', 'true');
    await expect(cardElement).toHaveAttribute('data-flipped', 'true');
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
