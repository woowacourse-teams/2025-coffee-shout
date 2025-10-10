import { ComponentProps } from 'react';

import { useButtonInteraction } from '@/hooks/useButtonInteraction';

import CloseIcon from '../CloseIcon/CloseIcon';

import * as S from './Input.styled';

type Props = {
  height?: string;
  onClear: () => void;
} & ComponentProps<'input'>;

const Input = ({ height = '32px', onClear, value, onChange, ref, ...rest }: Props) => {
  const hasValue = Boolean(value && String(value).length > 0);
  const { touchState, pointerHandlers } = useButtonInteraction({ onClick: onClear });

  return (
    <S.Container $height={height} $hasValue={hasValue}>
      <S.Input ref={ref} value={value} onChange={onChange} {...rest} />
      <S.ClearButton
        type="button"
        {...pointerHandlers}
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
