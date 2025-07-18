import React from 'react';
import { TopContainer } from './TopBar.styled';

type Props = {
  backButton?: React.ReactElement;
};

const Top = ({ backButton }: Props) => {
  return <TopContainer>{backButton}</TopContainer>;
};

export default Top;
