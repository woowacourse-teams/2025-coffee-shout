import { type ComponentProps, type MouseEvent, type TouchEvent } from 'react';
import * as S from './IconButton.styled';
import { checkIsTouchDevice } from '@/utils/checkIsTouchDevice';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';

type Props = {
  iconSrc: string;
  onClick: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const IconButton = ({ iconSrc, onClick, ...rest }: Props) => {
  const { isTouching, startTouchPress, endTouchPress } = useTouchInteraction();
  const isTouchDevice = checkIsTouchDevice();

  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    if (isTouchDevice) return;
    onClick(e);
  };

  const handleTouchStart = (e: TouchEvent<HTMLButtonElement>) => {
    if (!isTouchDevice) return;
    e.preventDefault();
    startTouchPress();
  };

  const handleTouchEnd = (e: TouchEvent<HTMLButtonElement>) => {
    if (!isTouchDevice) return;
    e.preventDefault();
    onClick(e);
    endTouchPress();
  };

  return (
    <S.Container
      onClick={handleClick}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
      $isTouching={isTouching}
      {...rest}
    >
      <S.Icon src={iconSrc} alt={'icon-button'} />
    </S.Container>
  );
};

export default IconButton;
