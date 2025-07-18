import { PropsWithChildren } from 'react';
import { TopContainer } from './TopBar.styled';

const Top = ({ children }: PropsWithChildren) => {
  return <TopContainer>{children}</TopContainer>;
};

export default Top;
