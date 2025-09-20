import { api } from '@/apis/rest/api';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { colorList } from '@/constants/color';
import { useEffect } from 'react';

type ProbabilityResponse = {
  playerName: string;
  probability: number;
};

const useRouletteProbabilities = () => {
  const { joinCode } = useIdentifier();
  const { getParticipantColorIndex } = useParticipants();
  const { probabilityHistory, updateCurrentProbabilities } = useProbabilityHistory();

  useEffect(() => {
    (async () => {
      const data = await api.get<ProbabilityResponse[]>(`/room/${joinCode}/probabilities`);
      updateCurrentProbabilities(
        data.map((probability) => ({
          ...probability,
          playerColor: colorList[getParticipantColorIndex(probability.playerName)],
        }))
      );
    })();
  }, [joinCode, getParticipantColorIndex, updateCurrentProbabilities]);

  return {
    probabilityHistory,
  };
};

export default useRouletteProbabilities;
