import Divider from '@/components/@common/Divider/Divider';
import Headline4 from '@/components/@common/Headline4/Headline4';
import PlayerCard from '@/components/@composition/PlayerCard/PlayerCard';
import { colorList } from '@/constants/color';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { PlayerProbability } from '@/types/roulette';
import * as S from './ProbabilityList.styled';

type Props = {
  playerProbabilities: PlayerProbability[];
};

const ProbabilityList = ({ playerProbabilities }: Props) => {
  const { myName } = useIdentifier();
  const myProbability = playerProbabilities.find(({ playerName }) => playerName === myName);

  const filteredParticipants = playerProbabilities.filter(
    ({ playerName }) => playerName !== myName
  );

  return (
    <>
      <PlayerCard name={myProbability ? myProbability.playerName : myName} iconColor="#FF6B6B">
        <Headline4>{myProbability ? `${myProbability.probability}` : '100'}%</Headline4>
      </PlayerCard>
      <Divider />
      <S.ScrollableWrapper>
        {filteredParticipants.length === 0 ? (
          <S.Empty>현재 참여한 인원이 없습니다</S.Empty>
        ) : (
          filteredParticipants.map(({ playerName, probability }, index) => (
            // TODO: colorList를 index로 접근하면 player가 나가거나 들어올때마다 색상이 당겨지면서 계속 바뀔 것 같아서
            // 이 부분을 아예 특정 사람에게 아예 지정해버리는 걸로 가는게 좋을듯
            // 우선은 RouletteWheel 컴포넌트 내부에서 index로 색상을 나눠주고 있어서 여기도 index로 통일함
            <PlayerCard key={playerName} name={playerName} iconColor={colorList[index]}>
              <Headline4>{probability}%</Headline4>
            </PlayerCard>
          ))
        )}
      </S.ScrollableWrapper>
      <S.BottomGap />
    </>
  );
};

export default ProbabilityList;
