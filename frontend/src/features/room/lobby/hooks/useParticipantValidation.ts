import { api } from '@/apis/rest/api';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import { useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

type Props = {
  isConnected: boolean;
};

export const useParticipantValidation = ({ isConnected }: Props) => {
  const { myName, joinCode } = useIdentifier();
  const { participants } = useParticipants();
  const { playerType } = usePlayerType();
  const navigate = useNavigate();

  const validateUserExistsAndRedirect = useCallback(async () => {
    if (!joinCode) {
      console.log('joinCode가 없음 - 홈으로 리디렉션');
      navigate('/', { replace: true });
      return;
    }

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

    try {
      const { exist } = await api.get<{ exist: boolean }>(
        `/rooms/check-joinCode?joinCode=${joinCode}`
      );

      if (!exist) {
        console.log('방이 존재하지 않음 - 홈으로 리디렉션');
        navigate('/', { replace: true });
        return;
      }
    } catch (error) {
      console.error('방 존재 여부 체크 실패:', error);
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
  }, [playerType, myName, joinCode, participants, navigate]);

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
