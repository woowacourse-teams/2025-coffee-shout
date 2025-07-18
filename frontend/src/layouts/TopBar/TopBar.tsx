import { TopContainer } from './TopBar.styled';

const Top = ({ hasBackIcon = true }: { hasBackIcon?: boolean }) => (
  <TopContainer>{hasBackIcon && <img src="/images/back-icon.svg" alt="뒤로가기" />}</TopContainer>
);

export default Top;
