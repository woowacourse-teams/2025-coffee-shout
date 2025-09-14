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
      console.log('해당 사용자 닉네임이 없음 - 홈으로 리디렉션');
      navigate('/', { replace: true });
      return;
    }

    const currentUser = participants.find((participant) => participant.playerName === myName);

    console.log('participants: ', participants);
    console.log('myName: ', myName);
    console.log('currentUser: ', currentUser);

    if (!participants.length || !currentUser) {
      console.log('사용자 정보에서 자기 자신을 찾을 수 없음 - 홈으로 리디렉션');
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
