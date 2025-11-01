import { NetworkRequest } from '../../../types/network';
import * as S from './NetworkRequestList.styled';

type Props = {
  requests: NetworkRequest[];
  selectedRequestId: string | null;
  onSelectRequest: (id: string) => void;
};

/**
 * 네트워크 요청 목록을 표시하는 컴포넌트입니다.
 */
const NetworkRequestList = ({ requests, selectedRequestId, onSelectRequest }: Props) => {
  /**
   * 요청의 상태에 따른 색상을 반환합니다.
   */
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

  /**
   * 요청의 상태 텍스트를 반환합니다.
   */
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

  /**
   * 타임스탬프를 포맷팅합니다.
   */
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
      <S.EmptyState>
        <S.EmptyText>No network requests</S.EmptyText>
      </S.EmptyState>
    );
  }

  return (
    <S.List>
      <S.ListHeader>
        <S.HeaderCell style={{ width: '80px' }}>Type</S.HeaderCell>
        <S.HeaderCell style={{ width: '100px' }}>Context</S.HeaderCell>
        <S.HeaderCell style={{ flex: 1 }}>URL</S.HeaderCell>
        <S.HeaderCell style={{ width: '80px' }}>Status</S.HeaderCell>
        <S.HeaderCell style={{ width: '100px' }}>Time</S.HeaderCell>
      </S.ListHeader>
      <S.ListBody>
        {requests.map((request) => (
          <S.RequestRow
            key={request.id}
            selected={selectedRequestId === request.id}
            onClick={() => onSelectRequest(request.id)}
          >
            <S.RequestCell style={{ width: '80px' }}>
              <S.TypeBadge type={request.type}>{request.type}</S.TypeBadge>
            </S.RequestCell>
            <S.RequestCell style={{ width: '100px' }}>
              <S.ContextBadge>{request.context}</S.ContextBadge>
            </S.RequestCell>
            <S.RequestCell style={{ flex: 1 }} title={request.url}>
              <S.UrlText>{request.url}</S.UrlText>
            </S.RequestCell>
            <S.RequestCell style={{ width: '80px' }}>
              <S.StatusText color={getStatusColor(request)}>{getStatusText(request)}</S.StatusText>
            </S.RequestCell>
            <S.RequestCell style={{ width: '100px' }}>
              {formatTime(request.timestamp)}
            </S.RequestCell>
          </S.RequestRow>
        ))}
      </S.ListBody>
    </S.List>
  );
};

export default NetworkRequestList;
