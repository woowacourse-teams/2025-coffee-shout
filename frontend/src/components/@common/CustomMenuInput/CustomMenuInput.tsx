import { ComponentProps, ChangeEvent, useRef, KeyboardEvent } from 'react';
import * as S from './CustomMenuInput.styled';

type Props = {
  placeholder: string;
  value?: string;
  onChange?: (e: ChangeEvent<HTMLInputElement>) => void;
  onClickDoneButton?: () => void;
} & ComponentProps<'input'>;

const CustomMenuInput = ({
  placeholder,
  value,
  onChange,
  ref,
  onClickDoneButton,
  ...rest
}: Props) => {
  const hasValue = Boolean(value && String(value).length > 0);
  const doneButtonRef = useRef<HTMLButtonElement>(null);

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      onClickDoneButton?.();
    }
  };

  return (
    <S.Container>
      <S.Input
        ref={ref}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        {...rest}
        onKeyDown={handleKeyDown}
      />
      <S.DoneButton
        $hasValue={hasValue}
        onClick={onClickDoneButton}
        aria-label="입력 완료"
        ref={doneButtonRef}
      >
        →
      </S.DoneButton>
    </S.Container>
  );
};

export default CustomMenuInput;
