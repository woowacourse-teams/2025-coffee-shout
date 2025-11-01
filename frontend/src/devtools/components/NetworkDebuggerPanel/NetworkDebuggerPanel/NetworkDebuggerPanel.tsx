import { useState, useMemo } from 'react';
import { useNetworkCollector } from '../../../hooks/useNetworkCollector';
import { usePanelResize } from '../../../hooks/usePanelResize';
import { useVerticalResize } from '../../../hooks/useVerticalResize';
import NetworkFilterBar from '../NetworkFilterBar/NetworkFilterBar';
import NetworkRequestList from '../NetworkRequestList/NetworkRequestList';
import NetworkRequestDetail from '../NetworkRequestDetail/NetworkRequestDetail';
import * as S from '../NetworkDebuggerPanel.styled';

/**
 * 네트워크 디버거 패널 컴포넌트입니다.
 * Fetch 및 WebSocket 요청을 모니터링하고 디버깅할 수 있는 도구를 제공합니다.
 */
const NetworkDebuggerPanel = () => {
  const isTopWindow = useMemo(() => {
    if (typeof window === 'undefined') return false;
    try {
      return window.self === window.top;
    } catch {
      return false;
    }
  }, []);

  const [open, setOpen] = useState(false);
  const [selectedContext, setSelectedContext] = useState<string | null>(null);
  const [selectedType, setSelectedType] = useState<'fetch' | 'websocket' | null>(null);
  const [selectedRequest, setSelectedRequest] = useState<string | null>(null);

  const { requests, clearRequests } = useNetworkCollector();
  const { panelHeight, handleResizeStart } = usePanelResize(400);
  const { detailWidthPercent, handleVerticalResizeStart, contentRef } = useVerticalResize();

  if (!isTopWindow) return null;

  /**
   * 고유한 context 목록을 추출합니다.
   * MAIN이 있으면 첫 번째로 배치하고, 나머지는 정렬합니다.
   */
  const availableContexts = useMemo(() => {
    const contexts = new Set<string>();
    requests.forEach((req) => {
      if (req.context) contexts.add(req.context);
    });

    const sorted = Array.from(contexts).sort();
    const mainIndex = sorted.indexOf('MAIN');
    if (mainIndex > 0) {
      sorted.splice(mainIndex, 1);
      sorted.unshift('MAIN');
    }

    return sorted;
  }, [requests]);

  /**
   * 필터링된 요청 목록을 반환합니다.
   */
  const filteredRequests = useMemo(() => {
    return requests.filter((req) => {
      if (selectedContext && req.context !== selectedContext) return false;
      if (selectedType && req.type !== selectedType) return false;
      return true;
    });
  }, [requests, selectedContext, selectedType]);

  const selectedRequestData = useMemo(() => {
    if (!selectedRequest) return null;
    return requests.find((req) => req.id === selectedRequest) || null;
  }, [requests, selectedRequest]);

  if (!open) {
    return (
      <S.ToggleButton type="button" onClick={() => setOpen(true)}>
        Network
      </S.ToggleButton>
    );
  }

  return (
    <S.Panel height={panelHeight}>
      <S.ResizeHandle onMouseDown={handleResizeStart} />
      <S.Header>
        <S.Title>Network</S.Title>
        <S.HeaderActions>
          <S.ClearButton type="button" onClick={clearRequests}>
            Clear
          </S.ClearButton>
          <S.CloseButton type="button" onClick={() => setOpen(false)}>
            ✕
          </S.CloseButton>
        </S.HeaderActions>
      </S.Header>

      <NetworkFilterBar
        contexts={availableContexts}
        selectedContext={selectedContext}
        selectedType={selectedType}
        onContextChange={setSelectedContext}
        onTypeChange={setSelectedType}
      />

      <S.Content ref={contentRef}>
        <S.RequestListSection detailWidthPercent={selectedRequestData ? detailWidthPercent : 0}>
          <NetworkRequestList
            requests={filteredRequests}
            selectedRequestId={selectedRequest}
            onSelectRequest={setSelectedRequest}
          />
          {selectedRequestData && (
            <S.VerticalResizeHandle onMouseDown={handleVerticalResizeStart} />
          )}
        </S.RequestListSection>
        {selectedRequestData && (
          <S.DetailSection widthPercent={detailWidthPercent}>
            <NetworkRequestDetail request={selectedRequestData} />
          </S.DetailSection>
        )}
      </S.Content>
    </S.Panel>
  );
};

export default NetworkDebuggerPanel;
