import { Page, expect } from '@playwright/test';

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
