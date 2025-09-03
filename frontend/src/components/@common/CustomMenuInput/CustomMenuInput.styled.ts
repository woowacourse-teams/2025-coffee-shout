import styled from '@emotion/styled';

export const Container = styled.div`
  width: 100%;
  height: 40px;
  border: 1px solid ${({ theme }) => theme.color.gray[200]};
  padding-left: 16px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px;
`;

export const Input = styled.input`
  width: 100%;
  height: 100%;
  border: none;
  outline: none;
  padding-left: 16px;
  ${({ theme }) => theme.typography.paragraph}
`;

type DoneButtonProps = { $hasValue: boolean };

export const DoneButton = styled.button<DoneButtonProps>`
  width: 56px;
  height: 100%;
  border-radius: 3px;
  background-color: ${({ theme, $hasValue }) =>
    $hasValue ? theme.color.point[400] : theme.color.gray[200]};
  color: ${({ theme }) => theme.color.white};
  ${({ theme }) => theme.typography.h3}
`;
