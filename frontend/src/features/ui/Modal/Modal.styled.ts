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
  display: flex;
  width: 100%;
  height: 66%;
  overflow: hidden;
  margin: 0 24px;
  flex-direction: column;
  gap: 5px;
  padding: 32px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow:
    0 3px 6px rgba(0, 0, 0, 0.16),
    0 3px 6px rgba(0, 0, 0, 0.23);
`;

export const Content = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
`;
