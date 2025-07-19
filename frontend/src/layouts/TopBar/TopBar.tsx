import React from 'react';
import { Container } from './TopBar.styled';

type Props = {
  backButton?: React.ReactElement;
};

const TopBar = ({ backButton }: Props) => {
  return <Container>{backButton}</Container>;
};

export default TopBar;
