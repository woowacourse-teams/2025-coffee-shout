import { PlayerType } from '@/types/player';
import { useEffect, useState } from 'react';

const useReadyAnnouncement = (
  isAllReady: boolean,
  participantCount: number,
  playerType: PlayerType | null
) => {
  const [announcement, setAnnouncement] = useState<string>('');

  useEffect(() => {
    if (isAllReady && participantCount >= 2) {
      if (playerType === 'HOST') {
        setAnnouncement('모든 참가자가 준비되었습니다. 게임 시작 버튼을 눌러주세요.');
      } else {
        setAnnouncement(
          '모든 참가자가 준비되었습니다. 호스트가 게임을 시작할 때까지 기다려주세요.'
        );
      }
      setTimeout(() => setAnnouncement(''), 100);
    } else {
      setAnnouncement('아직 모든 참가자가 준비되지 않았습니다.');
      setTimeout(() => setAnnouncement(''), 100);
    }
  }, [isAllReady, participantCount, playerType]);

  return announcement;
};

export default useReadyAnnouncement;
