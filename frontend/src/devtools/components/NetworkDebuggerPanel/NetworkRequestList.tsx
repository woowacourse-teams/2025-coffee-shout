import styled from '@emotion/styled';
import { NetworkRequest } from '../../types/network';

const List = styled.div`
  display: flex;
  flex-direction: column;
  height: 100%;
`;

const ListHeader = styled.div`
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  font-weight: 600;
  font-size: 11px;
  color: #666;
  position: sticky;
  top: 0;
  z-index: 1;
`;

const HeaderCell = styled.div`
  padding: 0 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
`;

const ListBody = styled.div`
  flex: 1;
  overflow-y: auto;
`;

const RequestRow = styled.div<{ selected: boolean }>`
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  cursor: pointer;
  background: ${({ selected }) => (selected ? '#e8f0fe' : '#ffffff')};
  transition: background 0.1s ease;

  &:hover {
    background: ${({ selected }) => (selected ? '#e8f0fe' : '#f8f9fa')};
  }
`;

const RequestCell = styled.div`
  padding: 0 8px;
  font-size: 12px;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
`;

const TypeBadge = styled.span<{ type: 'fetch' | 'websocket' }>`
  display: inline-block;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  background: ${({ type }) => (type === 'fetch' ? '#e8f5e9' : '#fff3e0')};
  color: ${({ type }) => (type === 'fetch' ? '#2e7d32' : '#e65100')};
`;

const ContextBadge = styled.span`
  display: inline-block;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: 600;
  background: #e3f2fd;
  color: #1976d2;
`;

const UrlText = styled.span`
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 11px;
`;

const StatusText = styled.span<{ color: string }>`
  color: ${({ color }) => color};
  font-weight: 500;
`;

const EmptyState = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
`;

const EmptyText = styled.p`
  margin: 0;
  font-size: 13px;
`;

type Props = {
  requests: NetworkRequest[];
  selectedRequestId: string | null;
  onSelectRequest: (id: string) => void;
};

const NetworkRequestList = ({ requests, selectedRequestId, onSelectRequest }: Props) => {
  const getStatusColor = (request: NetworkRequest): string => {
    const status = request.status;
    if (!status) return '#999';
    if (status === 'NETWORK_ERROR') return '#d93025';
    if (request.type === 'websocket') {
      // WebSocket Status 101은 성공적으로 연결됨을 의미
      if (status === 101 || request.connectionStatus === 'open') return '#0f9d58';
      if (request.connectionStatus === 'error') return '#d93025';
      if (request.connectionStatus === 'closed') return '#999';
      return '#0f9d58';
    }
    if (typeof status === 'number') {
      if (status >= 200 && status < 300) return '#0f9d58';
      if (status >= 300 && status < 400) return '#f4b400';
      if (status >= 400) return '#d93025';
    }
    return '#999';
  };

  const getStatusText = (request: NetworkRequest): string => {
    if (request.type === 'websocket') {
      // WebSocket은 Status 101로 표시 (구글 개발자 도구와 동일)
      if (request.status === 101) {
        return '101';
      }
      // fallback
      if (request.connectionStatus === 'open') return '101';
      if (request.connectionStatus === 'closed') return 'Closed';
      if (request.connectionStatus === 'error') return 'Error';
      return '101';
    }
    if (request.status === 'NETWORK_ERROR') return 'Error';
    return String(request.status || '-');
  };

  const formatTime = (timestamp: number): string => {
    const date = new Date(timestamp);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    const milliseconds = date.getMilliseconds().toString().padStart(3, '0');
    return `${hours}:${minutes}:${seconds}.${milliseconds}`;
  };

  if (requests.length === 0) {
    return (
      <EmptyState>
        <EmptyText>No network requests</EmptyText>
      </EmptyState>
    );
  }

  return (
    <List>
      <ListHeader>
        <HeaderCell style={{ width: '80px' }}>Type</HeaderCell>
        <HeaderCell style={{ width: '100px' }}>Context</HeaderCell>
        <HeaderCell style={{ flex: 1 }}>URL</HeaderCell>
        <HeaderCell style={{ width: '80px' }}>Status</HeaderCell>
        <HeaderCell style={{ width: '100px' }}>Time</HeaderCell>
      </ListHeader>
      <ListBody>
        {requests.map((request) => (
          <RequestRow
            key={request.id}
            selected={selectedRequestId === request.id}
            onClick={() => onSelectRequest(request.id)}
          >
            <RequestCell style={{ width: '80px' }}>
              <TypeBadge type={request.type}>{request.type}</TypeBadge>
            </RequestCell>
            <RequestCell style={{ width: '100px' }}>
              <ContextBadge>{request.context}</ContextBadge>
            </RequestCell>
            <RequestCell style={{ flex: 1 }} title={request.url}>
              <UrlText>{request.url}</UrlText>
            </RequestCell>
            <RequestCell style={{ width: '80px' }}>
              <StatusText color={getStatusColor(request)}>{getStatusText(request)}</StatusText>
            </RequestCell>
            <RequestCell style={{ width: '100px' }}>{formatTime(request.timestamp)}</RequestCell>
          </RequestRow>
        ))}
      </ListBody>
    </List>
  );
};

export default NetworkRequestList;
