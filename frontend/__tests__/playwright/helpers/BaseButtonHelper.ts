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
