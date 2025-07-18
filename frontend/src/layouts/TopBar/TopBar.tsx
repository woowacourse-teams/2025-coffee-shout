import React from 'react';
import { TopContainer } from './TopBar.styled';

type Props = {
  children: React.ReactElement;
};

const Top = ({ children }: Props) => {
  return <TopContainer>{children}</TopContainer>;
};

export default Top;
