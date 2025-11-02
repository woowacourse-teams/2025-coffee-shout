type TestMessage =
  | { type: 'START_TEST'; role: 'host' | 'guest'; joinCode?: string; iframeName?: string }
  | { type: 'JOIN_CODE_RECEIVED'; joinCode: string }
  | { type: 'GUEST_READY'; iframeName?: string }
  | { type: 'CLICK_GAME_START' }
  | { type: 'TEST_COMPLETED' }
  | { type: 'RESET_TO_HOME' };

const DELAY_BETWEEN_ACTIONS = 250;
const DELAY_AFTER_API = 1000;

const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

const findElement = (testId: string): HTMLElement | null => {
  return document.querySelector(`[data-testid="${testId}"]`);
};

// const findElementByText = (text: string, tagName = 'button'): HTMLElement | null => {
//   const elements = Array.from(document.querySelectorAll(tagName));
//   return elements.find((el) => el.textContent?.trim() === text) as HTMLElement | null;
// };

const clickElement = async (element: HTMLElement | null) => {
  if (!element) {
    console.warn('[AutoTest] Element not found');
    return;
  }

  // React의 pointer 이벤트만 사용하여 시뮬레이션
  const pointerDownEvent = new PointerEvent('pointerdown', {
    bubbles: true,
    cancelable: true,
    pointerId: 1,
    pointerType: 'mouse',
    clientX: 0,
    clientY: 0,
  });

  const pointerUpEvent = new PointerEvent('pointerup', {
    bubbles: true,
    cancelable: true,
    pointerId: 1,
    pointerType: 'mouse',
    clientX: 0,
    clientY: 0,
  });

  element.dispatchEvent(pointerDownEvent);
  await wait(10);
  element.dispatchEvent(pointerUpEvent);
  await wait(DELAY_BETWEEN_ACTIONS);
};

const clickElementWithClickEvent = async (element: HTMLElement | null) => {
  if (!element) {
    console.warn('[AutoTest] Element not found');
    return;
  }

  // onClick 이벤트 핸들러를 가진 요소 (예: ToggleButton)
  const clickEvent = new MouseEvent('click', {
    bubbles: true,
    cancelable: true,
    view: window,
  });

  element.dispatchEvent(clickEvent);
  await wait(DELAY_BETWEEN_ACTIONS);
};

const typeInInput = async (element: HTMLElement | null, value: string) => {
  if (!element || !(element instanceof HTMLInputElement)) {
    console.warn('[AutoTest] Input element not found');
    return;
  }

  element.focus();

  // React의 onChange를 트리거하기 위한 이벤트 생성
  const createInputEvent = () => {
    return new Event('input', { bubbles: true, cancelable: true });
  };

  const createChangeEvent = () => {
    return new Event('change', { bubbles: true, cancelable: true });
  };

  // value를 직접 설정하고 이벤트 발생
  element.value = '';
  const clearEvent = createInputEvent();
  element.dispatchEvent(clearEvent);
  await wait(50);

  // React가 value 변경을 감지하도록 Object.defineProperty로 설정
  const nativeInputValueSetter = Object.getOwnPropertyDescriptor(
    window.HTMLInputElement.prototype,
    'value'
  )?.set;
  if (nativeInputValueSetter) {
    nativeInputValueSetter.call(element, value);
    const inputEvent = createInputEvent();
    const changeEvent = createChangeEvent();
    element.dispatchEvent(inputEvent);
    await wait(50);
    element.dispatchEvent(changeEvent);
  } else {
    // Fallback: 직접 설정
    element.value = value;
    const inputEvent = createInputEvent();
    const changeEvent = createChangeEvent();
    element.dispatchEvent(inputEvent);
    await wait(50);
    element.dispatchEvent(changeEvent);
  }

  await wait(DELAY_BETWEEN_ACTIONS);
};

const waitForElement = async (testId: string, maxWait = 10000): Promise<HTMLElement | null> => {
  const startTime = Date.now();
  while (Date.now() - startTime < maxWait) {
    const element = findElement(testId);
    if (element) {
      await wait(100);
      return element;
    }
    await wait(100);
  }
  console.warn(`[AutoTest] Element with testId "${testId}" not found after ${maxWait}ms`);
  return null;
};

const findFirstElementByTestIdPrefix = (prefix: string): HTMLElement | null => {
  const elements = Array.from(document.querySelectorAll(`[data-testid^="${prefix}"]`));
  return (elements[0] as HTMLElement) || null;
};

const waitForPathChange = async (expectedPath: string, maxWait = 10000): Promise<boolean> => {
  const startTime = Date.now();
  while (Date.now() - startTime < maxWait) {
    if (window.location.pathname === expectedPath) {
      await wait(500);
      return true;
    }
    await wait(100);
  }
  return false;
};

