import { type ComponentProps, type MouseEvent, type TouchEvent } from 'react';
import * as S from './Button.styled';
import { checkIsTouchDevice } from '@/utils/checkIsTouchDevice';
import { Size } from '@/types/styles';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';

type Props = {
  variant?: S.ButtonVariant;
  onClick?: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
  isLoading?: boolean;
  width?: string;
  height?: Size;
} & Omit<ComponentProps<'button'>, 'disabled'>;

const Button = ({
  variant = 'primary',
  isLoading = false,
  width = '100%',
  height = 'large',
  children,
  onClick,
  ...rest
}: Props) => {
  const isDisabled = variant === 'disabled' || variant === 'loading' || isLoading;
  const { touchState, handleTouchStart, handleTouchEnd } = useTouchInteraction({
    onClick,
    isDisabled,
  });
  const isTouchDevice = checkIsTouchDevice();

  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    if (isTouchDevice) return;
    if (isDisabled) return;

    onClick?.(e);
  };

  const showLoading = variant === 'loading' || isLoading;

  return (
    <S.Container
      type="button"
      $variant={variant}
      $touchState={touchState}
      $width={width}
      $height={height}
      $isLoading={isLoading}
      disabled={isDisabled}
      onClick={handleClick}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
      {...rest}
    >
      {showLoading ? <LoadingDots /> : children}
    </S.Container>
  );
};

export default Button;

const LoadingDots = () => (
  <S.LoadingDots>
    <span />
    <span />
    <span />
  </S.LoadingDots>
);
