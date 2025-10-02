import { ReactNode, MouseEvent } from 'react';
import * as S from './RacingGameOverlay.styled';

type Props = {
  children: ReactNode;
};

const RacingGameOverlay = ({ children }: Props) => {
  const handleClick = (event: MouseEvent<HTMLDivElement>) => {
    event.stopPropagation();
    console.log('clicked');
  };

  return <S.Overlay onClick={handleClick}>{children}</S.Overlay>;
};

export default RacingGameOverlay;
