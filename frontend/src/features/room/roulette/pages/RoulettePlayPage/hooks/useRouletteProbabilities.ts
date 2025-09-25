import { api } from '@/apis/rest/api';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { colorList } from '@/constants/color';
import { useEffect, useState } from 'react';

type ProbabilityResponse = {
  playerName: string;
  probability: number;
};

const useRouletteProbabilities = () => {
  const { joinCode } = useIdentifier();
  const { getParticipantColorIndex } = useParticipants();
  const { updateCurrentProbabilities } = useProbabilityHistory();
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
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
  }, []);

  return {
    isLoading,
  };
};

export default useRouletteProbabilities;
