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
  width: 50%;

  @media (max-height: 650px) {
    width: 45%;
  }

  @media (max-height: 580px) {
    width: 35%;
  }

  @media (max-height: 500px) {
    width: 25%;
  }
`;

export const ButtonContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
`;
