import { DESIGN_TOKENS } from '@/constants/design';
import styled from '@emotion/styled';

export const PlayerNameText = styled.text`
  fill: ${({ theme }) => theme.color.point[100]};
  font-size: ${DESIGN_TOKENS.typography.small};
  font-weight: bold;
`;
