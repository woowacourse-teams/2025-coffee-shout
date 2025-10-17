import styled from '@emotion/styled';

export const OrderList = styled.section`
  height: calc(100% - 4rem);
  overflow-y: scroll;
`;

export const OrderItem = styled.article`
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 0;
`;

export const Divider = styled.hr`
  border-top: 1px dashed #d1d5db;
`;

export const TotalWrapper = styled.div`
  display: flex;
  justify-content: flex-end;
  margin: 20px 0;
`;
