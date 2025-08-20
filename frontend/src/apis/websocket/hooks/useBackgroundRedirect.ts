import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { useCallback, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

type Props = {
  isConnected: boolean;
  isVisible: boolean;
};

export const useBackgroundRedirect = ({ isConnected, isVisible }: Props) => {
  // TODO: 웹소켓 provider에 도메인 정보가 있는 것은 좋지 않음. 추후 리팩토링 필요
  const { myName } = useIdentifier();
  const { participants } = useParticipants();
  const wasConnectedBeforeBackground = useRef(false);
  const navigate = useNavigate();

  const checkUserExistsAndRedirect = useCallback(() => {
    if (!myName) return;

    const currentUser = participants.find((participant) => participant.playerName === myName);

    if (!participants.length || !currentUser) {
      navigate('/', { replace: true });
    }
  }, [myName, participants, navigate]);

  useEffect(() => {
    if (!isVisible && isConnected) {
      wasConnectedBeforeBackground.current = true;
    } else if (isVisible && wasConnectedBeforeBackground.current) {
      checkUserExistsAndRedirect();
    }
  }, [isVisible, isConnected, checkUserExistsAndRedirect]);
};
