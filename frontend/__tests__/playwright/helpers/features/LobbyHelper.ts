import { Page, expect } from '@playwright/test';

/**
 * 로비 관련 헬퍼 함수들
 */
export class LobbyHelper {
  constructor(private page: Page) {}

  // === 로비 페이지 검증 ===
  async expectToBeOnLobbyPage() {
    await expect(this.page).toHaveURL(/\/room\/.*\/lobby/);
    await expect(this.page.getByRole('heading', { name: '참가자', level: 2 })).toBeVisible();
  }
}
