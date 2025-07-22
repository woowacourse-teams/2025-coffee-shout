import MiniGameTransition from './MiniGameTransition';
import CardsStackIcon from '@/assets/card-stack-icon.svg';
import styled from '@emotion/styled';

export default {
  title: '@composition/MiniGameTransition',
  component: MiniGameTransition,
};

export const Default = () => (
  <RootContainer>
    <MiniGameTransition>
      <img src={CardsStackIcon} alt="cards" />
    </MiniGameTransition>
  </RootContainer>
);
const RootContainer = styled.div`
  max-width: 430px;
  width: 100%;
  height: 100dvh;
  margin: 0 auto;
`;
