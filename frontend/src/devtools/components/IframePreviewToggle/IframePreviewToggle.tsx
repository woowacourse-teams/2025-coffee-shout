import { useEffect, useMemo, useRef, useState, type MouseEvent } from 'react';
import { useLocation } from 'react-router-dom';
import { checkIsTouchDevice } from '../../../utils/checkIsTouchDevice';
import * as S from './IframePreviewToggle.styled';

type TestMessage =
  | { type: 'START_TEST'; role: 'host' | 'guest'; joinCode?: string }
  | { type: 'JOIN_CODE_RECEIVED'; joinCode: string }
  | { type: 'GUEST_READY'; iframeName?: string }
  | { type: 'CLICK_GAME_START' }
  | { type: 'PATH_CHANGE'; iframeName: string; path: string }
  | { type: 'TEST_COMPLETED' }
  | { type: 'RESET_TO_HOME' };

const IframePreviewToggle = () => {
  const location = useLocation();
  const [open, setOpen] = useState<boolean>(false);
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [iframeNames, setIframeNames] = useState<string[]>(['host', 'guest1']);
  const [iframePaths, setIframePaths] = useState<{ [key: string]: string }>({});
  const [guestReadyState, setGuestReadyState] = useState<{ [guestName: string]: boolean }>({});
  const iframeRefs = useRef<{ [key: string]: HTMLIFrameElement | null }>({});
  const joinCodeRef = useRef<string | null>(null);

  const isTopWindow = useMemo(() => {
    if (typeof window === 'undefined') return false;
    try {
      return window.self === window.top;
    } catch {
      return false;
    }
  }, []);

  const isTouchDevice = useMemo(() => checkIsTouchDevice(), []);
  const isRootPath = location.pathname === '/';

  useEffect(() => {
    // 경로가 바뀌면 닫아준다 (예상치 못한 잔상 방지)
    setOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    if (!open) return;

    const handleMessage = (event: MessageEvent<TestMessage>) => {
      if (event.data.type === 'JOIN_CODE_RECEIVED') {
        const { joinCode } = event.data;
        joinCodeRef.current = joinCode;

        const guestNames = iframeNames.filter((name) => name.startsWith('guest'));
        guestNames.forEach((guestName) => {
          const guestIframe = iframeRefs.current[guestName];
          if (guestIframe?.contentWindow) {
            guestIframe.contentWindow.postMessage(
              { type: 'START_TEST', role: 'guest', joinCode, iframeName: guestName },
              '*'
            );
          }
        });
      } else if (event.data.type === 'GUEST_READY') {
        const { iframeName } = event.data;
        if (iframeName) {
          setGuestReadyState((prev) => ({
            ...prev,
            [iframeName]: true,
          }));
        }
      } else if (event.data.type === 'PATH_CHANGE') {
        const { iframeName, path } = event.data;
        setIframePaths((prev) => ({
          ...prev,
          [iframeName]: path,
        }));
      } else if (event.data.type === 'TEST_COMPLETED') {
        setIsRunning(false);
      }
    };

    window.addEventListener('message', handleMessage);

    return () => {
      window.removeEventListener('message', handleMessage);
    };
  }, [open, iframeNames]);

  useEffect(() => {
    if (!open) return;

    const guestNames = iframeNames.filter((name) => name.startsWith('guest'));
    const allGuestsReady =
      guestNames.length > 0 && guestNames.every((guestName) => guestReadyState[guestName] === true);

    if (allGuestsReady) {
      const hostIframe = iframeRefs.current.host;
      if (hostIframe?.contentWindow) {
        hostIframe.contentWindow.postMessage({ type: 'CLICK_GAME_START' }, '*');
      }
    }
  }, [open, iframeNames, guestReadyState]);

  const iframeHeight = useMemo(() => {
    return iframeNames.length <= 4 ? '100%' : 'auto';
  }, [iframeNames.length]);

  const useMinHeight = useMemo(() => {
    return iframeNames.length > 4;
  }, [iframeNames.length]);

  const canAddMore = useMemo(() => {
    return iframeNames.length < 9;
  }, [iframeNames.length]);

  const handleAddIframe = () => {
    if (!canAddMore) return;

    const guestNames = iframeNames.filter((name) => name.startsWith('guest'));
    const usedNumbers = new Set<number>();
    guestNames.forEach((name) => {
      const match = name.match(/^guest(\d+)$/);
      if (match) {
        const num = parseInt(match[1], 10);
        if (num >= 1 && num <= 8) {
          usedNumbers.add(num);
        }
      }
    });

    // 1~8 중 사용 가능한 가장 작은 번호 찾기
    let nextGuestNumber: number | null = null;
    for (let i = 1; i <= 8; i++) {
      if (!usedNumbers.has(i)) {
        nextGuestNumber = i;
        break;
      }
    }

    if (nextGuestNumber === null) {
      // 1~8이 모두 사용 중
      return;
    }

    const newGuestName = `guest${nextGuestNumber}`;
    setIframeNames((prev) => [...prev, newGuestName]);
  };

  const handleDeleteIframe = (name: string) => {
    if (name === 'host' || name === 'guest1') return;

    // 맨 마지막 iframe만 삭제 가능
    const lastIndex = iframeNames.length - 1;
    const lastIframeName = iframeNames[lastIndex];
    if (name !== lastIframeName) return;

    setIframeNames((prev) => prev.filter((n) => n !== name));
    delete iframeRefs.current[name];
    setGuestReadyState((prev) => {
      const newState = { ...prev };
      delete newState[name];
      return newState;
    });
  };

  const handleStartTest = () => {
    setIsRunning(true);
    joinCodeRef.current = null;
    setGuestReadyState({});

    iframeNames.forEach((name) => {
      const iframe = iframeRefs.current[name];
      if (iframe?.contentWindow) {
        iframe.contentWindow.postMessage({ type: 'RESET_TO_HOME' }, '*');
      }
    });

    setTimeout(() => {
      const hostIframe = iframeRefs.current.host;
      if (hostIframe?.contentWindow) {
        hostIframe.contentWindow.postMessage({ type: 'START_TEST', role: 'host' }, '*');
      }
    }, 500);
  };

  if (!isTopWindow || !isRootPath || isTouchDevice) return null;

  return (
    <S.Container>
      <S.ToggleBar>
        {open && (
          <S.PlayButton type="button" onClick={handleStartTest} disabled={isRunning}>
            {isRunning ? '테스트 실행 중...' : '재생'}
          </S.PlayButton>
        )}
        <S.ToggleButton type="button" onClick={() => setOpen((v) => !v)}>
          {open ? 'Hide iframes' : 'Show iframes'}
        </S.ToggleButton>
      </S.ToggleBar>
      {open && (
        <S.IframePanel>
          {iframeNames.map((name, index) => {
            const path = iframePaths[name] || '';
            const labelText = path ? `${name} - ${path}` : name;
            // 맨 마지막 iframe만 삭제 가능 (host와 guest1은 항상 삭제 불가)
            const lastIndex = iframeNames.length - 1;
            const isLastIframe = index === lastIndex;
            const canDelete = name !== 'host' && name !== 'guest1' && isLastIframe;

            return (
              <S.IframeWrapper key={name} $height={iframeHeight} $useMinHeight={useMinHeight}>
                <S.IframeLabel>{labelText}</S.IframeLabel>
                {canDelete && (
                  <S.DeleteButton
                    type="button"
                    data-delete-button
                    onClick={(e: MouseEvent<HTMLButtonElement>) => {
                      e.stopPropagation();
                      handleDeleteIframe(name);
                    }}
                    aria-label={`Remove ${name}`}
                  >
                    ×
                  </S.DeleteButton>
                )}
                <S.PreviewIframe
                  ref={(el) => {
                    if (el) iframeRefs.current[name] = el;
                  }}
                  name={name}
                  title={`preview-${index === 0 ? 'left' : 'right'}`}
                  src="/"
                />
              </S.IframeWrapper>
            );
          })}
          {canAddMore && (
            <S.IframeWrapper $height={iframeHeight} $useMinHeight={useMinHeight}>
              <S.AddIframeButton type="button" onClick={handleAddIframe}>
                +
              </S.AddIframeButton>
            </S.IframeWrapper>
          )}
        </S.IframePanel>
      )}
    </S.Container>
  );
};

export default IframePreviewToggle;
