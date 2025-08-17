import type { ComponentProps, MouseEvent, TouchEvent } from 'react';
import * as S from './Button.styled';
import { isTouchDevice } from '@/utils/isTouchDevice';

type Props = {
  onClick?: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
  variant?: S.ButtonVariant;
  width?: string;
  height?: string;
} & Omit<ComponentProps<'button'>, 'disabled'>;

const Button = ({
  variant = 'primary',
  width = '100%',
  height = '45px',
  children,
  onClick,
  ...rest
}: Props) => {
  const isDisabled = variant === 'disabled' || variant === 'loading';
  const isTouch = isTouchDevice();

  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    if (isTouch) return;
    if (isDisabled) return;

    onClick?.(e);
  };

  const handleTouchEnd = (e: TouchEvent<HTMLButtonElement>) => {
    if (!isTouch) return;
    if (isDisabled) return;

    e.preventDefault();

    onClick?.(e);
  };

  return (
    <S.Container
      type="button"
      $variant={variant}
      $width={width}
      $height={height}
      disabled={isDisabled}
      onClick={handleClick}
      onTouchEnd={handleTouchEnd}
      {...rest}
    >
      {children}
    </S.Container>
  );
};

export default Button;
