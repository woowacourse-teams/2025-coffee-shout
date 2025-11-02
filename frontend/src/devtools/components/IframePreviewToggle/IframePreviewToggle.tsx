import { useEffect, useMemo, useRef, useState } from 'react';
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

  const handleAddIframe = () => {
    const guestNames = iframeNames.filter((name) => name.startsWith('guest'));
    const maxGuestNumber = guestNames.reduce((max, name) => {
      const match = name.match(/^guest(\d+)$/);
      if (match) {
        const num = parseInt(match[1], 10);
        return Math.max(max, num);
      }
      return max;
    }, 0);
    const nextGuestNumber = maxGuestNumber + 1;
    const newGuestName = `guest${nextGuestNumber}`;
    setIframeNames((prev) => [...prev, newGuestName]);
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

            return (
              <S.IframeWrapper key={name}>
                <S.IframeLabel>{labelText}</S.IframeLabel>
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
          <S.IframeWrapper>
            <S.AddIframeButton type="button" onClick={handleAddIframe}>
              +
            </S.AddIframeButton>
          </S.IframeWrapper>
        </S.IframePanel>
      )}
    </S.Container>
  );
};

export default IframePreviewToggle;
