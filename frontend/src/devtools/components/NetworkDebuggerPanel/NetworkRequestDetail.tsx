import styled from '@emotion/styled';
import { NetworkRequest } from '../../types/network';

const DetailContainer = styled.div`
  padding: 16px;
  height: 100%;
  overflow-y: auto;
`;

const Section = styled.div`
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
`;

const SectionTitle = styled.h4`
  margin: 0 0 12px 0;
  font-size: 13px;
  font-weight: 600;
  color: #222;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
`;

const DetailGrid = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

const DetailRow = styled.div`
  display: flex;
  gap: 12px;
`;

const DetailLabel = styled.span`
  font-weight: 600;
  color: #666;
  min-width: 100px;
  font-size: 12px;
`;

const DetailValue = styled.span`
  color: #222;
  font-size: 12px;
  flex: 1;
  word-break: break-all;
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
  word-break: break-all;
`;

const CodeBlock = styled.div`
  background: #f5f5f5;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  padding: 12px;
  overflow-x: auto;

  pre {
    margin: 0;
    font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
    font-size: 11px;
    line-height: 1.5;
    color: #222;
    white-space: pre-wrap;
    word-wrap: break-word;
  }
`;

const ErrorBlock = styled.div`
  background: #ffebee;
  border: 1px solid #ffcdd2;
  border-radius: 4px;
  padding: 12px;
  overflow-x: auto;

  pre {
    margin: 0;
    font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
    font-size: 11px;
    line-height: 1.5;
    color: #c62828;
    white-space: pre-wrap;
    word-wrap: break-word;
  }
`;

type Props = {
  request: NetworkRequest;
};

const NetworkRequestDetail = ({ request }: Props) => {
  const formatTimestamp = (timestamp: number): string => {
    const date = new Date(timestamp);
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    const milliseconds = date.getMilliseconds().toString().padStart(3, '0');
    return `${month}/${day}/${year}, ${hours}:${minutes}:${seconds}.${milliseconds}`;
  };

  const formatJSON = (text: string): string => {
    try {
      const parsed = JSON.parse(text);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return text;
    }
  };

  return (
    <DetailContainer>
      <Section>
        <SectionTitle>General</SectionTitle>
        <DetailGrid>
          <DetailRow>
            <DetailLabel>Type:</DetailLabel>
            <DetailValue>
              <TypeBadge type={request.type}>{request.type}</TypeBadge>
            </DetailValue>
          </DetailRow>
          <DetailRow>
            <DetailLabel>Context:</DetailLabel>
            <DetailValue>
              <ContextBadge>{request.context}</ContextBadge>
            </DetailValue>
          </DetailRow>
          <DetailRow>
            <DetailLabel>URL:</DetailLabel>
            <DetailValue>
              <UrlText>{request.url}</UrlText>
            </DetailValue>
          </DetailRow>
          {request.method && (
            <DetailRow>
              <DetailLabel>Method:</DetailLabel>
              <DetailValue>{request.method}</DetailValue>
            </DetailRow>
          )}
          {request.status && (
            <DetailRow>
              <DetailLabel>Status:</DetailLabel>
              <DetailValue>{request.status}</DetailValue>
            </DetailRow>
          )}
          {request.durationMs !== undefined && (
            <DetailRow>
              <DetailLabel>Duration:</DetailLabel>
              <DetailValue>{request.durationMs}ms</DetailValue>
            </DetailRow>
          )}
          <DetailRow>
            <DetailLabel>Timestamp:</DetailLabel>
            <DetailValue>{formatTimestamp(request.timestamp)}</DetailValue>
          </DetailRow>
        </DetailGrid>
      </Section>

      {request.responseBody && (
        <Section>
          <SectionTitle>Response Body</SectionTitle>
          <CodeBlock>
            <pre>{formatJSON(request.responseBody)}</pre>
          </CodeBlock>
        </Section>
      )}

      {request.data && request.type === 'websocket' && (
        <Section>
          <SectionTitle>Message Data</SectionTitle>
          <CodeBlock>
            <pre>{formatJSON(request.data)}</pre>
          </CodeBlock>
        </Section>
      )}

      {request.errorMessage && (
        <Section>
          <SectionTitle>Error</SectionTitle>
          <ErrorBlock>
            <pre>{request.errorMessage}</pre>
          </ErrorBlock>
        </Section>
      )}
    </DetailContainer>
  );
};

export default NetworkRequestDetail;
