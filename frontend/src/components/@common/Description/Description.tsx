import { PropsWithChildren } from 'react';
import { COLOR_MAP } from '@/constants/colorMap';
import { ColorKey } from '@/types/colorKey';
import * as S from './Description.styled';

type Props = { color?: ColorKey } & PropsWithChildren;

const Description = ({ children, color = 'gray-700' as ColorKey }: Props) => {
  const resolvedColor = COLOR_MAP[color];
  return <S.Container $color={resolvedColor}>{children}</S.Container>;
};

export default Description;
