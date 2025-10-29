import { useState } from 'react';
import { NetworkRequest } from '../networkCollector';
import { ListContainer, RequestItem } from './DebuggerPanel.styled';
import { RequestDetail } from './RequestDetail';

interface RequestListProps {
  requests: NetworkRequest[];
}

export const RequestList = ({ requests }: RequestListProps) => {
  const [selectedRequest, setSelectedRequest] = useState<NetworkRequest | null>(null);

  const formatTime = (timestamp: number) => {
    return new Date(timestamp).toLocaleTimeString();
  };

  const getStatusColor = (status?: number) => {
    if (!status) return '#666';
    if (status >= 200 && status < 300) return '#4CAF50';
    if (status >= 300 && status < 400) return '#FF9800';
    return '#F44336';
  };

  const displayUrl = (request: NetworkRequest) => {
    if (request.type === 'fetch' && request.queryParams) {
      return request.url + request.queryParams;
    }
    return request.url;
  };

  return (
    <ListContainer style={{ position: 'relative' }}>
      {selectedRequest && (
        <RequestDetail request={selectedRequest} onClose={() => setSelectedRequest(null)} />
      )}
      {requests.length === 0 ? (
        <div style={{ padding: '16px', textAlign: 'center', color: '#999' }}>No requests found</div>
      ) : (
        requests.map((request, index) => (
          <RequestItem
            key={`${request.id}-${index}`}
            onClick={() => setSelectedRequest(request)}
            style={{ display: selectedRequest ? 'none' : 'block' }}
          >
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
              {displayUrl(request)}
            </div>
          </RequestItem>
        ))
      )}
    </ListContainer>
  );
};
