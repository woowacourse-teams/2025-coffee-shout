import styled from '@emotion/styled';
import { cardVariants, circleVariants } from '../../constants/variants';

type Props = {
  $size?: 'small' | 'medium' | 'large';
};

const fontSize = {
  small: '0.625rem', // ~10px
  medium: '0.875rem', // ~14px
  large: '1rem', // ~16px
};

const iconSize = {
  small: '0.625rem', // ~10px
  medium: '0.875rem', // ~14px
  large: '1rem', // ~16px
};

const playerMarginTop = {
  small: '0.1875rem', // ~3px
  medium: '0.375rem', // ~6px
  large: '0.5rem', // ~8px
};

const playerNameMaxWidth = {
  small: '2rem', // ~32px
  medium: '3rem', // ~48px
  large: '4.5rem', // ~72px
};

export const Container = styled.button<Props>`
  ${({ $size }) => cardVariants[$size || 'large']}
  background-color: ${({ theme }) => theme.color.point[200]};

  border-radius: 7px;
  box-shadow: 0 3px 3px rgba(0, 0, 0, 0.4);
  position: relative;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  }
`;

export const Circle = styled.div<Props>`
  background-color: ${({ theme }) => theme.color.point[50]};
  width: ${({ $size }) => circleVariants[$size || 'large']};
  height: ${({ $size }) => circleVariants[$size || 'large']};

  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
`;

export const Player = styled.div<Props>`
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: ${({ $size }) => playerMarginTop[$size || 'large']};
`;

export const PlayerName = styled.span<Props>`
  font-size: ${({ $size }) => fontSize[$size || 'large']};
  max-width: ${({ $size }) => playerNameMaxWidth[$size || 'large']};

  line-height: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
`;

export const PlayerIcon = styled.img<Props>`
  width: ${({ $size }) => iconSize[$size || 'large']};
  height: ${({ $size }) => iconSize[$size || 'large']};

  border-radius: 50%;
  display: block;
  flex-shrink: 0;
`;
