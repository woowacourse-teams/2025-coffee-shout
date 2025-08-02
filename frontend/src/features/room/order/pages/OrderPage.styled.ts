import styled from '@emotion/styled';

export const BannerContent = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: white;
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

export const OrderList = styled.div`
  height: calc(100% - 4rem);
  overflow-y: scroll;
`;

export const OrderItem = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 0;
`;

export const Divider = styled.div`
  border-top: 1px dashed #d1d5db;
`;

export const TotalWrapper = styled.div`
  display: flex;
  justify-content: flex-end;
  margin: 20px 0;
`;

export const Logo = styled.img`
  width: 100px;
  margin-bottom: 1rem;
`;
