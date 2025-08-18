import styled from '@emotion/styled';

export const Banner = styled.div`
  height: 100%;
  padding: 40px;
  line-height: 30px;
  position: relative;
`;

export const Logo = styled.img`
  position: absolute;
  bottom: 20px;
  right: 20px;
  -webkit-user-drag: none;
  user-select: none;

  @media (max-height: 675px) {
    display: none;
  }
`;

export const ButtonContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
`;
