import { PropsWithChildren } from 'react';
import TopBar from './TopBar/TopBar';
import Banner from './Banner/Banner';
import Content from './Content/Content';
import ButtonBar from './ButtonBar/ButtonBar';
import { COLOR_MAP, ColorKey } from '@/constants/color';
import * as S from './Layout.styled';

type LayoutProps = {
  color?: ColorKey;
} & PropsWithChildren;

const Layout = ({ color = 'white', children }: LayoutProps) => (
  <S.LayoutContainer $color={COLOR_MAP[color]}>{children}</S.LayoutContainer>
);

Layout.TopBar = TopBar;
Layout.Banner = Banner;
Layout.Content = Content;
Layout.ButtonBar = ButtonBar;

export default Layout;
