import styled from '@emotion/styled';

type Props = {
  $size?: 'small' | 'medium' | 'large';
};

const cardVariants = {
  small: {
    width: '12vw',
    height: '15.36vw',
  },
  medium: {
    width: '16vw',
    height: '20.48vw',
  },
  large: {
    width: '24vw',
    height: '30.72vw',
  },
};

const circleVariants = {
  small: '8.6vw',
  medium: '11.5vw',
  large: '17.3vw',
};

export const Container = styled.button<Props>`
  ${({ $size }) => cardVariants[$size || 'large']}
  border: 2px solid ${({ theme }) => theme.color.point[200]};
  background-color: ${({ theme }) => theme.color.point[400]};
  border-radius: 7px;
  box-shadow: 0 3px 3px rgba(0, 0, 0, 0.4);
  position: relative;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  }

  /* 모바일 터치 효과 제거 */
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  outline: none;

  /* 더블클릭 및 더블탭 확대 차단 */
  touch-action: manipulation;
`;

export const Circle = styled.div<Props>`
  background-color: ${({ theme }) => theme.color.point[300]};
  width: ${({ $size }) => circleVariants[$size || 'large']};
  height: ${({ $size }) => circleVariants[$size || 'large']};
  border-radius: 50%;
  margin: auto;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
`;

export const CoffeeIcon = styled.img`
  width: 100%;
  height: 100%;
`;
