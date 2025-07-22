import InfoGrayIcon from '@/assets/images/gray-info.svg';
import InfoWhiteIcon from '@/assets/images/white-info.svg';
import { ComponentProps } from 'react';
import Headline4 from '../Headline4/Headline4';
import * as S from './GameActionButton.styled';

type Props = {
  onClick: () => void;
  isSelected: boolean;
  gameName: string;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const GameActionButton = ({ onClick, isSelected, gameName, ...rest }: Props) => {
  return (
    <S.Container onClick={onClick} $isSelected={isSelected} {...rest}>
      <S.GameNameWrapper>
        <Headline4 color={isSelected ? 'white' : 'point-400'}>{gameName}</Headline4>
      </S.GameNameWrapper>
      <S.InfoIcon src={isSelected ? InfoWhiteIcon : InfoGrayIcon} alt="info" />
      <S.GameIcon $isSelected={isSelected}>🎮</S.GameIcon>
    </S.Container>
  );
};

export default GameActionButton;
