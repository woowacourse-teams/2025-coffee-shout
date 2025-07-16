import { PropsWithChildren } from 'react';
import { COLOR_MAP, ColorKey } from '@/constants/color';
import * as S from './Headline2.styled';

type Props = { color?: ColorKey } & PropsWithChildren;

const Headline2 = ({ children, color = 'gray-700' as ColorKey }: Props) => {
  const resolvedColor = COLOR_MAP[color];
  return <S.Container $color={resolvedColor}>{children}</S.Container>;
};

export default Headline2;
