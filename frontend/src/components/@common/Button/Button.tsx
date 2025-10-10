import { type ComponentProps, type PointerEvent } from 'react';
import * as S from './Button.styled';
import { Size } from '@/types/styles';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import { useCancelablePointer } from '@/hooks/useCancelablePointer';

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

  // 애니메이션 상태 관리
  const {
    touchState,
    handleTouchDown: handleTouchDownAnimation,
    handleTouchUp: handleTouchUpAnimation,
  } = useTouchInteraction();

  // 스크롤 감지 + 클릭 처리
  const {
    handlePointerDown: handlePointerDownCancel,
    handlePointerMove: handlePointerMoveCancel,
    handlePointerUp: handlePointerUpCancel,
  } = useCancelablePointer({
    onClick,
  });

  const handlePointerDown = (e: PointerEvent<HTMLButtonElement>) => {
    handleTouchDownAnimation(e);
    if (e.pointerType === 'touch') {
      handlePointerDownCancel(e);
    }
  };

  const handlePointerMove = (e: PointerEvent<HTMLButtonElement>) => {
    if (e.pointerType === 'touch') {
      handlePointerMoveCancel(e);
    }
  };

  const handlePointerUp = (e: PointerEvent<HTMLButtonElement>) => {
    if (isDisabled) return;

    handleTouchUpAnimation(e);
    if (e.pointerType === 'touch') {
      handlePointerUpCancel(e);
    } else {
      onClick?.();
    }
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
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
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
