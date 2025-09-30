import { ComponentProps } from 'react';
import CloseIcon from '../CloseIcon/CloseIcon';
import * as S from './Input.styled';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';

type Props = {
  height?: string;
  onClear: () => void;
} & ComponentProps<'input'>;

const Input = ({ height = '32px', onClear, value, onChange, ref, ...rest }: Props) => {
  const hasValue = Boolean(value && String(value).length > 0);
  const { touchState, handleTouchStart, handleTouchEnd } = useTouchInteraction({
    onClick: onClear,
  });

  return (
    <S.Container $height={height} $hasValue={hasValue}>
      <S.Input ref={ref} value={value} onChange={onChange} {...rest} />
      <S.ClearButton
        type="button"
        onClick={onClear}
        aria-label="입력 내용 지우기"
        $hasValue={hasValue}
        onTouchStart={handleTouchStart}
        onTouchEnd={handleTouchEnd}
        $touchState={touchState}
      >
        <CloseIcon />
      </S.ClearButton>
    </S.Container>
  );
};

export default Input;
