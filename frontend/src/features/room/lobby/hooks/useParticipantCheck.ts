import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { useCallback, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

type Props = {
  isConnected: boolean;
};

export const useParticipantCheck = ({ isConnected }: Props) => {
  const { myName } = useIdentifier();
  const { participants } = useParticipants();
  const navigate = useNavigate();
  const wasReconnected = useRef(false);

  const checkUserExistsAndRedirect = useCallback(() => {
    if (!myName) {
      navigate('/', { replace: true });
      return;
    }

    const currentUser = participants.find((participant) => participant.playerName === myName);

    if (!participants.length || !currentUser) {
      navigate('/', { replace: true });
    }
  }, [myName, participants, navigate]);

  useEffect(() => {
    if (isConnected && wasReconnected.current) {
      const timeoutId = setTimeout(() => {
        checkUserExistsAndRedirect();
        wasReconnected.current = false;
      }, 500);

      return () => clearTimeout(timeoutId);
    }
  }, [isConnected, participants, checkUserExistsAndRedirect]);

  useEffect(() => {
    if (!isConnected) {
      wasReconnected.current = true;
    }
  }, [isConnected]);
};
