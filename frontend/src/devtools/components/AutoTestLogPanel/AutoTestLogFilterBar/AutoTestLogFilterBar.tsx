import { useMemo } from 'react';
import * as S from './AutoTestLogFilterBar.styled';

type Props = {
  contexts: string[];
  selectedContext: string | null;
  onContextChange: (context: string | null) => void;
};

/**
 * AutoTest 로그 필터 바 컴포넌트입니다.
 */
const AutoTestLogFilterBar = ({ contexts, selectedContext, onContextChange }: Props) => {
  /**
   * 고유한 context 목록을 추출합니다.
   * 순서: MAIN → HOST → GUEST1, GUEST2... (숫자 순서로 정렬)
   */
  const availableContexts = useMemo(() => {
    const contextArray = [...contexts];
    const contextUpper = contextArray.map((ctx) => ctx.toUpperCase());

    // MAIN, HOST, GUEST 계열로 분류
    const mainContexts: string[] = [];
    const hostContexts: string[] = [];
    const guestContexts: { original: string; number: number }[] = [];
    const otherContexts: string[] = [];

    contextArray.forEach((ctx, index) => {
      const upper = contextUpper[index];
      if (upper === 'MAIN') {
        mainContexts.push(ctx);
      } else if (upper === 'HOST') {
        hostContexts.push(ctx);
      } else if (upper.startsWith('GUEST')) {
        const match = upper.match(/^GUEST(\d+)$/);
        if (match) {
          const num = parseInt(match[1], 10);
          guestContexts.push({ original: ctx, number: num });
        } else {
          guestContexts.push({ original: ctx, number: Infinity });
        }
      } else {
        otherContexts.push(ctx);
      }
    });

    // GUEST 계열을 숫자 순서로 정렬
    guestContexts.sort((a, b) => a.number - b.number);

    // 순서: MAIN → HOST → GUEST1, GUEST2... → 기타
    return [
      ...mainContexts,
      ...hostContexts,
      ...guestContexts.map((g) => g.original),
      ...otherContexts.sort(),
    ];
  }, [contexts]);

  return (
    <S.FilterBar>
      <S.FilterGroup>
        <S.FilterLabel>Context:</S.FilterLabel>
        <S.FilterButton
          type="button"
          $active={selectedContext === null}
          onClick={() => onContextChange(null)}
        >
          All
        </S.FilterButton>
        {availableContexts.map((context) => (
          <S.FilterButton
            key={context}
            type="button"
            $active={selectedContext === context}
            onClick={() => onContextChange(context)}
          >
            {context}
          </S.FilterButton>
        ))}
      </S.FilterGroup>
    </S.FilterBar>
  );
};

export default AutoTestLogFilterBar;

