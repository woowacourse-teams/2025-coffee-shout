import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import { useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

type Props = {
  isConnected: boolean;
};

export const useParticipantValidation = ({ isConnected }: Props) => {
  const { myName } = useIdentifier();
  const { participants } = useParticipants();
  const { playerType } = usePlayerType();
  const navigate = useNavigate();

  const validateUserExistsAndRedirect = useCallback(() => {
    // TODO: 방 생존 여부 받아서 처리 (participants가 아무도 없을 때를 의존하지 않고 방 생존 여부에 의존하기)

    if (!playerType) {
      console.log('playerType이 없음 - 홈으로 리디렉션');
      navigate('/', { replace: true });
      return;
    }

    if (!myName) {
      console.log('해당 사용자 닉네임이 없음 - 홈으로 리디렉션');
      navigate('/', { replace: true });
      return;
    }

    if (!participants.length) {
      console.log('participants가 아직 로드되지 않음 - 체크 건너뜀');
      return;
    }

    const currentUser = participants.find((participant) => participant.playerName === myName);

    if (!currentUser) {
      console.log('사용자 정보에서 자기 자신을 찾을 수 없음 - 홈으로 리디렉션');
      navigate('/', { replace: true });
    }
  }, [playerType, myName, participants, navigate]);

  /**
   * 웹소켓 연결되고 participants가 로드된 후 유효성 검사
   */
  useEffect(() => {
    if (isConnected && participants.length > 0) {
      const timeoutId = setTimeout(() => {
        validateUserExistsAndRedirect();
      }, 500);

      return () => clearTimeout(timeoutId);
    }
  }, [isConnected, participants.length, validateUserExistsAndRedirect]);
};
