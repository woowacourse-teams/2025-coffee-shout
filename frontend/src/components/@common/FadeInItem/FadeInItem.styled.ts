import styled from '@emotion/styled';

export const Wrapper = styled.div<{ 
  $index: number; 
  $delay: number; 
  $duration: number; 
}>`
  animation: fadeInUp ${({ $duration }) => $duration}s ease-out 
    ${({ $index, $delay }) => $index * $delay}s both;

  @keyframes fadeInUp {
    0% {
      opacity: 0;
      transform: translateY(20px);
    }
    100% {
      opacity: 1;
      transform: translateY(0);
    }
  }
`;
