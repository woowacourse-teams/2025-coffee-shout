import type { ComponentProps } from 'react';
import * as S from './Button.styled';

type Props = Omit<ComponentProps<'button'>, 'disabled'> & S.Props;

const Button = ({
  variant = 'primary',
  width = '328px',
  height = '50px',
  children,
  ...rest
}: Props) => {
  const isDisabled = variant === 'disabled' || variant === 'loading';

  return (
    <S.Container
      type="button"
      variant={variant}
      width={width}
      height={height}
      disabled={isDisabled}
      {...rest}
    >
      {children}
    </S.Container>
  );
};

export default Button;
