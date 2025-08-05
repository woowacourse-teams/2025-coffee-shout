import { Size } from '@/types/styles';
import styled from '@emotion/styled';

export type ButtonVariant = 'primary' | 'secondary' | 'disabled' | 'loading' | 'ready';

type Props = {
  $height: Size;
  $variant: ButtonVariant;
  $width: string;
};

export const Container = styled.button<Props>`
  width: ${({ $width }) => $width};
  height: ${({ $height }) => {
    switch ($height) {
      case 'small':
        return '40px';
      case 'medium':
        return '45px';
      case 'large':
        return '50px';
      default:
        return '50px';
    }
  }};
  ${({ theme }) => theme.typography.h4}
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 12px;
  cursor: pointer;

  ${({ $variant, theme }) => {
    switch ($variant) {
      case 'secondary':
        return `
          background: ${theme.color.gray[50]};
          color: ${theme.color.gray[700]};
          @media (hover: hover) and (pointer: fine) {
            &:hover { background: ${theme.color.gray[100]}; }
          }
          @media (hover: none) {
            &:active { background: ${theme.color.gray[100]}; }
          }
        `;

      case 'loading':
        return `
          background: ${theme.color.point[200]};
          color: ${theme.color.white};
          cursor: default;
        `;

      case 'disabled':
        return `
          background: ${theme.color.gray[200]};
          color: ${theme.color.white};
          cursor: default;
          opacity: 0.7;
        `;

      case 'ready':
        return `
          background: ${theme.color.point[50]};
          color: ${theme.color.point[400]};
        `;

      case 'primary':
      default:
        return `
          background: ${theme.color.point[400]};
          color: ${theme.color.white};
          @media (hover: hover) and (pointer: fine) {
            &:hover { background: ${theme.color.point[500]}; }
          }
          @media (hover: none) {
            &:active { background: ${theme.color.point[500]}; }
          }
        `;
    }
  }}
`;
