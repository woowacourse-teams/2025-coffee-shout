import styled from '@emotion/styled';
import Headline3 from '../Headline3/Headline3';
import Description from '../Description/Description';

export const Container = styled.button`
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 28px 20px;
  gap: 20px;
  width: 331px;
  height: 135px;
  background-color: ${({ theme }) => theme.color.gray[50]};
  border-radius: 12px;
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

export const RoomDescription = styled(Description)`
  color: ${({ theme }) => theme.color.gray[400]};
`;
