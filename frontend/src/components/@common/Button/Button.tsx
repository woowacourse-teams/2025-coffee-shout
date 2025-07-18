import type { ComponentProps } from 'react';
import * as S from './Button.styled';

type Props = {
  variant: S.ButtonVariant;
  width: string;
  height: 'small' | 'medium' | 'large';
} & Omit<ComponentProps<'button'>, 'disabled'>;

const Button = ({
  variant = 'primary',
  width = '100%',
  height = 'large',
  children,
  ...rest
}: Props) => {
  const isDisabled = variant === 'disabled' || variant === 'loading';

  return (
    <S.Container
      type="button"
      $variant={variant}
      $width={width}
      $height={height}
      disabled={isDisabled}
      {...rest}
    >
      {children}
    </S.Container>
  );
};

export default Button;
