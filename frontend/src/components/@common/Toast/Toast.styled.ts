import { Z_INDEX } from '@/constants/zIndex';
import styled from '@emotion/styled';
import { ToastType } from './types';

type Props = {
  $type: ToastType;
};

const TOAST_COLORS = {
  success: '#ace6a8',
  error: '#ffc9c9',
  warning: '#f9e29c',
  info: '#90caf9',
} as const;

export const Container = styled.div<Props>`
  position: fixed;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 90%;
  height: 45px;
  max-width: 400px;
  max-height: 60px;
  background-color: ${({ theme }) => theme.color.white};
  border: 1px solid ${({ $type }) => TOAST_COLORS[$type]};
  border-radius: 8px;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.12),
    0 1px 2px rgba(0, 0, 0, 0.24);
  padding: 4px 16px;
  z-index: ${Z_INDEX.TOAST};
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);

  > * {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 100%;
  }
`;
