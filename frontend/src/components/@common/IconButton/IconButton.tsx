import { ComponentProps } from 'react';
import * as S from './IconButton.styled';

type Props = {
  iconSrc: string;
  onClick: () => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const IconButton = ({ iconSrc, onClick, ...rest }: Props) => {
  return (
    <S.Container onClick={onClick} {...rest}>
      <S.Icon src={iconSrc} alt={'icon-button'} />
    </S.Container>
  );
};

export default IconButton;
