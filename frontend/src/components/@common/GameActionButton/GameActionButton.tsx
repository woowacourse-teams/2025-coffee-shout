import InfoGrayIcon from '@/assets/gray-info.svg';
import InfoWhiteIcon from '@/assets/white-info.svg';
import { ComponentProps } from 'react';
import Headline4 from '../Headline4/Headline4';
import * as S from './GameActionButton.styled';
import { usePlayerRole } from '@/contexts/PlayerRoleContext';

type Props = {
  onClick: () => void;
  isSelected: boolean;
  gameName: string;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const GameActionButton = ({ onClick, isSelected, gameName, ...rest }: Props) => {
  const { playerRole } = usePlayerRole();

  //TODO: 다른 에러 처리방식을 찾아보기
  if (!playerRole) return null;

  const handleClick = () => {
    if (playerRole === 'GUEST') return;
    onClick();
  };

  return (
    <S.Container
      onClick={handleClick}
      $isSelected={isSelected}
      $disabled={playerRole === 'GUEST'}
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
