import styled from '@emotion/styled';

export const Container = styled.button`
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 20px;

  width: 100%;
  min-width: 310px;
  height: 135px;
  background-color: ${({ theme }) => theme.color.gray[50]};
  border-radius: 12px;
  padding: 28px 20px;

  &:hover,
  &:active {
    background-color: ${({ theme }) => theme.color.gray[200]};
  }
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
