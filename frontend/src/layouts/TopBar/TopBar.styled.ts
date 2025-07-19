import styled from '@emotion/styled';

type AlignType = 'start' | 'center' | 'end' | 'stretch';
type SectionProps = {
  $align?: AlignType;
};

export const Container = styled.div`
  width: 100%;
  height: 2.6rem;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
`;

export const LeftSection = styled.div<SectionProps>`
  justify-self: start;
  align-self: ${({ $align }) => $align || 'center'};
`;

export const CenterSection = styled.div<SectionProps>`
  justify-self: center;
  align-self: ${({ $align }) => $align || 'center'};
`;

export const RightSection = styled.div<SectionProps>`
  justify-self: end;
  align-self: ${({ $align }) => $align || 'center'};
`;
