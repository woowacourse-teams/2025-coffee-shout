import { useState, useMemo } from 'react';
import { useAutoTestLogger } from '../../hooks/useAutoTestLogger';
import { usePanelResize } from '../../hooks/usePanelResize';
import { checkIsTouchDevice } from '../../../utils/checkIsTouchDevice';
import AutoTestLogList from './AutoTestLogList/AutoTestLogList';
import AutoTestLogFilterBar from './AutoTestLogFilterBar/AutoTestLogFilterBar';
import * as S from './AutoTestLogPanel.styled';

type Props = {
  isIframeOpen?: boolean;
};

/**
 * AutoTest 로그 패널 컴포넌트입니다.
 * 오토테스트 디버깅 로그를 표시합니다.
 */
const AutoTestLogPanel = ({ isIframeOpen = false }: Props) => {
  const isTopWindow = useMemo(() => {
    if (typeof window === 'undefined') return false;
    try {
      return window.self === window.top;
    } catch {
      return false;
    }
  }, []);

  const isTouchDevice = useMemo(() => checkIsTouchDevice(), []);

  const [open, setOpen] = useState(false);
  const [selectedContext, setSelectedContext] = useState<string | null>(null);

  const initialPanelHeight = useMemo(() => {
    return 400;
  }, []);

  const { logs } = useAutoTestLogger();
  const { panelHeight, handleResizeStart } = usePanelResize(initialPanelHeight);

  /**
   * 필터링된 로그 목록을 반환합니다.
   */
  const filteredLogs = useMemo(() => {
    if (!selectedContext) return logs;
    return logs.filter((log) => log.context === selectedContext);
  }, [logs, selectedContext]);

  /**
   * 고유한 context 목록을 추출합니다.
   */
  const availableContexts = useMemo(() => {
    const contexts = new Set<string>();
    logs.forEach((log) => {
      if (log.context) contexts.add(log.context);
    });
    return Array.from(contexts);
  }, [logs]);

  if (!isTopWindow || isTouchDevice) return null;

  // IframePreviewToggle이 열려있지 않으면 토글 버튼도 표시하지 않음
  if (!isIframeOpen) {
    return null;
  }

  // 패널이 열려있지 않으면 토글 버튼만 표시
  if (!open) {
    return (
      <S.ToggleButton type="button" onClick={() => setOpen(true)}>
        AutoTest Logs
      </S.ToggleButton>
    );
  }

  return (
    <S.Panel height={panelHeight}>
      <S.ResizeHandle onPointerDown={handleResizeStart} />
      <S.Header>
        <S.Title>AutoTest Logs</S.Title>
        <S.HeaderActions>
          <S.CloseButton type="button" onClick={() => setOpen(false)}>
            ✕
          </S.CloseButton>
        </S.HeaderActions>
      </S.Header>

      <AutoTestLogFilterBar
        contexts={availableContexts}
        selectedContext={selectedContext}
        onContextChange={setSelectedContext}
      />

      <S.Content>
        <AutoTestLogList logs={filteredLogs} />
      </S.Content>
    </S.Panel>
  );
};

export default AutoTestLogPanel;
