import { NetworkRequest } from '../networkCollector';
import { ListContainer, RequestItem } from './DebuggerPanel.styled';

interface RequestListProps {
  requests: NetworkRequest[];
}

export const RequestList = ({ requests }: RequestListProps) => {
  const formatTime = (timestamp: number) => {
    return new Date(timestamp).toLocaleTimeString();
  };

  const getStatusColor = (status?: number) => {
    if (!status) return '#666';
    if (status >= 200 && status < 300) return '#4CAF50';
    if (status >= 300 && status < 400) return '#FF9800';
    return '#F44336';
  };

  return (
    <ListContainer>
      {requests.length === 0 ? (
        <div style={{ padding: '16px', textAlign: 'center', color: '#999' }}>No requests found</div>
      ) : (
        requests.map((request, index) => (
          <RequestItem key={`${request.id}-${index}`}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 600,
                  color: request.type === 'fetch' ? '#1976D2' : '#9C27B0',
                }}
              >
                {request.type.toUpperCase()}
              </span>
              <span style={{ fontSize: '11px', color: '#999' }}>[{request.context}]</span>
              {request.status && (
                <span
                  style={{
                    fontSize: '11px',
                    color: getStatusColor(request.status),
                    fontWeight: 600,
                  }}
                >
                  {request.status}
                </span>
              )}
              <span style={{ fontSize: '10px', color: '#999', marginLeft: 'auto' }}>
                {formatTime(request.timestamp)}
              </span>
            </div>
            <div style={{ fontSize: '12px', color: '#333', wordBreak: 'break-all' }}>
              {request.method && `${request.method} `}
              {request.url}
            </div>
            {request.direction && (
              <div style={{ fontSize: '10px', color: '#999', marginTop: '2px' }}>
                {request.direction === 'sent' ? '→' : '←'}
              </div>
            )}
            {request.data != null && (
              <div
                style={{
                  fontSize: '11px',
                  color: '#666',
                  marginTop: '4px',
                  padding: '4px',
                  background: '#f5f5f5',
                  borderRadius: '2px',
                  maxHeight: '100px',
                  overflow: 'auto',
                }}
              >
                {typeof request.data === 'string'
                  ? request.data
                  : String(JSON.stringify(request.data, null, 2) || '')}
              </div>
            )}
          </RequestItem>
        ))
      )}
    </ListContainer>
  );
};
