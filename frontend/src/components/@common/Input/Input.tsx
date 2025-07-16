import { ComponentProps } from 'react';
import * as S from './Input.styled';

type Props = ComponentProps<'input'> & {
  height?: string;
  onClear: () => void;
};

const Input = ({ height = '32px', onClear, value, onChange, ref, ...restProps }: Props) => {
  const hasContent = Boolean(value && String(value).length > 0);

  return (
    <S.Container height={height} hasContent={hasContent}>
      <S.Input ref={ref} value={value} onChange={onChange} {...restProps} />
      <S.ClearButton
        type="button"
        onClick={() => onClear()}
        aria-label="입력 내용 지우기"
        hasContent={hasContent}
      >
        <S.CloseIcon src={'/images/close-icon.svg'} />
      </S.ClearButton>
    </S.Container>
  );
};

export default Input;
