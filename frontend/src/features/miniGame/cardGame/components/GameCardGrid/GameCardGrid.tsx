import { colorList } from '@/constants/color';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { Card, CardInfo } from '@/types/miniGame/cardGame';
import CardBack from '../CardBack/CardBack';
import CardFront from '../CardFront/CardFront';
import * as S from './GameCardGrid.styled';

type Props = {
  cardInfos: CardInfo[];
  onCardClick: (cardIndex: number) => void;
};

const GameCardGrid = ({ cardInfos, onCardClick }: Props) => {
  const { getParticipantColorIndex } = useParticipants();

  const renderCard = (cardInfo: CardInfo, index: number) => {
    if (cardInfo.selected) {
      const playerColor = cardInfo.playerName
        ? colorList[getParticipantColorIndex(cardInfo.playerName)]
        : null;

      return (
        <CardFront
          key={index}
          card={
            {
              type: cardInfo.cardType,
              value: cardInfo.value,
            } as Card
          }
          playerColor={playerColor}
        />
      );
    }

    return <CardBack key={index} onClick={() => onCardClick(index)} />;
  };

  return <S.Container>{cardInfos.map(renderCard)}</S.Container>;
};

export default GameCardGrid;
