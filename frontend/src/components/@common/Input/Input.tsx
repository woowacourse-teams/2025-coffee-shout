import { ComponentProps } from 'react';

import { usePressAnimation } from '@/hooks/usePressAnimation';

import CloseIcon from '../CloseIcon/CloseIcon';

import * as S from './Input.styled';

type Props = {
  height?: string;
  onClear: () => void;
} & ComponentProps<'input'>;

const Input = ({ height = '32px', onClear, value, onChange, ref, ...rest }: Props) => {
  const hasValue = Boolean(value && String(value).length > 0);
  const { touchState, onPointerDown, onPointerUp } = usePressAnimation();

  return (
    <S.Container $height={height} $hasValue={hasValue}>
      <S.Input ref={ref} value={value} onChange={onChange} {...rest} />
      <S.ClearButton
        type="button"
        onPointerDown={onPointerDown}
        onPointerUp={(e) => {
          onPointerUp(e);
          onClear();
        }}
        aria-label="입력 내용 지우기"
        $hasValue={hasValue}
        $touchState={touchState}
      >
        <CloseIcon />
      </S.ClearButton>
    </S.Container>
  );
};

export default Input;
