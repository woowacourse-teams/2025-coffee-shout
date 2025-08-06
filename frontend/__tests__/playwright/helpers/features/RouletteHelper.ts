import { Page, expect } from '@playwright/test';

/**
 * 룰렛 관련 헬퍼 함수들
 */
export class RouletteHelper {
  constructor(private page: Page) {}

  // === 룰렛 페이지 검증 ===
  async expectToBeOnRoulettePage() {
    await this.page.waitForURL(/\/room\/.*\/roulette\/play/, { timeout: 15000 });
    await expect(this.page.getByText('룰렛 현황')).toBeVisible();
  }

  // === 대기 상태 검증 ===
  async expectWaitingMessage() {
    await expect(this.page.getByText('대기 중')).toBeVisible();
  }
}
