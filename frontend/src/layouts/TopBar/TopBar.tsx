import React from 'react';
import { Container } from './TopBar.styled';

type Props = {
  backButton?: React.ReactElement;
};

const Top = ({ backButton }: Props) => {
  return <Container>{backButton}</Container>;
};

export default Top;
