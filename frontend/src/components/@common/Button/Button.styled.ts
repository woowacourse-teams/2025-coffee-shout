import styled from '@emotion/styled';

type ButtonVariant = 'primary' | 'secondary' | 'disabled' | 'loading';

export type Props = {
  variant?: ButtonVariant;
  width?: string;
  height?: string;
};

export const Container = styled.button<Props>`
  width: ${({ width }) => width || '328px'};
  height: ${({ height }) => height || '50px'};
  ${({ theme }) => theme.typography.h4}
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 12px;
  cursor: pointer;

  ${({ variant, theme }) => {
    switch (variant) {
      case 'secondary':
        return `
          background: ${theme.color.gray[50]};
          color: ${theme.color.gray[700]};
          &:hover { background: ${theme.color.gray[100]}; }
          &:active { background: ${theme.color.gray[100]}; }
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

      case 'primary':
      default:
        return `
          background: ${theme.color.point[400]};
          color: ${theme.color.white};
          &:hover { background: ${theme.color.point[500]}; }
          &:active { background: ${theme.color.point[500]}; }
        `;
    }
  }}
`;
