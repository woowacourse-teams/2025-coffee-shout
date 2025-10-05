import { type ComponentProps, type MouseEvent, type TouchEvent } from 'react';
import * as S from './TextButton.styled';
import { checkIsTouchDevice } from '@/utils/checkIsTouchDevice';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';

type Props = {
  text: string;
  onClick: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const TextButton = ({ text, onClick, ...rest }: Props) => {
  const { touchState, handleTouchStart, handleTouchEnd } = useTouchInteraction({ onClick });
  const isTouchDevice = checkIsTouchDevice();

  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    if (isTouchDevice) return;
    onClick(e);
  };

  return (
    <S.Container
      onClick={handleClick}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
      $touchState={touchState}
      {...rest}
    >
      {text}
    </S.Container>
  );
};

export default TextButton;
