import Description from '@/components/@common/Description/Description';
import Headline2 from '@/components/@common/Headline2/Headline2';
import { ROUND_MAP, RoundType } from '@/types/miniGame/round';
import CircularProgress from '../CircularProgress/CircularProgress';
import * as S from './Round.styled';

type Props = {
  round: RoundType;
  currentTime: number;
  roundTotalTime: number;
  isTimerActive: boolean;
};

const RoundHeader = ({ round, currentTime, roundTotalTime, isTimerActive }: Props) => {
  return (
    <S.TitleContainer>
      <S.TitleWrapper>
        <Headline2>Round {ROUND_MAP[round]}</Headline2>
        <Description>카드를 골라주세요!</Description>
      </S.TitleWrapper>
      <S.CircularProgressWrapper>
        <CircularProgress current={currentTime} total={roundTotalTime} isActive={isTimerActive} />
      </S.CircularProgressWrapper>
    </S.TitleContainer>
  );
};

export default RoundHeader;
