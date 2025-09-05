import * as S from './CircleIcon.styled';

type Props = {
  color: string;
  imgUrl: string;
  iconAlt?: string;
};

const CircleIcon = ({ color, imgUrl, iconAlt = 'icon' }: Props) => {
  return (
    <S.Container $color={color}>
      <S.Icon src={imgUrl} alt={iconAlt} />
    </S.Container>
  );
};

export default CircleIcon;
