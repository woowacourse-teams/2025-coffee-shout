import { api } from '@/apis/rest/api';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { colorList } from '@/constants/color';
import { useEffect, useRef, useState } from 'react';

type ProbabilityResponse = {
  playerName: string;
  probability: number;
};

const useRouletteProbabilities = () => {
  const { joinCode } = useIdentifier();
  const { getParticipantColorIndex } = useParticipants();
  const { updateCurrentProbabilities } = useProbabilityHistory();
  const [isLoading, setIsLoading] = useState(true);

  const isFirst = useRef<boolean>(false);

  useEffect(() => {
    (async () => {
      try {
        //개발모드일때 1번만 실행하도록 조건 추가
        if (process.env.NODE_ENV === 'development') {
          if (isFirst.current) {
            return;
          }
          isFirst.current = true;
        }

        const data = await api.get<ProbabilityResponse[]>(`/rooms/${joinCode}/probabilities`);
        updateCurrentProbabilities(
          data.map((probability) => ({
            ...probability,
            playerColor: colorList[getParticipantColorIndex(probability.playerName)],
          }))
        );
      } catch (error) {
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    })();
  }, [updateCurrentProbabilities, getParticipantColorIndex, joinCode]);

  return {
    isLoading,
  };
};

export default useRouletteProbabilities;
