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
