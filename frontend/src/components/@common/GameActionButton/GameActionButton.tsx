import { ComponentProps } from 'react';
import Headline4 from '../Headline4/Headline4';
import * as S from './GameActionButton.styled';

type Props = {
  onClick: () => void;
  isSelected: boolean;
  gameName: string; // 추가: 게임 이름을 props로 받기
} & Omit<ComponentProps<'button'>, 'onClick'>; // onClick은 이미 정의되어 있으므로 제외

const GameActionButton = ({ onClick, isSelected, gameName, ...restProps }: Props) => {
  return (
    <S.Container onClick={onClick} isSelected={isSelected} {...restProps}>
      <S.GameNameWrapper>
        <Headline4 color={isSelected ? 'white' : 'point-400'}>{gameName}</Headline4>
      </S.GameNameWrapper>
      <S.InfoIcon src={`/images/info-${isSelected ? 'white' : 'gray'}.svg`} alt="info" />
      <S.GameIcon isSelected={isSelected}>🎮</S.GameIcon>
    </S.Container>
  );
};

export default GameActionButton;
