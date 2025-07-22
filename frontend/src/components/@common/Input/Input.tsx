import CloseIcon from '@/assets/close-icon.svg';
import { ComponentProps } from 'react';
import * as S from './Input.styled';

type Props = {
  height?: string;
  onClear: () => void;
} & ComponentProps<'input'>;

const Input = ({ height = '32px', onClear, value, onChange, ref, ...rest }: Props) => {
  const hasValue = Boolean(value && String(value).length > 0);

  return (
    <S.Container $height={height} $hasValue={hasValue}>
      <S.Input ref={ref} value={value} onChange={onChange} {...rest} />
      <S.ClearButton
        type="button"
        onClick={onClear}
        aria-label="입력 내용 지우기"
        $hasValue={hasValue}
      >
        <S.CloseIcon src={CloseIcon} />
      </S.ClearButton>
    </S.Container>
  );
};

export default Input;
