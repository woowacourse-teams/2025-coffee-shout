import * as S from './Divider.styled';

type DividerProps = {
  color?: string;
  height?: string;
  width?: string;
};

const Divider = ({ color, height = '1px', width = '100%' }: DividerProps) => {
  return <S.Container $color={color} $height={height} $width={width} />;
};

export default Divider;
