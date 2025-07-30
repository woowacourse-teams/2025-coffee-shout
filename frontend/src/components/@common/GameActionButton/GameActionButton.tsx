import InfoGrayIcon from '@/assets/gray-info.svg';
import InfoWhiteIcon from '@/assets/white-info.svg';
import { ComponentProps } from 'react';
import Headline4 from '../Headline4/Headline4';
import * as S from './GameActionButton.styled';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';

type Props = {
  onClick: () => void;
  isSelected: boolean;
  gameName: string;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const GameActionButton = ({ onClick, isSelected, gameName, ...rest }: Props) => {
  const { playerType } = usePlayerType();

  //TODO: 다른 에러 처리방식을 찾아보기
  if (!playerType) return null;

  const handleClick = () => {
    if (playerType === 'GUEST') return;
    onClick();
  };

  return (
    <S.Container
      onClick={handleClick}
      $isSelected={isSelected}
      $disabled={playerType === 'GUEST'}
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
