import { theme } from '@/styles/theme';
import styled from '@emotion/styled';
import { TemperatureOption } from '@/types/menu';

type Props = {
  $position: 'left' | 'right';
  $option: TemperatureOption;
  $selected: boolean;
};

export const Container = styled.div<Props>`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background-color: ${({ $option, $selected }) => getBackgroundColor($option, $selected)};
  border: 1px solid
    ${({ $option, $selected }) =>
      $selected ? getBackgroundColor($option, $selected) : theme.color.gray[200]};
  ${({ $position }) => ($position === 'left' ? 'border-right: none;' : 'border-left: none;')}
  border-radius: ${({ $position }) => ($position === 'left' ? '4px 0 0 4px' : '0 4px 4px 0')};
  cursor: pointer;
  ${({ theme }) => theme.typography.paragraph}
  color: ${({ $selected }) => ($selected ? theme.color.white : theme.color.gray[200])};
`;

const getBackgroundColor = (option: TemperatureOption, selected: boolean) => {
  if (option === 'HOT' && selected) return theme.color.red;
  if (option === 'ICE' && selected) return theme.color.blue;
  return theme.color.white;
};
