import { type ComponentProps, type PointerEvent } from 'react';

import { useButtonInteraction } from '@/hooks/useButtonInteraction';

import { Size } from '@/types/styles';

import * as S from './Button.styled';

type Props = {
  variant?: S.ButtonVariant;
  onClick?: () => void;
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

  const { touchState, pointerHandlers } = useButtonInteraction({
    onClick,
  });

  const handlePointerUp = (e: PointerEvent<HTMLButtonElement>) => {
    if (isDisabled) return;
    pointerHandlers.onPointerUp(e);
  };

  const showLoading = variant === 'loading' || isLoading;

  const renderContent = () => {
    if (!showLoading) return children;
    if (children && typeof children === 'string') return <LoadingText text={children} />;
    return <LoadingDots />;
  };

  return (
    <S.Container
      type="button"
      $variant={variant}
      $touchState={touchState}
      $width={width}
      $height={height}
      $isLoading={isLoading}
      disabled={isDisabled}
      {...pointerHandlers}
      onPointerUp={handlePointerUp}
      {...rest}
    >
      {renderContent()}
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

const LoadingText = ({ text }: { text: string }) => (
  <S.LoadingText>
    {text.split('').map((char, index) => (
      <span key={index}>{char === ' ' ? '\u00A0' : char}</span>
    ))}
  </S.LoadingText>
);
