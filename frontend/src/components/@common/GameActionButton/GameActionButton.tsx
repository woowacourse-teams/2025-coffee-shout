import InfoGrayIcon from '@/assets/gray-info.svg';
import InfoWhiteIcon from '@/assets/white-info.svg';
import { ComponentProps, useState } from 'react';
import Headline4 from '../Headline4/Headline4';
import * as S from './GameActionButton.styled';
import { UserRole } from '@/types/player';

type Props = {
  onClick: () => void;
  isSelected: boolean;
  gameName: string;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const GameActionButton = ({ onClick, isSelected, gameName, ...rest }: Props) => {
  const [userRole] = useState<UserRole>('HOST');

  const handleClick = () => {
    if (userRole === 'GUEST') return;
    onClick();
  };

  return (
    <S.Container
      onClick={handleClick}
      $isSelected={isSelected}
      $disabled={userRole === 'GUEST'}
      {...rest}
    >
      <S.GameNameWrapper>
        <Headline4 color={isSelected ? 'white' : 'point-400'}>{gameName}</Headline4>
      </S.GameNameWrapper>
      <S.InfoIcon src={isSelected ? InfoWhiteIcon : InfoGrayIcon} alt="info" />
      <S.GameIcon $isSelected={isSelected}>🎮</S.GameIcon>
    </S.Container>
  );
};

export default GameActionButton;
