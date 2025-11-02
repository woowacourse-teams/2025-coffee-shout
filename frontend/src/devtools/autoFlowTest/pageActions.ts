import {
  clickElement,
  clickElementWithClickEvent,
  findElement,
  findFirstElementByTestIdPrefix,
  typeInInput,
  wait,
  waitForElement,
  waitForPathChange,
  waitForPathPattern,
  extractJoinCode,
  DELAY_BETWEEN_ACTIONS,
  DELAY_AFTER_API,
} from './domUtils';

export type PageActionContext = {
  role: 'host' | 'guest';
  joinCode?: string;
  playerName: string;
  iframeName?: string;
};

export type PageAction = {
  pathPattern: string | RegExp;
  role?: 'host' | 'guest';
  execute: (context: PageActionContext) => Promise<void>;
};

// 홈 페이지 액션
const homePageHostAction = async () => {
  const createButton = await waitForElement('create-room-button', 5000);
  if (!createButton) {
    console.warn('[AutoTest] Create room button not found');
    return;
  }
  await clickElement(createButton);
};

const homePageGuestAction = async (context: PageActionContext) => {
  const joinButton = await waitForElement('join-room-button', 5000);
  if (!joinButton) {
    console.warn('[AutoTest] Join room button not found');
    return;
  }
  await clickElement(joinButton);

  // joinCode 입력 처리 (모달이 열림)
  await wait(DELAY_BETWEEN_ACTIONS);

  if (context.joinCode) {
    const joinCodeInput = await waitForElement('join-code-input', 10000);
    if (!joinCodeInput) {
      console.warn('[AutoTest] Join code input not found');
      return;
    }
    await typeInInput(joinCodeInput, context.joinCode);

    const enterButton = await waitForElement('enter-room-button', 10000);
    if (!enterButton) {
      console.warn('[AutoTest] Enter room button not found');
      return;
    }
    await clickElement(enterButton);
  }
};

// 이름 입력 페이지 액션 (공통)
const entryNamePageAction = async (context: PageActionContext) => {
  await wait(300);

  const nameInput = await waitForElement('player-name-input', 10000);
  if (!nameInput) {
    console.warn('[AutoTest] Name input not found');
    return;
  }
  await typeInInput(nameInput, context.playerName);

  const goToMenuButton = await waitForElement('go-to-menu-button', 10000);
  if (!goToMenuButton) {
    console.warn('[AutoTest] Go to menu button not found');
    return;
  }
  await clickElement(goToMenuButton);
};

// 메뉴 선택 페이지 액션 (공통 로직, 제출 버튼만 다름)
const entryMenuPageAction = async (context: PageActionContext) => {
  await wait(DELAY_BETWEEN_ACTIONS);

  let firstCategory = findFirstElementByTestIdPrefix('category-card-');
  let attempts = 0;
  while (!firstCategory && attempts < 30) {
    await wait(100);
    firstCategory = findFirstElementByTestIdPrefix('category-card-');
    attempts++;
  }
  if (firstCategory) {
    await clickElement(firstCategory);
  }

  await wait(DELAY_BETWEEN_ACTIONS);

  let firstMenuItem = findFirstElementByTestIdPrefix('menu-item-');
  attempts = 0;
  while (!firstMenuItem && attempts < 30) {
    await wait(100);
    firstMenuItem = findFirstElementByTestIdPrefix('menu-item-');
    attempts++;
  }
  if (firstMenuItem) {
    await clickElement(firstMenuItem);
  }

  await wait(DELAY_BETWEEN_ACTIONS);

  const temperatureButton =
    (await waitForElement('temperature-option-ICE', 5000)) ||
    (await waitForElement('temperature-option-HOT', 5000));
  if (temperatureButton) {
    await clickElement(temperatureButton);
  } else {
    console.warn('[AutoTest] Temperature button not found');
  }

  await wait(DELAY_BETWEEN_ACTIONS);

  // 역할별 제출 버튼
  const submitButtonTestId =
    context.role === 'host' ? 'create-room-submit-button' : 'join-room-submit-button';
  const submitButton = await waitForElement(submitButtonTestId, 10000);
  if (!submitButton) {
    console.warn(`[AutoTest] ${submitButtonTestId} not found`);
    return;
  }
  await clickElement(submitButton);

  await wait(DELAY_AFTER_API);
};

