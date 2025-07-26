import { type ComponentProps, type MouseEvent, type TouchEvent } from 'react';
import * as S from './IconButton.styled';
import { isTouchDevice } from '@/utils/isTouchDevice';

type Props = {
  iconSrc: string;
  onClick: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const IconButton = ({ iconSrc, onClick, ...rest }: Props) => {
  const isTouch = isTouchDevice();

  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    if (isTouch) return;
    onClick(e);
  };

  const handleTouchEnd = (e: TouchEvent<HTMLButtonElement>) => {
    if (!isTouch) return;
    e.preventDefault();
    onClick(e);
  };

  return (
    <S.Container onClick={handleClick} onTouchEnd={handleTouchEnd} {...rest}>
      <S.Icon src={iconSrc} alt={'icon-button'} />
    </S.Container>
  );
};

export default IconButton;
