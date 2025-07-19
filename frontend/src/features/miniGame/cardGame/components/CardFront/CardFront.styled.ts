import styled from '@emotion/styled';
import { cardVariants, circleVariants } from '../../constants/variants';

type Props = {
  $size?: 'small' | 'medium' | 'large';
};

const fontSize = {
  small: '2.0vw',
  medium: '2.67vw',
  large: '4.0vw',

  // TODO: clamp로 최소 및 최댓값 설정하여 너무 작거나 커지지 않도록 조정
  // small: 'clamp(12px, 2.0vw, 16px)',
  // medium: 'clamp(14px, 2.67vw, 20px)',
  // large: 'clamp(18px, 4.0vw, 28px)',
};

const iconSize = {
  small: '2.0vw',
  medium: '2.67vw',
  large: '4.0vw',
};

const playerMarginTop = {
  small: '1.0vw',
  medium: '1.33vw',
  large: '2.0vw',
};

const playerNameMaxWidth = {
  small: '8vw',
  medium: '10vw',
  large: '14vw',
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