// 로비 페이지 - 호스트 액션
const lobbyPageHostAction = async (context: PageActionContext) => {
  const joinCode = extractJoinCode();
  if (!joinCode) {
    console.warn('[AutoTest] Could not extract joinCode from path');
    return;
  }

  console.log('[AutoTest] Host room created, joinCode:', joinCode);

  if (window.parent && window.parent !== window) {
    window.parent.postMessage({ type: 'JOIN_CODE_RECEIVED', joinCode }, '*');
  }

  await wait(DELAY_BETWEEN_ACTIONS);

  const miniGameToggle = await waitForElement('toggle-option-미니게임', 10000);
  if (!miniGameToggle) {
    console.warn('[AutoTest] MiniGame toggle button not found');
    return;
  }
  await clickElementWithClickEvent(miniGameToggle);

  await wait(DELAY_BETWEEN_ACTIONS * 2);

  let cardGameButton = findElement('game-action-CARD_GAME');
  let cardGameAttempts = 0;
  while (!cardGameButton && cardGameAttempts < 50) {
    await wait(200);
    cardGameButton = findElement('game-action-CARD_GAME');
    cardGameAttempts++;
  }

  if (!cardGameButton) {
    console.warn('[AutoTest] Card game button not found after waiting');
    return;
  }

  console.log('[AutoTest] Card game button found');
  await clickElementWithClickEvent(cardGameButton);

  await wait(DELAY_AFTER_API);

  console.log('[AutoTest] Host selected card game, waiting for guest to be ready...');
};

// 로비 페이지 - 게스트 액션
const lobbyPageGuestAction = async (context: PageActionContext) => {
  await wait(DELAY_BETWEEN_ACTIONS);

  const readyButton = await waitForElement('game-ready-button', 10000);
  if (!readyButton) {
    console.warn('[AutoTest] Game ready button not found');
    return;
  }
  await clickElement(readyButton);

  await wait(DELAY_AFTER_API);

  console.log('[AutoTest] Guest clicked ready button');

  if (window.parent && window.parent !== window) {
    const iframeName = window.frameElement?.getAttribute('name') || '';
    window.parent.postMessage({ type: 'GUEST_READY', iframeName }, '*');
  }
};

