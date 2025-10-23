import { TemperatureOption } from '@/types/menu';
import * as S from './TemperatureOnly.styled';

type Props = {
  temperature: TemperatureOption;
};

const TemperatureOnly = ({ temperature }: Props) => {
  return <S.Container $temperature={temperature}>{`${temperature} ONLY`}</S.Container>;
};

export default TemperatureOnly;
