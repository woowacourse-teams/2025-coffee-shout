import { Page, expect } from '@playwright/test';

/**
 * 룰렛 관련 헬퍼 함수들
 */
export class RouletteHelper {
  constructor(private page: Page) {}

  // === 룰렛 페이지 검증 ===
  async expectToBeOnRoulettePage() {
    await expect(this.page).toHaveURL(/\/room\/.*\/roulette/);
    await expect(this.page.getByText(/룰렛/)).toBeVisible();
  }
}
