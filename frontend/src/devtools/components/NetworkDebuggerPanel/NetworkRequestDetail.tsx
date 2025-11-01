import styled from '@emotion/styled';
import { useState } from 'react';
import { NetworkRequest, WebSocketMessage } from '../../types/network';

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

const MessageRow = styled.div<{ isExpanded: boolean }>`
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  cursor: pointer;
  background: ${({ isExpanded }) => (isExpanded ? '#e8f0fe' : '#ffffff')};
  transition: background 0.1s ease;

  &:hover {
    background: ${({ isExpanded }) => (isExpanded ? '#e8f0fe' : '#f8f9fa')};
  }
`;

const MessageArrow = styled.span<{ type: 'sent' | 'received' }>`
  display: inline-block;
  margin-right: 8px;
  font-size: 12px;
  color: ${({ type }) => (type === 'sent' ? '#0f9d58' : '#d93025')};
  font-weight: 600;
  user-select: none;
`;

const MessageSummary = styled.span`
  flex: 1;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 11px;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
`;

const MessageTime = styled.span`
  font-size: 11px;
  color: #666;
  margin-left: 8px;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
`;

const ExpandedMessageDetail = styled.div`
  padding: 16px;
  background: #f8f9fa;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
`;

type Props = {
  request: NetworkRequest;
};

