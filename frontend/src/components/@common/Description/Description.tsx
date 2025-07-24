import { COLOR_MAP, ColorKey } from '@/constants/color';
import { PropsWithChildren } from 'react';
import * as S from './Description.styled';

type Props = { color?: ColorKey } & PropsWithChildren;

const Description = ({ children, color = 'gray-700' as ColorKey }: Props) => {
  const resolvedColor = COLOR_MAP[color];
  return <S.Container $color={resolvedColor}>{children}</S.Container>;
};

export default Description;