// 게임 시작 버튼 클릭 (메시지 수신 시)
export const handleHostGameStart = async () => {
  console.log('[AutoTest] Host received signal to start game');

  const gameStartButton = await waitForElement('game-start-button', 10000);
  if (!gameStartButton) {
    console.warn('[AutoTest] Game start button not found');
    return;
  }

  console.log('[AutoTest] All players are ready! Game start button appeared.');
  await wait(DELAY_BETWEEN_ACTIONS);
  await clickElement(gameStartButton);
  console.log('[AutoTest] Host clicked game start button');

  await wait(DELAY_AFTER_API);

  const joinCode = extractJoinCode();
  if (!joinCode) {
    console.warn('[AutoTest] Could not extract joinCode from path');
    return;
  }

  const gameResultPathPattern = new RegExp(`^/room/${joinCode}/[^/]+/result$`);

  let gameResultPathMatched = gameResultPathPattern.test(window.location.pathname);
  let pathAttempts = 0;
  while (!gameResultPathMatched && pathAttempts < 300) {
    await wait(100);
    gameResultPathMatched = gameResultPathPattern.test(window.location.pathname);
    pathAttempts++;
  }

  if (!gameResultPathMatched) {
    console.warn('[AutoTest] Game result page not reached within timeout');
    return;
  }

  await wait(500);
  await wait(DELAY_BETWEEN_ACTIONS);

  const rouletteResultButton = await waitForElement('roulette-result-button', 10000);
  if (!rouletteResultButton) {
    console.warn('[AutoTest] Roulette result button not found');
    return;
  }

  console.log('[AutoTest] Roulette result button found');
  await clickElement(rouletteResultButton);

  await wait(DELAY_AFTER_API);

  const roulettePlayPath = `/room/${joinCode}/roulette/play`;
  const roulettePathMatched = await waitForPathChange(roulettePlayPath, 10000);
  if (!roulettePathMatched) {
    console.warn('[AutoTest] Roulette play page not reached');
    return;
  }

  await wait(DELAY_BETWEEN_ACTIONS * 2);

  let rouletteSpinButton = findElement('roulette-spin-button');
  let spinAttempts = 0;
  while (!rouletteSpinButton && spinAttempts < 100) {
    await wait(200);
    rouletteSpinButton = findElement('roulette-spin-button');
    spinAttempts++;
  }

  if (!rouletteSpinButton) {
    console.warn('[AutoTest] Roulette spin button not found');
    return;
  }

  const buttonText = rouletteSpinButton.textContent?.trim();
  if (buttonText !== '룰렛 돌리기') {
    console.log(`[AutoTest] Roulette button text is "${buttonText}", waiting for "룰렛 돌리기"...`);

    let buttonTextMatches = false;
    let textAttempts = 0;
    while (!buttonTextMatches && textAttempts < 100) {
      await wait(200);
      rouletteSpinButton = findElement('roulette-spin-button');
      if (rouletteSpinButton?.textContent?.trim() === '룰렛 돌리기') {
        buttonTextMatches = true;
      }
      textAttempts++;
    }

    if (!buttonTextMatches) {
      console.warn('[AutoTest] Roulette button text did not become "룰렛 돌리기"');
      return;
    }
  }

  console.log('[AutoTest] Roulette spin button found and ready');
  await clickElement(rouletteSpinButton);
  console.log('[AutoTest] Host clicked roulette spin button');
};

// 페이지 액션 목록
export const pageActions: PageAction[] = [
  {
    pathPattern: '/',
    role: 'host',
    execute: homePageHostAction,
  },
  {
    pathPattern: '/',
    role: 'guest',
    execute: homePageGuestAction,
  },
  {
    pathPattern: '/entry/name',
    execute: entryNamePageAction,
  },
  {
    pathPattern: '/entry/menu',
    execute: entryMenuPageAction,
  },
  {
    pathPattern: /^\/room\/[^/]+\/lobby$/,
    role: 'host',
    execute: lobbyPageHostAction,
  },
  {
    pathPattern: /^\/room\/[^/]+\/lobby$/,
    role: 'guest',
    execute: lobbyPageGuestAction,
  },
];

// 경로 패턴 매칭 유틸리티
export const matchPath = (pathname: string, pattern: string | RegExp): boolean => {
  if (typeof pattern === 'string') {
    return pathname === pattern;
  }
  return pattern.test(pathname);
};

// 현재 경로에 맞는 페이지 액션 찾기
export const findPageAction = (pathname: string, role: 'host' | 'guest'): PageAction | null => {
  // 역할별 필터링 후 정확한 경로 매칭 우선, 그 다음 패턴 매칭
  const exactMatch = pageActions.find(
    (action) =>
      (!action.role || action.role === role) &&
      typeof action.pathPattern === 'string' &&
      action.pathPattern === pathname
  );

  if (exactMatch) {
    return exactMatch;
  }

  const patternMatch = pageActions.find(
    (action) =>
      (!action.role || action.role === role) &&
      typeof action.pathPattern !== 'string' &&
      matchPath(pathname, action.pathPattern)
  );

  return patternMatch || null;
};
