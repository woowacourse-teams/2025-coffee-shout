import { Card, SelectedCardInfo } from '@/types/miniGame/cardGame';
import CardBack from '../CardBack/CardBack';
import CardFront from '../CardFront/CardFront';
import * as S from './PlayerCardDisplay.styled';

type Props = {
  selectedCardInfo: SelectedCardInfo;
};

const PlayerCardDisplay = ({ selectedCardInfo }: Props) => {
  const renderPlayerCard = (round: 'FIRST' | 'SECOND') => {
    const cardInfo = selectedCardInfo[round];

    if (cardInfo.isSelected) {
      return (
        <CardFront
          size="medium"
          card={
            {
              type: cardInfo.type,
              value: cardInfo.value,
            } as Card
          }
        />
      );
    }

    return <CardBack size="medium" disabled={true} />;
  };

  return (
    <S.Container>
      {renderPlayerCard('FIRST')}
      {renderPlayerCard('SECOND')}
    </S.Container>
  );
};

export default PlayerCardDisplay;
