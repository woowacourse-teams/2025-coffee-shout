import { useState, type ComponentProps, type MouseEvent, type TouchEvent } from 'react';
import * as S from './IconButton.styled';
import { isTouchDevice } from '@/utils/isTouchDevice';

type Props = {
  iconSrc: string;
  onClick: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const IconButton = ({ iconSrc, onClick, ...rest }: Props) => {
  const isTouch = isTouchDevice();
  const [isTouching, setIsTouching] = useState(false);

  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    if (isTouch) return;
    onClick(e);
  };

  const handleTouchStart = (e: TouchEvent<HTMLButtonElement>) => {
    if (!isTouch) return;
    e.preventDefault();
    setIsTouching(true);
  };

  const handleTouchEnd = (e: TouchEvent<HTMLButtonElement>) => {
    if (!isTouch) return;
    e.preventDefault();
    onClick(e);
    setIsTouching(false);
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
