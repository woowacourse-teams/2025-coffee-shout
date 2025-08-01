import Divider from '@/components/@common/Divider/Divider';
import useModal from '@/components/@common/Modal/useModal';
import ProgressCounter from '@/components/@common/ProgressCounter/ProgressCounter';
import PlayerCard from '@/components/@composition/PlayerCard/PlayerCard';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import MenuModifyModal from '@/features/room/lobby/components/MenuModifyModal/MenuModifyModal';
import { Player } from '@/types/player';
import * as S from './ParticipantSection.styled';
import { getMenuIcon } from './utils/getMenuIcon';

const TOTAL_PARTICIPANTS = 9;

type Props = { participants: Player[] };

export const ParticipantSection = ({ participants }: Props) => {
  const { myName } = useIdentifier();
  const { openModal, closeModal } = useModal();

  const handleModifyMenu = () => {
    openModal(<MenuModifyModal myMenu={mySelect.menuResponse.name} onClose={closeModal} />, {
      title: '음료 변경',
      showCloseButton: true,
    });
  };

  const mySelect = participants.filter((participant) => participant.playerName === myName)[0];
  const filteredParticipants = participants.filter(
    (participant) => participant.playerName !== myName
  );

  return (
    <>
      <SectionTitle
        title="참가자"
        description="음료 아이콘을 누르면 음료를 변경할 수 있습니다"
        suffix={<ProgressCounter current={participants.length} total={TOTAL_PARTICIPANTS} />}
      />
      <PlayerCard name={myName} iconColor="#FF6B6B">
        <S.Menu
          src={getMenuIcon(mySelect && mySelect.menuResponse.menuType)}
          onClick={handleModifyMenu}
        />
      </PlayerCard>
      <Divider />
      <S.ScrollableWrapper>
        {filteredParticipants.length === 0 ? (
          <S.Empty>현재 참여한 인원이 없습니다</S.Empty>
        ) : (
          filteredParticipants.map((participant) => (
            <PlayerCard
              key={participant.playerName}
              name={participant.playerName}
              iconColor="#FF6B6B"
            >
              <S.Menu src={getMenuIcon(participant.menuResponse.menuType)} />
            </PlayerCard>
          ))
        )}
      </S.ScrollableWrapper>
      <S.BottomGap />
    </>
  );
};
