import { Z_INDEX } from '@/constants/zIndex';
import styled from '@emotion/styled';

export const Backdrop = styled.div`
  display: flex;
  width: 100%;
  height: 100%;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.3);
  inset: 0;
  position: fixed;
  z-index: ${Z_INDEX.MODAL};
`;

export const Container = styled.div`
  width: 100%;
  max-height: 90%;
  margin: 0 24px;
  padding: 16px;
  overflow: hidden;
  background-color: ${({ theme }) => theme.color.white};
  border-radius: 12px;
  box-shadow:
    0 3px 6px rgba(0, 0, 0, 0.16),
    0 3px 6px rgba(0, 0, 0, 0.23);
`;
