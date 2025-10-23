import { TemperatureOption } from '@/types/menu';
import * as S from './TemperatureOnly.styled';
import useAutoFocus from '@/hooks/useAutoFocus';

type Props = {
  temperature: TemperatureOption;
};

const TemperatureOnly = ({ temperature }: Props) => {
  const liveRef = useAutoFocus<HTMLDivElement>();

  return (
    <S.Container $temperature={temperature} ref={liveRef}>{`${temperature} ONLY`}</S.Container>
  );
};

export default TemperatureOnly;
