import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { RouletteWinnerResponse } from '@/types/roulette';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const useRoulettePlay = () => {
  const { joinCode, myName } = useIdentifier();
  const { send } = useWebSocket();
  const navigate = useNavigate();
  const [winner, setWinner] = useState<string | null>(null);
  const [randomAngle, setRandomAngle] = useState(0);
  const [isSpinning, setIsSpinning] = useState(false);

  const startSpinWithResult = (data: RouletteWinnerResponse) => {
    setWinner(data.playerName);
    setRandomAngle(data.randomAngle);
    setIsSpinning(true);
  };

  const handleSpinClick = () => {
    send(`/room/${joinCode}/spin-roulette`, { hostName: myName });
  };

  useEffect(() => {
    // TODO: 당첨자가 나오지 않았을 때, 에러 처리 방식 정하기
    if (!winner || !winner.trim()) console.warn('당첨자가 추첨되지 않았습니다.');

    if (isSpinning) {
      const timer = setTimeout(() => {
        setIsSpinning(false);
        navigate(`/room/${joinCode}/roulette/result`, { state: { winner } });
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [isSpinning, winner, navigate, joinCode]);

  return {
    winner,
    randomAngle,
    isSpinning,
    handleSpinClick,
    startSpinWithResult,
  };
};

export default useRoulettePlay;
