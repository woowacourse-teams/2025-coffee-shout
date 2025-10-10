import { type ComponentProps } from 'react';
import * as S from './TextButton.styled';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';

type Props = {
  text: string;
  onClick: () => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const TextButton = ({ text, onClick, ...rest }: Props) => {
  const { touchState, handleTouchDown, handleTouchUp } = useTouchInteraction();

  return (
    <S.Container
      onPointerDown={handleTouchDown}
      onPointerUp={(e) => {
        handleTouchUp(e);
        onClick();
      }}
      $touchState={touchState}
      {...rest}
    >
      {text}
    </S.Container>
  );
};

export default TextButton;
