import { useEffect, useState } from 'react';
import { networkCollector, NetworkRequest, RequestType, RequestContext } from '../networkCollector';
import { Container, Header, ToggleButton, FilterBar } from './DebuggerPanel.styled';
import { RequestList } from './RequestList';

type TypeFilter = 'all' | RequestType;
type ContextFilter = 'all' | RequestContext;

export const DebuggerPanel = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [requests, setRequests] = useState<NetworkRequest[]>([]);
  const [typeFilter, setTypeFilter] = useState<TypeFilter>('all');
  const [contextFilter, setContextFilter] = useState<ContextFilter>('all');

  useEffect(() => {
    // 초기 요청 로드
    setRequests(networkCollector.getRequests());

    // 새 요청 구독
    const unsubscribe = networkCollector.subscribe(() => {
      setRequests(networkCollector.getRequests());
    });

    return unsubscribe;
  }, []);

  // 필터링된 요청
  const filteredRequests = requests.filter((request) => {
    const matchType = typeFilter === 'all' || request.type === typeFilter;
    const matchContext = contextFilter === 'all' || request.context === contextFilter;
    return matchType && matchContext;
  });

  // 고유한 컨텍스트 목록
  const contexts = Array.from(new Set(requests.map((r) => r.context))).sort() as RequestContext[];

  return (
    <>
      <ToggleButton onClick={() => setIsOpen(!isOpen)} $isOpen={isOpen}>
        {isOpen ? '◀' : '▶'}
      </ToggleButton>
      {isOpen && (
        <Container>
          <Header>Network Debugger</Header>
          <FilterBar>
            <div>
              <label>Type: </label>
              <select
                value={typeFilter}
                onChange={(e) => setTypeFilter(e.target.value as TypeFilter)}
              >
                <option value="all">All</option>
                <option value="fetch">Fetch</option>
                <option value="websocket">WebSocket</option>
              </select>
            </div>
            <div>
              <label>Context: </label>
              <select
                value={contextFilter}
                onChange={(e) => setContextFilter(e.target.value as ContextFilter)}
              >
                <option value="all">All</option>
                {contexts.map((ctx) => (
                  <option key={ctx} value={ctx}>
                    {ctx}
                  </option>
                ))}
              </select>
            </div>
          </FilterBar>
          <RequestList requests={filteredRequests} />
        </Container>
      )}
    </>
  );
};
