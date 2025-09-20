import styled from '@emotion/styled';

export const BannerContent = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  gap: 20px;
`;

export const Logo = styled.img`
  width: 120px;
`;

export const IconWrapper = styled.div`
  margin-bottom: 1.5rem;

  svg {
    width: 4rem;
    height: 4rem;
    stroke-width: 1.5;
  }
`;

export const ListHeader = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
`;
