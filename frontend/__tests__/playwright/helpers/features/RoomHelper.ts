import { Page, expect } from '@playwright/test';

/**
 * 방 생성/참가 관련 헬퍼 함수들
 */
export class RoomHelper {
  constructor(private page: Page) {}

  // === 방 생성/참가 플로우 페이지 검증 ===
  async expectToBeOnNicknamePage() {
    await expect(this.page).toHaveURL(/\/entry\/name/);
    await expect(this.page.getByPlaceholder('닉네임을 입력해주세요')).toBeVisible();
  }

  async expectToBeOnMenuSelectionPage() {
    await expect(this.page).toHaveURL(/\/entry\/menu/);
    await expect(this.page.getByRole('combobox')).toBeVisible();
  }

  async expectToBeOnJoinCodePage() {
    // 메인 페이지에서 모달이 열린 상태
    await expect(this.page).toHaveURL('/');
    await expect(this.page.getByPlaceholder('ex) ABCDE')).toBeVisible();
  }
}
