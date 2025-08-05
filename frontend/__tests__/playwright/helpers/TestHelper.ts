import { Page } from '@playwright/test';
import { CardGameHelper, LobbyHelper, RoomHelper, RouletteHelper } from './features';
import { ButtonHelper, FormHelper, NavigationHelper } from './shared';

/**
 * 통합 테스트 헬퍼 클래스
 */
export class TestHelper {
  public navigation: NavigationHelper;
  public form: FormHelper;
  public button: ButtonHelper;
  public room: RoomHelper;
  public lobby: LobbyHelper;
  public cardGame: CardGameHelper;
  public roulette: RouletteHelper;

  constructor(public page: Page) {
    this.navigation = new NavigationHelper(page);
    this.form = new FormHelper(page);
    this.button = new ButtonHelper(page);
    this.room = new RoomHelper(page);
    this.lobby = new LobbyHelper(page);
    this.cardGame = new CardGameHelper(page);
    this.roulette = new RouletteHelper(page);
  }

  /**
   * 호스트 방 생성 전체 플로우
   */
  async createRoomFlow(hostName: string, menuIndex: number = 0) {
    await this.navigation.goToHomePage();
    await this.button.clickCreateRoom();
    await this.room.expectToBeOnNicknamePage();

    await this.form.fillNickname(hostName);
    await this.button.clickGoToMenuSelection();
    await this.room.expectToBeOnMenuSelectionPage();

    await this.form.selectMenu(menuIndex);
    await this.button.clickGoToCreateRoom();
    await this.lobby.expectToBeOnLobbyPage();
  }

  /**
   * 게스트 방 참가 전체 플로우
   */
  async joinRoomFlow(joinCode: string, guestName: string, menuIndex: number = 0) {
    await this.navigation.goToHomePage();
    await this.button.clickJoinRoomFromHome();
    await this.room.expectToBeOnJoinCodePage();

    await this.form.fillJoinCode(joinCode);
    await this.button.clickJoinRoomFromModal();
    await this.room.expectToBeOnNicknamePage();

    await this.form.fillNickname(guestName);
    await this.button.clickGoToMenuSelection();
    await this.room.expectToBeOnMenuSelectionPage();

    await this.form.selectMenu(menuIndex);
    await this.button.clickEnterRoom();
    await this.lobby.expectToBeOnLobbyPage();
  }

  /**
   * 호스트가 방 생성 후 초대 코드 가져오기
   */
  async createRoomAndGetJoinCode(hostName: string, menuIndex: number = 0): Promise<string> {
    await this.createRoomFlow(hostName, menuIndex);
    await this.button.clickShare();
    await this.page.waitForSelector('h4');

    const joinCodeElement = this.page.getByRole('heading', { level: 4 }).last();
    const code = await joinCodeElement.textContent();

    if (!code) {
      throw new Error('방 참가 코드를 찾을 수 없습니다.');
    }

    await this.button.clickCloseModal();

    return code.trim();
  }
}
