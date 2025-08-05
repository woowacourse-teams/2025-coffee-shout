import { Page, expect } from '@playwright/test';

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

  async expectShareModalToBeOpen() {
    await expect(this.page.getByRole('dialog')).toBeVisible();
    await expect(this.page.getByRole('heading', { name: '초대 코드' })).toBeVisible();
  }
}
