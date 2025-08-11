import styled from '@emotion/styled';

export const ContentContainer = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
`;

export const ImageContainer = styled.div`
  width: 200px;
  height: 380px;
  margin: 16px 0px;
`;

export const TextContainer = styled.div`
  margin-bottom: 20px;
`;

export const DescriptionWrapper = styled.div`
  margin: 10px 0px;
`;

export const Description = styled.p`
  ${({ theme }) => theme.typography.small}
  line-height: 1.5;
`;
