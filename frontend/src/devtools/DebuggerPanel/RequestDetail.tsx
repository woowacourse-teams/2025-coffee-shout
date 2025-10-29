import { useState } from 'react';
import { NetworkRequest } from '../networkCollector';
import {
  DetailContainer,
  TabBar,
  Tab,
  TabContent,
  HeaderRow,
  HeaderKey,
  HeaderValue,
  CodeBlock,
} from './DebuggerPanel.styled';

interface RequestDetailProps {
  request: NetworkRequest;
  onClose: () => void;
}

type TabType = 'headers' | 'payload' | 'response' | 'preview';

export const RequestDetail = ({ request, onClose }: RequestDetailProps) => {
  const [activeTab, setActiveTab] = useState<TabType>('headers');

  const formatHeaders = (headers?: Record<string, string>) => {
    if (!headers || Object.keys(headers).length === 0) return null;
    return Object.entries(headers).map(([key, value]) => ({ key, value }));
  };

  const formatQueryParams = (queryString?: string) => {
    if (!queryString || queryString === '') return null;
    const params = new URLSearchParams(queryString);
    return Array.from(params.entries()).map(([key, value]) => ({ key, value }));
  };

  const parsePreview = () => {
    if (request.type === 'websocket') {
      return request.data;
    }
    if (request.responseBody) {
      try {
        const parsed = JSON.parse(request.responseBody);
        return JSON.stringify(parsed, null, 2);
      } catch {
        return request.responseBody;
      }
    }
    return null;
  };

  const requestHeaders = formatHeaders(request.requestHeaders);
  const responseHeaders = formatHeaders(request.responseHeaders);
  const queryParams = formatQueryParams(request.queryParams);

  return (
    <DetailContainer>
      <div
        style={{
          padding: '12px 16px',
          borderBottom: '1px solid #ddd',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <div style={{ fontSize: '14px', fontWeight: 600 }}>
          {request.method} {request.url}
        </div>
        <button
          onClick={onClose}
          style={{
            background: 'none',
            border: 'none',
            fontSize: '18px',
            cursor: 'pointer',
            padding: '0 8px',
            color: '#666',
          }}
        >
          ×
        </button>
      </div>

      <TabBar>
        <Tab $active={activeTab === 'headers'} onClick={() => setActiveTab('headers')}>
          Headers
        </Tab>
        <Tab $active={activeTab === 'payload'} onClick={() => setActiveTab('payload')}>
          Payload
        </Tab>
        <Tab $active={activeTab === 'response'} onClick={() => setActiveTab('response')}>
          Response
        </Tab>
        <Tab $active={activeTab === 'preview'} onClick={() => setActiveTab('preview')}>
          Preview
        </Tab>
      </TabBar>

      <TabContent>
        {activeTab === 'headers' && (
          <div>
            {requestHeaders && requestHeaders.length > 0 && (
              <div style={{ marginBottom: '24px' }}>
                <div
                  style={{ fontSize: '12px', fontWeight: 600, marginBottom: '8px', color: '#666' }}
                >
                  Request Headers
                </div>
                {requestHeaders.map(({ key, value }) => (
                  <HeaderRow key={key}>
                    <HeaderKey>{key}:</HeaderKey>
                    <HeaderValue>{value}</HeaderValue>
                  </HeaderRow>
                ))}
              </div>
            )}
            {responseHeaders && responseHeaders.length > 0 && (
              <div>
                <div
                  style={{ fontSize: '12px', fontWeight: 600, marginBottom: '8px', color: '#666' }}
                >
                  Response Headers
                </div>
                {responseHeaders.map(({ key, value }) => (
                  <HeaderRow key={key}>
                    <HeaderKey>{key}:</HeaderKey>
                    <HeaderValue>{value}</HeaderValue>
                  </HeaderRow>
                ))}
              </div>
            )}
            {(!requestHeaders || requestHeaders.length === 0) &&
              (!responseHeaders || responseHeaders.length === 0) && (
                <div style={{ padding: '16px', textAlign: 'center', color: '#999' }}>
                  No headers
                </div>
              )}
          </div>
        )}

        {activeTab === 'payload' && (
          <div>
            {queryParams && queryParams.length > 0 && (
              <div style={{ marginBottom: '24px' }}>
                <div
                  style={{ fontSize: '12px', fontWeight: 600, marginBottom: '8px', color: '#666' }}
                >
                  Query String Parameters
                </div>
                {queryParams.map(({ key, value }) => (
                  <HeaderRow key={key}>
                    <HeaderKey>{key}:</HeaderKey>
                    <HeaderValue>{value}</HeaderValue>
                  </HeaderRow>
                ))}
              </div>
            )}
            {request.requestBody ? (
              <div>
                <div
                  style={{ fontSize: '12px', fontWeight: 600, marginBottom: '8px', color: '#666' }}
                >
                  Request Payload
                </div>
                <CodeBlock>{request.requestBody}</CodeBlock>
              </div>
            ) : (
              <div style={{ padding: '16px', textAlign: 'center', color: '#999' }}>No payload</div>
            )}
          </div>
        )}

        {activeTab === 'response' && (
          <div>
            {request.responseBody ? (
              <CodeBlock>{request.responseBody}</CodeBlock>
            ) : request.type === 'websocket' && request.data ? (
              <CodeBlock>
                {typeof request.data === 'string'
                  ? request.data
                  : JSON.stringify(request.data, null, 2)}
              </CodeBlock>
            ) : (
              <div style={{ padding: '16px', textAlign: 'center', color: '#999' }}>No response</div>
            )}
          </div>
        )}

        {activeTab === 'preview' && (
          <div>
            {parsePreview() ? (
              <CodeBlock>{String(parsePreview())}</CodeBlock>
            ) : (
              <div style={{ padding: '16px', textAlign: 'center', color: '#999' }}>
                No preview available
              </div>
            )}
          </div>
        )}
      </TabContent>
    </DetailContainer>
  );
};
