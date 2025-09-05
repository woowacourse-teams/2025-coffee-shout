import { TemperatureOption } from '@/types/menu';
import styled from '@emotion/styled';

type Props = {
  $temperature: TemperatureOption;
};

export const Container = styled.div<Props>`
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 40px;
  background-color: ${({ $temperature }) => ($temperature === 'HOT' ? '#FF4242' : '#236ED8')};
  border-radius: 4px;
  color: ${({ theme }) => theme.color.white};
  ${({ theme }) => theme.typography.paragraph}
`;
