import { useState, useMemo } from 'react';
import styled from '@emotion/styled';
import { useNetworkCollector } from '../../hooks/useNetworkCollector';
import NetworkFilterBar from './NetworkFilterBar';
import NetworkRequestList from './NetworkRequestList';
import NetworkRequestDetail from './NetworkRequestDetail';

const ToggleButton = styled.button`
  position: fixed;
  bottom: 8px;
  right: 12px;
  z-index: 1001;
  appearance: none;
  border: 1px solid rgba(0, 0, 0, 0.12);
  background: #ffffff;
  color: #222;
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s ease;

  &:hover {
    background: #f6f6f6;
  }
`;

const Panel = styled.div`
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 400px;
  z-index: 1000;
  background: #ffffff;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  font-family: 'Segoe UI', system-ui, sans-serif;
  font-size: 12px;
`;

const Header = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  background: #f8f9fa;
`;

const Title = styled.h3`
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #222;
`;

const HeaderActions = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

const ClearButton = styled.button`
  appearance: none;
  border: 1px solid rgba(0, 0, 0, 0.12);
  background: #ffffff;
  color: #222;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;

  &:hover {
    background: #f0f0f0;
  }
`;

const CloseButton = styled.button`
  appearance: none;
  border: none;
  background: transparent;
  color: #666;
  padding: 4px;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;

  &:hover {
    background: rgba(0, 0, 0, 0.05);
  }
`;

const Content = styled.div`
  flex: 1;
  display: flex;
  overflow: hidden;
`;

const RequestListSection = styled.div`
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  border-right: 1px solid rgba(0, 0, 0, 0.1);
`;

const DetailSection = styled.div`
  width: 50%;
  min-width: 300px;
  overflow-y: auto;
  background: #ffffff;
`;

const NetworkDebuggerPanel = () => {
  // 메인 윈도우에서만 표시
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

  if (!isTopWindow) return null;

  // 고유한 context 목록 추출 (All, Main, 그리고 각 iframe 이름)
  const availableContexts = useMemo(() => {
    const contexts = new Set<string>();
    requests.forEach((req) => {
      if (req.context) contexts.add(req.context);
    });

    // Main이 있으면 첫 번째로, 나머지는 정렬
    const sorted = Array.from(contexts).sort();
    const mainIndex = sorted.indexOf('MAIN');
    if (mainIndex > 0) {
      // MAIN을 맨 앞으로
      sorted.splice(mainIndex, 1);
      sorted.unshift('MAIN');
    } else if (mainIndex === -1 && sorted.length > 0) {
      // MAIN이 없으면 그냥 정렬된 상태
    }

    return sorted;
  }, [requests]);

  // 필터링된 요청 목록
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
      <ToggleButton type="button" onClick={() => setOpen(true)}>
        Network
      </ToggleButton>
    );
  }

  return (
    <Panel>
      <Header>
        <Title>Network</Title>
        <HeaderActions>
          <ClearButton type="button" onClick={clearRequests}>
            Clear
          </ClearButton>
          <CloseButton type="button" onClick={() => setOpen(false)}>
            ✕
          </CloseButton>
        </HeaderActions>
      </Header>

      <NetworkFilterBar
        contexts={availableContexts}
        selectedContext={selectedContext}
        selectedType={selectedType}
        onContextChange={setSelectedContext}
        onTypeChange={setSelectedType}
      />

      <Content>
        <RequestListSection>
          <NetworkRequestList
            requests={filteredRequests}
            selectedRequestId={selectedRequest}
            onSelectRequest={setSelectedRequest}
          />
        </RequestListSection>
        {selectedRequestData && (
          <DetailSection>
            <NetworkRequestDetail request={selectedRequestData} />
          </DetailSection>
        )}
      </Content>
    </Panel>
  );
};

export default NetworkDebuggerPanel;
