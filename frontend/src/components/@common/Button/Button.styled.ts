import { buttonHoverPress } from '@/styles/animations/buttonHoverPress';
import { Size } from '@/types/styles';
import { css } from '@emotion/react';
import styled from '@emotion/styled';

export type ButtonVariant = 'primary' | 'secondary' | 'disabled' | 'loading' | 'ready';

type Props = {
  $variant: ButtonVariant;
  $width: string;
  $height: Size;
  $isTouching: boolean;
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

  ${({ $variant, theme, $isTouching }) => {
    switch ($variant) {
      case 'secondary': {
        const baseColor = theme.color.gray[50];
        const activeColor = theme.color.gray[100];
        const hoverPressStyles = buttonHoverPress({ activeColor, isTouching: $isTouching });

        return css`
          background: ${baseColor};
          color: ${theme.color.gray[700]};
          ${hoverPressStyles}
        `;
      }

      case 'loading':
        return css`
          background: ${theme.color.point[200]};
          color: ${theme.color.white};
          cursor: default;
        `;

      case 'disabled':
        return css`
          background: ${theme.color.gray[200]};
          color: ${theme.color.white};
          cursor: default;
          opacity: 0.7;
        `;

      case 'ready':
        return css`
          background: ${theme.color.point[50]};
          color: ${theme.color.point[400]};
        `;

      case 'primary':
      default: {
        const baseColor = theme.color.point[400];
        const activeColor = theme.color.point[500];
        const hoverPressStyles = buttonHoverPress({ activeColor, isTouching: $isTouching });

        return css`
          background: ${baseColor};
          color: ${theme.color.white};
          ${hoverPressStyles}
        `;
      }
    }
  }}
`;
