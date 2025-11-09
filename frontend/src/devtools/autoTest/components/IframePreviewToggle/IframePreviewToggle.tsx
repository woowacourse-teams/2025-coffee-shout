import { useEffect, useMemo, useState, type MouseEvent } from 'react';
import { useLocation } from 'react-router-dom';
import { checkIsTouchDevice } from '../../../../utils/checkIsTouchDevice';
import { MINI_GAME_NAME_MAP } from '@/types/miniGame/common';
import { isTopWindow } from '@/devtools/common/utils/isTopWindow';
import {
  HOST_IFRAME_NAME,
  PRIMARY_GUEST_IFRAME_NAME,
  useIframeRegistry,
} from '@/devtools/autoTest/hooks/useIframeRegistry';
import { useGameSequenceSelector } from '@/devtools/autoTest/hooks/useGameSequenceSelector';
import { useIframeTestPostMessage } from '@/devtools/autoTest/hooks/useIframeTestPostMessage';
import * as S from './IframePreviewToggle.styled';

const IframePreviewToggle = () => {
  const location = useLocation();
  const [open, setOpen] = useState<boolean>(false);

  const topWindow = isTopWindow();
  const isTouchDevice = useMemo(() => checkIsTouchDevice(), []);
  const isRootPath = location.pathname === '/';

  const {
    iframeNames,
    iframeRefs,
    iframeHeight,
    useMinHeight,
    canAddMore,
    addGuestIframe,
    removeIframe,
    setIframeRef,
  } = useIframeRegistry();

  const {
    gameSequence,
    availableGames,
    isGameSelectionExpanded,
    toggleGameSelectionExpanded,
    setGameSelectionExpanded,
    handleGameToggle,
  } = useGameSequenceSelector();

  const {
    isRunning,
    isPaused,
    iframePaths,
    handleStartTest,
    handleStopTest,
    handlePauseTest,
    handleResumeTest,
  } = useIframeTestPostMessage({
    isOpen: open,
    iframeNames,
    gameSequence,
    iframeRefs,
  });

  useEffect(() => {
    // 경로가 바뀌면 닫아준다 (예상치 못한 잔상 방지)
    setOpen(false);
    setGameSelectionExpanded(false);
  }, [location.pathname, setGameSelectionExpanded]);

  if (!topWindow || !isRootPath || isTouchDevice) return null;

  return (
    <S.Container>
      <S.ToggleBar>
        <S.ToggleButton type="button" onClick={() => setOpen((v) => !v)}>
          {open ? 'Hide iframes' : 'Show iframes'}
        </S.ToggleButton>
        {open && (
          <>
            <S.GameSelectionContainer $isExpanded={isGameSelectionExpanded}>
              <S.GameSelectionLabel type="button" onClick={toggleGameSelectionExpanded}>
                게임 선택
              </S.GameSelectionLabel>
              {isGameSelectionExpanded && (
                <S.GameSelectionButtons>
                  {availableGames.map((game) => {
                    const isSelected = gameSequence.includes(game);
                    const order = gameSequence.indexOf(game) + 1; // 선택 순서 (1부터 시작)
                    return (
                      <S.GameSelectionButton
                        key={game}
                        type="button"
                        $selected={isSelected}
                        disabled={isRunning}
                        onClick={() => handleGameToggle(game)}
                      >
                        {MINI_GAME_NAME_MAP[game]}
                        <S.GameOrderBadge $visible={isSelected && order > 0}>
                          {order > 0 ? order : ''}
                        </S.GameOrderBadge>
                      </S.GameSelectionButton>
                    );
                  })}
                </S.GameSelectionButtons>
              )}
            </S.GameSelectionContainer>
            <S.PlayButton type="button" onClick={handleStartTest} disabled={isRunning}>
              {isRunning ? '테스트 실행 중...' : '재생'}
            </S.PlayButton>
            {isRunning && !isPaused && (
              <S.PauseButton type="button" onClick={handlePauseTest}>
                일시 중지
              </S.PauseButton>
            )}
            {isRunning && isPaused && (
              <S.ResumeButton type="button" onClick={handleResumeTest}>
                재개
              </S.ResumeButton>
            )}
            {isRunning && (
              <S.StopButton type="button" onClick={handleStopTest}>
                테스트 중단
              </S.StopButton>
            )}
          </>
        )}
      </S.ToggleBar>
      {open && (
        <S.IframePanel>
          {iframeNames.map((name, index) => {
            const path = iframePaths[name] || '';
            const labelText = path ? `${name} - ${path}` : name;
            // 맨 마지막 iframe만 삭제 가능 (host와 guest1은 항상 삭제 불가)
            const lastIndex = iframeNames.length - 1;
            const isLastIframe = index === lastIndex;
            const canDelete =
              name !== HOST_IFRAME_NAME && name !== PRIMARY_GUEST_IFRAME_NAME && isLastIframe;

            return (
              <S.IframeWrapper key={name} $height={iframeHeight} $useMinHeight={useMinHeight}>
                <S.IframeLabel>{labelText}</S.IframeLabel>
                {canDelete && (
                  <S.DeleteButton
                    type="button"
                    data-delete-button
                    onClick={(e: MouseEvent<HTMLButtonElement>) => {
                      e.stopPropagation();
                      removeIframe(name);
                    }}
                    aria-label={`Remove ${name}`}
                  >
                    ×
                  </S.DeleteButton>
                )}
                <S.PreviewIframe
                  ref={(el) => setIframeRef(name, el)}
                  name={name}
                  title={`preview-${index === 0 ? 'left' : 'right'}`}
                  src="/"
                />
              </S.IframeWrapper>
            );
          })}
          {canAddMore && (
            <S.IframeWrapper $height={iframeHeight} $useMinHeight={useMinHeight}>
              <S.AddIframeButton type="button" onClick={addGuestIframe}>
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
