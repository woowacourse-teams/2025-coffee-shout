import * as S from './Divider.styled';

type Props = {
  color?: string;
  height?: string;
  width?: string;
};

const Divider = ({ color, height = '1px', width = '100%' }: Props) => {
  return <S.Container $color={color} $height={height} $width={width} />;
};

export default Divider;
