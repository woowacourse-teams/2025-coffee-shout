import { ComponentProps } from 'react';
import * as S from './Input.styled';

type Props = ComponentProps<'input'> & {
  height?: string;
  onClear: () => void;
};

const Input = ({ height = '32px', onClear, value, onChange, ref, ...restProps }: Props) => {
  const hasValue = Boolean(value && String(value).length > 0);

  return (
    <S.Container height={height} hasValue={hasValue}>
      <S.Input ref={ref} value={value} onChange={onChange} {...restProps} />
      <S.ClearButton
        type="button"
        onClick={() => onClear()}
        aria-label="입력 내용 지우기"
        hasValue={hasValue}
      >
        <S.CloseIcon src={'/images/close-icon.svg'} />
      </S.ClearButton>
    </S.Container>
  );
};

export default Input;
