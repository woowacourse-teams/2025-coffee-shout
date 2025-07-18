import styled from '@emotion/styled';

export type ContainerProps = {
  width?: string;
  height?: string;
};

export type TriggerProps = {
  disabled?: boolean;
  isOpen: boolean;
};

export type SelectTextProps = {
  hasValue: boolean;
  disabled?: boolean;
};

export type ArrowIconProps = {
  isOpen: boolean;
  disabled?: boolean;
};

export type ContentProps = {
  isOpen: boolean;
};

export type ItemProps = {
  disabled?: boolean;
  selected?: boolean;
};

export const Container = styled.div<ContainerProps>`
  width: ${({ width }) => width};
  height: ${({ height }) => height};
  position: relative;
`;

export const Trigger = styled.div<TriggerProps>`
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px;
  background-color: ${({ disabled, theme }) => (disabled ? theme.color.gray[50] : 'white')};

  border-bottom: 2px solid
    ${({ theme, isOpen }) => {
      if (isOpen) return theme.color.gray[400];
      return theme.color.gray[200];
    }};

  cursor: ${({ disabled }) => (disabled ? 'default' : 'pointer')};
  user-select: none;

  &:hover:not(:disabled) {
    border-bottom-color: ${({ theme, disabled }) => {
      if (disabled) return theme.color.gray[200];
      return theme.color.gray[400];
    }};
  }

  &:focus-within {
    outline: none;
    border-bottom-color: ${({ theme }) => theme.color.gray[400]};
  }
`;

export const SelectText = styled.span<SelectTextProps>`
  color: ${({ theme, hasValue, disabled }) => {
    if (disabled) return theme.color.gray[400];
    if (hasValue) return theme.color.gray[700];
    return theme.color.gray[300];
  }};

  ${({ theme }) => theme.typography.h4}
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  flex: 1;
`;

export const ArrowIcon = styled.div<ArrowIconProps>`
  width: 0;
  height: 0;
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 6px solid
    ${({ theme, disabled }) => (disabled ? theme.color.gray[300] : theme.color.gray[400])};

  transform: ${({ isOpen }) => (isOpen ? 'rotate(180deg)' : 'rotate(0deg)')};
  transition: transform 0.2s ease;
  margin-left: 8px;
`;

export const Content = styled.ul<ContentProps>`
  position: absolute;
  top: 40px;
  left: 0;
  right: 0;
  z-index: 1;

  background-color: white;
  border: 1px solid ${({ theme }) => theme.color.gray[200]};
  border-radius: 4px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);

  max-height: 200px;
  overflow-y: auto;

  margin: 0;
  padding: 0;
  list-style: none;

  opacity: ${({ isOpen }) => (isOpen ? 1 : 0)};
  visibility: ${({ isOpen }) => (isOpen ? 'visible' : 'hidden')};
  transform: ${({ isOpen }) => (isOpen ? 'translateY(0)' : 'translateY(-10px)')};
  transition: all 0.2s ease;
`;

export const Item = styled.li<ItemProps>`
  padding: 8px 12px;
  cursor: ${({ disabled }) => (disabled ? 'default' : 'pointer')};

  background-color: ${({ selected, theme }) => (selected ? theme.color.gray[100] : 'transparent')};

  color: ${({ theme, disabled, selected }) => {
    if (disabled) return theme.color.gray[300];
    if (selected) return theme.color.gray[900];
    return theme.color.gray[700];
  }};

  ${({ selected, theme }) => {
    if (selected) return theme.typography.h4;
  }};

  &:hover:not([disabled]) {
    background-color: ${({ theme, selected }) =>
      selected ? theme.color.gray[100] : theme.color.gray[50]};
  }
`;