const NetworkRequestDetail = ({ request }: Props) => {
  const [expandedMessageIndex, setExpandedMessageIndex] = useState<number | null>(null);

  const formatJSON = (text: string): string => {
    try {
      const parsed = JSON.parse(text);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return text;
    }
  };

  // STOMP payload 파싱 함수 ({success, data, errorMessage})
  const parseStompPayload = (bodyText: string) => {
    if (!bodyText || typeof bodyText !== 'string') return null;

    // JSON이 끝나는 정확한 위치 찾기 (중괄호 카운팅)
    const findJsonEnd = (text: string, startIndex: number): number => {
      let braceCount = 0;
      let bracketCount = 0;
      let inString = false;
      let escapeNext = false;

      for (let i = startIndex; i < text.length; i++) {
        const char = text[i];
        const charCode = text.charCodeAt(i);

        // null 문자를 만나면 JSON이 끝난 것으로 간주
        if (charCode === 0) {
          if (braceCount === 0 && bracketCount === 0) {
            return i;
          }
        }

        if (escapeNext) {
          escapeNext = false;
          continue;
        }

        if (char === '\\') {
          escapeNext = true;
          continue;
        }

        if (char === '"') {
          inString = !inString;
          continue;
        }

        if (inString) continue;

        if (char === '{') {
          braceCount++;
        } else if (char === '}') {
          braceCount--;
          if (braceCount === 0 && bracketCount === 0) {
            return i + 1;
          }
        } else if (char === '[') {
          bracketCount++;
        } else if (char === ']') {
          bracketCount--;
          if (braceCount === 0 && bracketCount === 0) {
            return i + 1;
          }
        }
      }

      return -1;
    };

    // JSON 시작 위치 찾기
    const firstBrace = bodyText.indexOf('{');
    const firstBracket = bodyText.indexOf('[');
    let jsonStart = -1;

    if (firstBrace !== -1 && (firstBracket === -1 || firstBrace < firstBracket)) {
      jsonStart = firstBrace;
    } else if (firstBracket !== -1) {
      jsonStart = firstBracket;
    }

    if (jsonStart === -1) return null;

    // JSON 끝 위치 찾기
    const jsonEnd = findJsonEnd(bodyText, jsonStart);
    if (jsonEnd === -1) return null;

    // 유효한 JSON 부분만 추출
    const extractedJson = bodyText.substring(jsonStart, jsonEnd).trimEnd();
    // 끝의 null 문자 제거
    let cleanedJson = extractedJson;
    while (cleanedJson.length > 0 && cleanedJson.charCodeAt(cleanedJson.length - 1) === 0) {
      cleanedJson = cleanedJson.substring(0, cleanedJson.length - 1);
    }

    try {
      // 추출한 JSON 파싱
      const parsed = JSON.parse(cleanedJson);
      if (
        parsed &&
        typeof parsed === 'object' &&
        ('success' in parsed || 'data' in parsed || 'errorMessage' in parsed)
      ) {
        return parsed;
      }
    } catch (error) {
      // 디버깅: 파싱 실패 원인 분석
      console.warn('[STOMP Payload Parse] JSON 파싱 실패:', {
        error: error instanceof Error ? error.message : String(error),
        bodyLength: bodyText.length,
        jsonStart,
        jsonEnd,
        extractedJsonLength: cleanedJson.length,
        extractedJsonEnd: cleanedJson.substring(Math.max(0, cleanedJson.length - 50)),
        bodyEnd: bodyText.substring(Math.max(0, bodyText.length - 100)),
        hasDevtools: bodyText.includes('@devtools'),
      });
      return null;
    }

    return null;
  };

  // 메시지 요약 생성 함수
  const getMessageSummary = (message: WebSocketMessage): string => {
    console.log('[NetworkRequestDetail] getMessageSummary 호출:', {
      isStompMessage: message.isStompMessage,
      hasStompHeaders: !!message.stompHeaders,
      stompHeaders: message.stompHeaders,
    });

    if (message.isStompMessage && message.stompHeaders) {
      const command = message.stompHeaders['command'] || 'STOMP';
      const destination = message.stompHeaders['destination'] || '';
      const nid = message.stompHeaders['nid'] || message.stompHeaders['id'] || '';
      const receipt = message.stompHeaders['receipt'] || '';
      const receiptId = message.stompHeaders['receipt-id'] || '';

      let summary = command;

      // SUBSCRIBE/UNSUBSCRIBE의 경우 nid 추가
      if ((command === 'SUBSCRIBE' || command === 'UNSUBSCRIBE') && nid) {
        summary = `${command} ${nid}`;
        if (destination) {
          summary = `${summary} ${destination}`;
        }
      } else if (command === 'DISCONNECT' && receipt) {
        // DISCONNECT의 경우 receipt 추가
        summary = `${command} ${receipt}`;
      } else if (command === 'RECEIPT' && receiptId) {
        // RECEIPT의 경우 receipt-id 추가
        summary = `${command} ${receiptId}`;
      } else if (destination) {
        summary = `${command} ${destination}`;
      }

      console.log('[NetworkRequestDetail] STOMP 메시지 요약:', summary);
      return summary;
    }
    // 일반 메시지인 경우 데이터 일부 표시
    const maxLength = 100;
    const data = message.data || '';
    const summary = data.length > maxLength ? data.substring(0, maxLength) + '...' : data;
    console.log('[NetworkRequestDetail] 일반 메시지 요약:', summary);
    return summary;
  };

  // 메시지 상세 정보 렌더링
  const renderMessageDetail = (message: WebSocketMessage) => {
    console.log('[NetworkRequestDetail] renderMessageDetail 호출:', {
      type: message.type,
      isStompMessage: message.isStompMessage,
      stompHeaders: message.stompHeaders,
      stompBody: message.stompBody ? message.stompBody.substring(0, 200) : message.stompBody,
      rawData: message.data ? message.data.substring(0, 200) : message.data,
    });

    // STOMP payload 파싱
    const stompPayload =
      message.isStompMessage && message.stompBody ? parseStompPayload(message.stompBody) : null;

    console.log('[NetworkRequestDetail] Payload 파싱 결과:', {
      hasStompPayload: !!stompPayload,
      stompPayload,
    });

    // MESSAGE가 아닌 경우 원본 텍스트만 표시
    const isMessageCommand =
      message.isStompMessage &&
      message.stompHeaders &&
      message.stompHeaders['command'] === 'MESSAGE';

    if (!isMessageCommand) {
      return (
        <ExpandedMessageDetail>
          <CodeBlock>
            <pre>{formatJSON(message.data)}</pre>
          </CodeBlock>
        </ExpandedMessageDetail>
      );
    }

    return (
      <ExpandedMessageDetail>
        {/* MESSAGE인 경우 구조화된 형태로 표시 */}
        {message.isStompMessage && message.stompHeaders && (
          <>
            {/* Payload 섹션 */}
            {(message.stompBody || message.stompBody === '') && (
              <>
                <SectionTitle
                  style={{
                    margin: '16px 0 12px 0',
                    fontSize: '13px',
                    fontWeight: '600',
                    color: '#222',
                    border: 'none',
                    padding: 0,
                  }}
                >
                  Payload{' '}
                  {message.stompHeaders['content-type'] === 'application/json' ? '(JSON)' : ''}
                  {stompPayload ? ' (Structured: {success, data, errorMessage})' : ''}
                </SectionTitle>

                {/* 구조화된 Payload 표시 */}
                {stompPayload ? (
                  <>
                    {/* Success 상태 */}
                    {typeof stompPayload.success !== 'undefined' && (
                      <DetailRow style={{ marginBottom: '12px' }}>
                        <DetailLabel
                          style={{
                            fontSize: '12px',
                            color: '#666',
                            minWidth: '140px',
                            fontWeight: '600',
                          }}
                        >
                          Success:
                        </DetailLabel>
                        <DetailValue
                          style={{
                            fontSize: '12px',
                            color: stompPayload.success ? '#0f9d58' : '#d93025',
                            fontWeight: '600',
                          }}
                        >
                          {String(stompPayload.success)}
                        </DetailValue>
                      </DetailRow>
                    )}

                    {/* Data */}
                    {stompPayload.data !== undefined && (
                      <>
                        <DetailRow style={{ marginBottom: '8px' }}>
                          <DetailLabel
                            style={{
                              fontSize: '12px',
                              color: '#666',
                              minWidth: '140px',
                              fontWeight: '600',
                            }}
                          >
                            Data:
                          </DetailLabel>
                        </DetailRow>
                        <CodeBlock style={{ marginTop: '4px' }}>
                          <pre>{formatJSON(JSON.stringify(stompPayload.data))}</pre>
                        </CodeBlock>
                      </>
                    )}

                    {/* Error Message */}
                    {stompPayload.errorMessage && (
                      <div style={{ marginTop: '12px' }}>
                        <DetailRow style={{ marginBottom: '8px' }}>
                          <DetailLabel
                            style={{
                              fontSize: '12px',
                              color: '#666',
                              minWidth: '140px',
                              fontWeight: '600',
                            }}
                          >
                            Error Message:
                          </DetailLabel>
                        </DetailRow>
                        <ErrorBlock>
                          <pre>{stompPayload.errorMessage}</pre>
                        </ErrorBlock>
                      </div>
                    )}
                  </>
                ) : (
                  /* 구조화된 payload가 아니면 일반 JSON으로 표시 */
                  <CodeBlock>
                    <pre>
                      {message.isStompMessage && message.stompBody !== undefined
                        ? message.stompBody
                          ? formatJSON(message.stompBody)
                          : '(empty)'
                        : formatJSON(message.data)}
                    </pre>
                  </CodeBlock>
                )}
              </>
            )}
          </>
        )}

        {/* STOMP MESSAGE가 아닌 경우 일반 표시 */}
        {!message.isStompMessage && (
          <CodeBlock>
            <pre>{formatJSON(message.data)}</pre>
          </CodeBlock>
        )}
      </ExpandedMessageDetail>
    );
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
          {request.url && (
            <DetailRow>
              <DetailLabel>Request URL:</DetailLabel>
              <DetailValue>
                <UrlText>{request.url}</UrlText>
              </DetailValue>
            </DetailRow>
          )}
          {request.method && (
            <DetailRow>
              <DetailLabel>Method:</DetailLabel>
              <DetailValue>{request.method}</DetailValue>
            </DetailRow>
          )}
          {request.status && (
            <DetailRow>
              <DetailLabel>Status:</DetailLabel>
              <DetailValue>
                {request.type === 'websocket' && request.status === 101
                  ? '101 (Switching Protocols)'
                  : request.status}
              </DetailValue>
            </DetailRow>
          )}
          {request.type === 'websocket' && request.connectionStatus && (
            <DetailRow>
              <DetailLabel>Connection Status:</DetailLabel>
              <DetailValue>{request.connectionStatus}</DetailValue>
            </DetailRow>
          )}
          {request.durationMs !== undefined && (
            <DetailRow>
              <DetailLabel>Duration:</DetailLabel>
              <DetailValue>{request.durationMs}ms</DetailValue>
            </DetailRow>
          )}
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

      {request.type === 'websocket' && request.messages && request.messages.length > 0 && (
        <Section>
          <SectionTitle>Message Data</SectionTitle>
          {request.messages.map((message, index) => {
            const date = new Date(message.timestamp);
            const hours = date.getHours().toString().padStart(2, '0');
            const minutes = date.getMinutes().toString().padStart(2, '0');
            const seconds = date.getSeconds().toString().padStart(2, '0');
            const milliseconds = date.getMilliseconds().toString().padStart(3, '0');
            const timeStr = `${hours}:${minutes}:${seconds}.${milliseconds}`;

            const isExpanded = expandedMessageIndex === index;
            const summary = getMessageSummary(message);

            return (
              <div key={index}>
                <MessageRow
                  isExpanded={isExpanded}
                  onClick={() => setExpandedMessageIndex(isExpanded ? null : index)}
                >
                  <MessageArrow type={message.type}>
                    {message.type === 'sent' ? '▲' : '▼'}
                  </MessageArrow>
                  <MessageSummary title={message.data}>{summary}</MessageSummary>
                  <MessageTime>{timeStr}</MessageTime>
                </MessageRow>
                {isExpanded && renderMessageDetail(message)}
              </div>
            );
          })}
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
