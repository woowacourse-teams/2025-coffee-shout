import { ColorList } from '@/constants/color';
import * as S from './CircleIcon.styled';

type Props = {
  color: ColorList;
  iconSrc: string;
  iconAlt?: string;
};

const CircleIcon = ({ color, iconSrc, iconAlt = 'icon' }: Props) => {
  return (
    <S.Container color={color}>
      <S.Icon src={iconSrc} alt={iconAlt} />
    </S.Container>
  );
};

export default CircleIcon;
