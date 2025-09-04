import styled from '@emotion/styled';

type Props = {
  color: string;
};

export const Container = styled.div<Props>`
  display: flex;
  background-color: ${({ color }) => color};
  overflow: hidden;
  position: relative;
  width: 100%;
  height: 40px;
  border-radius: 4px;
  align-items: center;
  gap: 10px;
  padding-left: 8px;
`;

export const Icon = styled.img`
  object-fit: cover;
  object-position: top;
  flex-shrink: 0;
  margin-top: 14px;
  margin-left: -8px;
  width: 40px;
  height: 40px;
`;

export const Text = styled.span`
  ${({ theme }) => theme.typography.paragraph}
  color: ${({ theme }) => theme.color.gray[800]};
`;
