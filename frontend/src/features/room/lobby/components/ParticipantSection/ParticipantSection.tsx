import MenuIcon from '@/assets/juice.svg';
import Divider from '@/components/@common/Divider/Divider';
import useModal from '@/components/@common/Modal/useModal';
import ProgressCounter from '@/components/@common/ProgressCounter/ProgressCounter';
import PlayerCard from '@/components/@composition/PlayerCard/PlayerCard';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import MenuModifyModal from '@/features/room/lobby/components/MenuModifyModal/MenuModifyModal';
import * as S from './ParticipantSection.styled';

export const ParticipantSection = () => {
  const { openModal, closeModal } = useModal();
  const { myName } = useIdentifier();

  const handleModifyMenu = () => {
    openModal(<MenuModifyModal onClose={closeModal} />, {
      title: '음료 변경',
      showCloseButton: true,
    });
  };

  return (
    <>
      <SectionTitle
        title="참가자"
        description="음료 아이콘을 누르면 음료를 변경할 수 있습니다"
        suffix={<ProgressCounter current={7} total={9} />}
      />
      <PlayerCard name={myName} iconColor="red">
        <S.Menu src={MenuIcon} onClick={handleModifyMenu} />
      </PlayerCard>

      <Divider />

      <S.ScrollableWrapper>
        {['다이앤', '니야', '메리', '루키', '한스', '꾹이', '엠제이', '1'].map((name) => (
          <PlayerCard key={name} name={name} iconColor="red">
            <S.Menu src={MenuIcon} onClick={handleModifyMenu} />
          </PlayerCard>
        ))}
      </S.ScrollableWrapper>
      <S.BottomGap />
    </>
  );
};