const runHostFlow = async () => {
  console.log('[AutoTest] Host flow started');

  await wait(DELAY_BETWEEN_ACTIONS);

  if (window.location.pathname === '/') {
    const createButton = await waitForElement('create-room-button', 5000);
    if (!createButton) {
      console.warn('[AutoTest] Create room button not found');
      return;
    }
    await clickElement(createButton);
  }

  const pathChanged = await waitForPathChange('/entry/name', 10000);
  if (!pathChanged) {
    console.warn('[AutoTest] Failed to navigate to /entry/name');
    return;
  }

  await wait(300);

  const nameInput = await waitForElement('player-name-input', 10000);
  if (!nameInput) {
    console.warn('[AutoTest] Name input not found');
    return;
  }
  await typeInInput(nameInput, 'host');

  const goToMenuButton = await waitForElement('go-to-menu-button', 10000);
  if (!goToMenuButton) {
    console.warn('[AutoTest] Go to menu button not found');
    return;
  }
  await clickElement(goToMenuButton);

  const menuPathChanged = await waitForPathChange('/entry/menu', 10000);
  if (!menuPathChanged) {
    console.warn('[AutoTest] Failed to navigate to /entry/menu');
    return;
  }

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

  const submitButton = await waitForElement('create-room-submit-button', 10000);
  if (!submitButton) {
    console.warn('[AutoTest] Create room submit button not found');
    return;
  }
  await clickElement(submitButton);

  await wait(DELAY_AFTER_API);

  const joinCodeMatch = window.location.pathname.match(/^\/room\/([^/]+)\/lobby$/);
  if (joinCodeMatch) {
    const joinCode = joinCodeMatch[1];
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
  }

  console.log('[AutoTest] Host flow completed');
};

const handleHostGameStart = async () => {
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

  const joinCodeMatch = window.location.pathname.match(/^\/room\/([^/]+)\//);
  if (!joinCodeMatch) {
    console.warn('[AutoTest] Could not extract joinCode from path');
    return;
  }

  const joinCode = joinCodeMatch[1];
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

const runGuestFlow = async (joinCode: string, iframeName?: string) => {
  const guestName = iframeName || 'guest1';
  console.log('[AutoTest] Guest flow started, joinCode:', joinCode, 'name:', guestName);

  await wait(DELAY_BETWEEN_ACTIONS);

  if (window.location.pathname === '/') {
    const joinButton = await waitForElement('join-room-button', 5000);
    if (!joinButton) {
      console.warn('[AutoTest] Join room button not found');
      return;
    }
    await clickElement(joinButton);
  }

  await wait(DELAY_BETWEEN_ACTIONS);

  const joinCodeInput = await waitForElement('join-code-input', 10000);
  if (!joinCodeInput) {
    console.warn('[AutoTest] Join code input not found');
    return;
  }
  await typeInInput(joinCodeInput, joinCode);

  const enterButton = await waitForElement('enter-room-button', 10000);
  if (!enterButton) {
    console.warn('[AutoTest] Enter room button not found');
    return;
  }
  await clickElement(enterButton);

  const pathChanged = await waitForPathChange('/entry/name', 10000);
  if (!pathChanged) {
    console.warn('[AutoTest] Failed to navigate to /entry/name');
    return;
  }

  await wait(300);

  const nameInput = await waitForElement('player-name-input', 10000);
  if (!nameInput) {
    console.warn('[AutoTest] Name input not found');
    return;
  }
  await typeInInput(nameInput, guestName);

  const goToMenuButton = await waitForElement('go-to-menu-button', 10000);
  if (!goToMenuButton) {
    console.warn('[AutoTest] Go to menu button not found');
    return;
  }
  await clickElement(goToMenuButton);

  const menuPathChanged = await waitForPathChange('/entry/menu', 10000);
  if (!menuPathChanged) {
    console.warn('[AutoTest] Failed to navigate to /entry/menu');
    return;
  }

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

  const submitButton = await waitForElement('join-room-submit-button', 10000);
  if (!submitButton) {
    console.warn('[AutoTest] Join room submit button not found');
    return;
  }
  await clickElement(submitButton);

  await wait(DELAY_AFTER_API);

  const lobbyPathMatch = window.location.pathname.match(/^\/room\/([^/]+)\/lobby$/);
  if (lobbyPathMatch) {
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
  }

  console.log('[AutoTest] Guest flow completed');
};

export const setupAutoTestListener = () => {
  if (typeof window === 'undefined') return;

  let guestJoinCode: string | null = null;

  const messageHandler = async (event: MessageEvent<TestMessage>) => {
    if (event.data.type === 'START_TEST') {
      const { role, joinCode, iframeName } = event.data;

      if (role === 'host') {
        runHostFlow();
      } else if (role === 'guest') {
        if (joinCode) {
          guestJoinCode = joinCode;
          runGuestFlow(joinCode, iframeName);
        } else {
          console.log('[AutoTest] Guest waiting for joinCode...');
        }
      }
    } else if (event.data.type === 'JOIN_CODE_RECEIVED') {
      const { joinCode } = event.data;
      if (guestJoinCode === null) {
        guestJoinCode = joinCode;
        const iframeName = window.frameElement?.getAttribute('name') || '';
        runGuestFlow(joinCode, iframeName);
      }
    } else if (event.data.type === 'CLICK_GAME_START') {
      await handleHostGameStart();
    } else if (event.data.type === 'RESET_TO_HOME') {
      window.location.href = '/';
    }
  };

  window.addEventListener('message', messageHandler);

  return () => {
    window.removeEventListener('message', messageHandler);
  };
};
