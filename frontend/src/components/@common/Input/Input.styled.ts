import styled from '@emotion/styled';

type ContainerProps = { height: string; hasValue: boolean };
type ClearButtonProps = { hasValue: boolean };

export const Container = styled.div<ContainerProps>`
  display: flex;
  align-items: center;
  width: 100%;
  height: ${({ height }) => height};
  border-bottom: ${({ theme, hasValue }) =>
    `2px solid ${hasValue ? theme.color.gray[400] : theme.color.gray[200]}`};
`;

export const Input = styled.input`
  flex: 1;
  outline: none;
  border: none;
  background: transparent;
  padding: 4px;

  width: 80%;
  color: ${({ theme }) => theme.color.gray[700]};
  ${({ theme }) => theme.typography.h4};

  &:hover:not(:disabled) {
    border-color: #666666;
  }

  &:disabled {
    cursor: not-allowed;
  }

  &::placeholder {
    color: ${({ theme }) => theme.color.gray[300]};
    opacity: 1;
  }

  &:-webkit-autofill {
    -webkit-box-shadow: 0 0 0 1000px white inset;
    -webkit-text-fill-color: ${({ theme }) => theme.color.gray[900]};
  }
`;

export const ClearButton = styled.button<ClearButtonProps>`
  background-color: white;
  width: 20%;
  font-size: 20px;
  border: none;
  outline: none;
  cursor: pointer;
  visibility: ${({ hasValue }) => (hasValue ? 'visible' : 'hidden')};
`;

export const CloseIcon = styled.img`
  color: ${({ theme }) => theme.color.gray[300]};
`;
