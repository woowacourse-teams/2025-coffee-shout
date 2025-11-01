import { useState } from 'react';
import type { NetworkRequest, WebSocketMessage } from '../../../types/network';
import { formatJSON } from '../../../utils/formatJSON';
import { parseStompPayload } from '../../../utils/parseStompPayload';
import { getMessageSummary } from '../../../utils/getMessageSummary';
import { STOMP_COMMAND } from '../../../utils/stompMessageConstants';
import * as S from './NetworkRequestDetail.styled';

type Props = {
  request: NetworkRequest;
};

/**
 * 네트워크 요청의 상세 정보를 표시하는 컴포넌트입니다.
 */
const NetworkRequestDetail = ({ request }: Props) => {
  const [expandedMessageIndex, setExpandedMessageIndex] = useState<number | null>(null);

  /**
   * 타임스탬프를 포맷팅합니다.
   */
  const formatTimestamp = (timestamp: number): string => {
    const date = new Date(timestamp);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    const milliseconds = date.getMilliseconds().toString().padStart(3, '0');
    return `${hours}:${minutes}:${seconds}.${milliseconds}`;
  };

  /**
   * WebSocket 메시지의 상세 정보를 렌더링합니다.
   */
  const renderMessageDetail = (message: WebSocketMessage) => {
    const isMessageCommand =
      message.isStompMessage &&
      message.stompHeaders &&
      message.stompHeaders['command'] === STOMP_COMMAND.MESSAGE;

    // MESSAGE가 아닌 경우 원본 텍스트만 표시
    if (!isMessageCommand) {
      return (
        <S.ExpandedMessageDetail>
          <S.CodeBlock>
            <pre>{formatJSON(message.data)}</pre>
          </S.CodeBlock>
        </S.ExpandedMessageDetail>
      );
    }

    // MESSAGE인 경우 구조화된 형태로 표시
    const stompPayload =
      message.isStompMessage && message.stompBody ? parseStompPayload(message.stompBody) : null;

    return (
      <S.ExpandedMessageDetail>
        {message.isStompMessage && message.stompHeaders && (
          <>
            {(message.stompBody || message.stompBody === '') && (
              <>
                <S.SectionTitle
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
                </S.SectionTitle>

                {stompPayload ? (
                  <>
                    {typeof stompPayload.success !== 'undefined' && (
                      <S.DetailRow style={{ marginBottom: '12px' }}>
                        <S.DetailLabel
                          style={{
                            fontSize: '12px',
                            color: '#666',
                            minWidth: '140px',
                            fontWeight: '600',
                          }}
                        >
                          Success:
                        </S.DetailLabel>
                        <S.DetailValue
                          style={{
                            fontSize: '12px',
                            color: stompPayload.success ? '#0f9d58' : '#d93025',
                            fontWeight: '600',
                          }}
                        >
                          {String(stompPayload.success)}
                        </S.DetailValue>
                      </S.DetailRow>
                    )}

                    {stompPayload.data !== undefined && (
                      <>
                        <S.DetailRow style={{ marginBottom: '8px' }}>
                          <S.DetailLabel
                            style={{
                              fontSize: '12px',
                              color: '#666',
                              minWidth: '140px',
                              fontWeight: '600',
                            }}
                          >
                            Data:
                          </S.DetailLabel>
                        </S.DetailRow>
                        <S.CodeBlock style={{ marginTop: '4px' }}>
                          <pre>{formatJSON(JSON.stringify(stompPayload.data))}</pre>
                        </S.CodeBlock>
                      </>
                    )}

                    {stompPayload.errorMessage && (
                      <div style={{ marginTop: '12px' }}>
                        <S.DetailRow style={{ marginBottom: '8px' }}>
                          <S.DetailLabel
                            style={{
                              fontSize: '12px',
                              color: '#666',
                              minWidth: '140px',
                              fontWeight: '600',
                            }}
                          >
                            Error Message:
                          </S.DetailLabel>
                        </S.DetailRow>
                        <S.ErrorBlock>
                          <pre>{stompPayload.errorMessage}</pre>
                        </S.ErrorBlock>
                      </div>
                    )}
                  </>
                ) : (
                  <S.CodeBlock>
                    <pre>
                      {message.isStompMessage && message.stompBody !== undefined
                        ? message.stompBody
                          ? formatJSON(message.stompBody)
                          : '(empty)'
                        : formatJSON(message.data)}
                    </pre>
                  </S.CodeBlock>
                )}
              </>
            )}
          </>
        )}

        {!message.isStompMessage && (
          <S.CodeBlock>
            <pre>{formatJSON(message.data)}</pre>
          </S.CodeBlock>
        )}
      </S.ExpandedMessageDetail>
    );
  };

  return (
    <S.DetailContainer>
      <S.Section>
        <S.SectionTitle>General</S.SectionTitle>
        <S.DetailGrid>
          <S.DetailRow>
            <S.DetailLabel>Type:</S.DetailLabel>
            <S.DetailValue>
              <S.TypeBadge type={request.type}>{request.type}</S.TypeBadge>
            </S.DetailValue>
          </S.DetailRow>
          <S.DetailRow>
            <S.DetailLabel>Context:</S.DetailLabel>
            <S.DetailValue>
              <S.ContextBadge>{request.context}</S.ContextBadge>
            </S.DetailValue>
          </S.DetailRow>
          {request.url && (
            <S.DetailRow>
              <S.DetailLabel>Request URL:</S.DetailLabel>
              <S.DetailValue>
                <S.UrlText>{request.url}</S.UrlText>
              </S.DetailValue>
            </S.DetailRow>
          )}
          {request.method && (
            <S.DetailRow>
              <S.DetailLabel>Method:</S.DetailLabel>
              <S.DetailValue>{request.method}</S.DetailValue>
            </S.DetailRow>
          )}
          {request.status && (
            <S.DetailRow>
              <S.DetailLabel>Status:</S.DetailLabel>
              <S.DetailValue>
                {request.type === 'websocket' && request.status === 101
                  ? '101 (Switching Protocols)'
                  : request.status}
              </S.DetailValue>
            </S.DetailRow>
          )}
          {request.type === 'websocket' && request.connectionStatus && (
            <S.DetailRow>
              <S.DetailLabel>Connection Status:</S.DetailLabel>
              <S.DetailValue>{request.connectionStatus}</S.DetailValue>
            </S.DetailRow>
          )}
          {request.durationMs !== undefined && (
            <S.DetailRow>
              <S.DetailLabel>Duration:</S.DetailLabel>
              <S.DetailValue>{request.durationMs}ms</S.DetailValue>
            </S.DetailRow>
          )}
        </S.DetailGrid>
      </S.Section>

      {request.responseBody && (
        <S.Section>
          <S.SectionTitle>Response Body</S.SectionTitle>
          <S.CodeBlock>
            <pre>{formatJSON(request.responseBody)}</pre>
          </S.CodeBlock>
        </S.Section>
      )}

      {request.type === 'websocket' && request.messages && request.messages.length > 0 && (
        <S.Section>
          <S.SectionTitle>Message Data</S.SectionTitle>
          {request.messages.map((message, index) => {
            const isExpanded = expandedMessageIndex === index;
            const summary = getMessageSummary(message);
            const timeStr = formatTimestamp(message.timestamp);

            return (
              <div key={index}>
                <S.MessageRow
                  isExpanded={isExpanded}
                  onClick={() => setExpandedMessageIndex(isExpanded ? null : index)}
                >
                  <S.MessageArrow type={message.type}>
                    {message.type === 'sent' ? '▲' : '▼'}
                  </S.MessageArrow>
                  <S.MessageSummary title={message.data}>{summary}</S.MessageSummary>
                  <S.MessageTime>{timeStr}</S.MessageTime>
                </S.MessageRow>
                {isExpanded && renderMessageDetail(message)}
              </div>
            );
          })}
        </S.Section>
      )}

      {request.errorMessage && (
        <S.Section>
          <S.SectionTitle>Error</S.SectionTitle>
          <S.ErrorBlock>
            <pre>{request.errorMessage}</pre>
          </S.ErrorBlock>
        </S.Section>
      )}
    </S.DetailContainer>
  );
};

export default NetworkRequestDetail;
