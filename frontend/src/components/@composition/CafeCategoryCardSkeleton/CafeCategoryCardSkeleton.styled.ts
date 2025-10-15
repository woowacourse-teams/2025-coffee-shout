import styled from '@emotion/styled';
import { theme } from '@/styles/theme';

export const Container = styled.div`
  position: relative;
  background-color: ${({ theme }) => theme.color.white};
  width: 100%;
  min-height: 81px;
`;

export const Content = styled.div`
  display: flex;
  align-items: center;
  width: 100%;
  padding: 15px 0;
  border-bottom: 1px solid ${theme.color.gray[200]};
`;

export const IconWrapper = styled.div`
  flex-shrink: 0;
  margin-right: 20px;
`;

export const TextWrapper = styled.div`
  flex: 1;
  min-width: 0;
  padding-right: 20px;
`;
