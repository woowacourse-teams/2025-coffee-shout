import styled from '@emotion/styled';

const SPACING = {
  PADDING_VERTICAL: 20,
  PADDING_HORIZONTAL: 18,
  ICON_SIZE: 23,
  BORDER_DIFFERENCE: 2,
} as const;

const getAdjustedPadding = (isSelected: boolean) => {
  if (isSelected) {
    return {
      vertical: SPACING.PADDING_VERTICAL - SPACING.BORDER_DIFFERENCE,
      horizontal: SPACING.PADDING_HORIZONTAL - SPACING.BORDER_DIFFERENCE,
    };
  }
  return {
    vertical: SPACING.PADDING_VERTICAL,
    horizontal: SPACING.PADDING_HORIZONTAL,
  };
};

type Props = {
  $isSelected: boolean;
  $disabled?: boolean;
};

export const Container = styled.button<Props>`
  position: relative;
  display: flex;
  justify-content: space-between;
  background-color: ${({ theme, $isSelected }) =>
    $isSelected ? theme.color.point[400] : theme.color.white};

  border: ${({ theme, $isSelected }) =>
    $isSelected ? `3px solid ${theme.color.point[200]}` : `1px solid ${theme.color.point[200]}`};
  border-radius: 12px;

  width: 100%;
  height: 130px;
  padding: ${({ $isSelected }) => getAdjustedPadding($isSelected).vertical}px
    ${({ $isSelected }) => getAdjustedPadding($isSelected).horizontal}px;

  ${({ $disabled }) =>
    !$disabled &&
    `
    transition: transform 0.2s ease;

    &:active {
      transform: scale(0.98);
    }
  `}
`;

export const GameNameWrapper = styled.div``;

export const InfoIcon = styled.img`
  width: ${SPACING.ICON_SIZE}px;
  height: ${SPACING.ICON_SIZE}px;
`;

export const GameIcon = styled.div<Props>`
  position: absolute;
  bottom: ${({ $isSelected }) => getAdjustedPadding($isSelected).vertical}px;
  right: ${({ $isSelected }) => getAdjustedPadding($isSelected).horizontal}px;
`;
