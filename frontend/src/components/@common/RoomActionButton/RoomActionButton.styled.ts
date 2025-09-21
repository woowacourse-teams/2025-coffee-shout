import styled from '@emotion/styled';
import { buttonHoverPress } from '@/styles/animations/buttonHoverPress';

type Props = {
  $isTouching: boolean;
};

export const Container = styled.button<Props>`
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 20px;

  width: 100%;
  height: 130px;
  border-radius: 12px;
  padding: 28px 20px;
  background-color: ${({ theme }) => theme.color.gray[50]};

  ${({ theme, $isTouching }) =>
    buttonHoverPress({
      activeColor: theme.color.gray[200],
      isTouching: $isTouching,
    })}
`;

export const NextStepIcon = styled.img`
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
`;

export const DescriptionBox = styled.div`
  display: flex;
  align-items: flex-start;
`;
