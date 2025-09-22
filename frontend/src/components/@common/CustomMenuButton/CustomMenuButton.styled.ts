import { Z_INDEX } from '@/constants/zIndex';
import { buttonHoverPress } from '@/styles/animations/buttonHoverPress';
import styled from '@emotion/styled';

type Props = {
  $isTouching: boolean;
};

export const Container = styled.button<Props>`
  width: 120px;
  height: 50px;
  background-color: ${({ theme }) => theme.color.point[400]};
  border-radius: 100px;
  padding: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;

  /* 플로팅 버튼 위치 */
  @media (max-width: 430px) {
    position: fixed;
  }
  @media (min-width: 431px) {
    position: absolute;
  }
  bottom: 20px;
  right: 20px;
  z-index: ${Z_INDEX.CUSTOM_MENU_BUTTON};

  ${({ theme, $isTouching }) =>
    buttonHoverPress({ activeColor: theme.color.point[500], isTouching: $isTouching })}
`;

export const Icon = styled.img`
  width: 15px;
  height: 15px;
  object-fit: contain;
`;

export const Text = styled.div`
  ${({ theme }) => theme.typography.small}
  color: ${({ theme }) => theme.color.white};
`;
