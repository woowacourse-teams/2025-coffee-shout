import { useEffect } from 'react';
import Slide from '../components/Slide/Slide';
import { useNavigate, useParams } from 'react-router-dom';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import * as S from './CardGameReadyPage.styled';
import CardGameDescription1 from '@/assets/card_game_desc1.svg';
import CardGameDescription2 from '@/assets/card_game_desc2.svg';

const CardGameReadyPage = () => {
  const navigate = useNavigate();
  const { joinCode } = useIdentifier();
  const { miniGameType } = useParams();
  const TOTAL_DURATION = 3000;

  const slideData = [
    {
      textLines: ['각 라운드마다', '카드 1장을 선택하세요'],
      image: <S.Image src={CardGameDescription1} />,
      className: 'slide-first',
    },
    {
      textLines: ['합산된 값으로', '등수가 결정됩니다'],
      image: <S.Image src={CardGameDescription2} />,
      className: 'slide-second',
    },
  ];

  useEffect(() => {
    const gameStartTimer = setTimeout(() => {
      navigate(`/room/${joinCode}/${miniGameType}/play`);
    }, TOTAL_DURATION);

    return () => {
      clearTimeout(gameStartTimer);
    };
  }, []);

  return (
    <S.Container>
      {slideData.map((slide, index) => (
        <Slide
          key={index}
          textLines={slide.textLines}
          image={slide.image}
          className={slide.className}
        />
      ))}
    </S.Container>
  );
};

export default CardGameReadyPage;
