import useFetch from '@/apis/rest/useFetch';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { colorList } from '@/constants/color';

type ProbabilityResponse = {
  playerName: string;
  probability: number;
};

const useRouletteProbabilities = () => {
  const { joinCode } = useIdentifier();
  const { getParticipantColorIndex } = useParticipants();
  const { updateCurrentProbabilities } = useProbabilityHistory();

  const { loading: isLoading } = useFetch<ProbabilityResponse[]>({
    endpoint: `/rooms/${joinCode}/probabilities`,
    enabled: !!joinCode,
    onSuccess: (data) => {
      updateCurrentProbabilities(
        data.map((probability) => ({
          ...probability,
          playerColor: colorList[getParticipantColorIndex(probability.playerName)],
        }))
      );
    },
  });

  return {
    isLoading,
  };
};

export default useRouletteProbabilities;
