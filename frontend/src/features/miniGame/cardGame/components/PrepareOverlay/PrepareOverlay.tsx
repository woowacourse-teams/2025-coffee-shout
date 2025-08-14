import Headline1 from '@/components/@common/Headline1/Headline1';
import * as S from './PrepareOverlay.styled';
import chatBubble from '@/assets/chat_bubble.svg';
import coffee from '@/assets/coffee-white.svg';
import { useEffect, useState } from 'react';

const PrepareOverlay = () => {
  const [displayText, setDisplayText] = useState<string>('READY');

  useEffect(() => {
    const timer = setTimeout(() => {
      setDisplayText('START!');
    }, 1000);

    return () => clearTimeout(timer);
  }, []);

  return (
    <S.Backdrop>
      <S.Content>
        <S.ImageWrapper>
          <S.BubbleImage src={chatBubble} />
          <Headline1 color="white">{displayText}</Headline1>
        </S.ImageWrapper>
        <S.CoffeeImage src={coffee} />
      </S.Content>
    </S.Backdrop>
  );
};

export default PrepareOverlay;
