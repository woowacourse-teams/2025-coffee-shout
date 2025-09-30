import { Card, SelectedCardInfo } from '@/types/miniGame/cardGame';
import CardBack from '../CardBack/CardBack';
import CardFront from '../CardFront/CardFront';

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
    <>
      {renderPlayerCard('FIRST')}
      {renderPlayerCard('SECOND')}
    </>
  );
};

export default PlayerCardDisplay;
