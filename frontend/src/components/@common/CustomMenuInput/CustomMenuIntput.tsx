import { ComponentProps, ChangeEvent } from 'react';
import * as S from './CustomMenuInput.styled';

type Props = {
  placeholder: string;
  value?: string;
  onChange?: (e: ChangeEvent<HTMLInputElement>) => void;
} & ComponentProps<'input'>;

const CustomMenuInput = ({ placeholder, value, onChange, ref, ...rest }: Props) => {
  const hasValue = Boolean(value && String(value).length > 0);

  return (
    <S.Container>
      <S.Input ref={ref} placeholder={placeholder} value={value} onChange={onChange} {...rest} />
      <S.DoneButton $hasValue={hasValue}>→</S.DoneButton>
    </S.Container>
  );
};

export default CustomMenuInput;
