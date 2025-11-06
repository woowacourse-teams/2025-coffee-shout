import { AutoTestLog } from '../../../types/autoTest';
import * as S from './AutoTestLogList.styled';

type Props = {
  logs: AutoTestLog[];
};

/**
 * AutoTest 로그 목록을 표시하는 컴포넌트입니다.
 */
const AutoTestLogList = ({ logs }: Props) => {
  /**
   * 타임스탬프를 포맷팅합니다.
   */
  const formatTimestamp = (timestamp: number): string => {
    const date = new Date(timestamp);
    const timeString = date.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
    const milliseconds = date.getMilliseconds().toString().padStart(3, '0');
    return `${timeString}.${milliseconds}`;
  };

  if (logs.length === 0) {
    return (
      <S.EmptyState>
        <S.EmptyText>No logs</S.EmptyText>
      </S.EmptyState>
    );
  }

  return (
    <S.List>
      <S.ListHeader>
        <S.HeaderCell $width="140px">Time</S.HeaderCell>
        <S.HeaderCell $width="100px">Context</S.HeaderCell>
        <S.HeaderCell $flex={1}>Message</S.HeaderCell>
      </S.ListHeader>
      <S.ListBody>
        {logs.map((log) => (
          <S.LogRow key={log.id}>
            <S.LogCell $width="140px">
              <S.TimeText>{formatTimestamp(log.timestamp)}</S.TimeText>
            </S.LogCell>
            <S.LogCell $width="100px">
              <S.ContextBadge>{log.context}</S.ContextBadge>
            </S.LogCell>
            <S.LogCell $flex={1}>
              <S.MessageText>{log.message}</S.MessageText>
            </S.LogCell>
          </S.LogRow>
        ))}
      </S.ListBody>
    </S.List>
  );
};

export default AutoTestLogList;

