import { type ComponentProps, type MouseEvent, type TouchEvent } from 'react';
import * as S from './Button.styled';
import { checkIsTouchDevice } from '@/utils/checkIsTouchDevice';
import { Size } from '@/types/styles';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';

type Props = {
  onClick?: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
  variant?: S.ButtonVariant;
  width?: string;
  height?: Size;
} & Omit<ComponentProps<'button'>, 'disabled'>;

const Button = ({
  variant = 'primary',
  width = '100%',
  height = 'large',
  children,
  onClick,
  ...rest
}: Props) => {
  const isDisabled = variant === 'disabled' || variant === 'loading';
  const { isTouching, handleTouchStart, handleTouchEnd } = useTouchInteraction({
    onClick: onClick || (() => {}),
    isDisabled,
  });
  const isTouchDevice = checkIsTouchDevice();

  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    if (isTouchDevice) return;
    if (isDisabled) return;

    onClick?.(e);
  };

  return (
    <S.Container
      type="button"
      $variant={variant}
      $isTouching={isTouching}
      $width={width}
      $height={height}
      disabled={isDisabled}
      onClick={handleClick}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
      {...rest}
    >
      {children}
    </S.Container>
  );
};

export default Button;
