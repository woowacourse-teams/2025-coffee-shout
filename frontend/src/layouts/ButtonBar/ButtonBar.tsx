import { Children, ReactElement } from 'react';
import * as S from './ButtonBar.styled';

type ButtonBarProps = {
  children: ReactElement | ReactElement[];
  flexRatios?: number[];
};

const ButtonBar = ({ children, flexRatios }: ButtonBarProps) => {
  if (flexRatios?.some((ratio) => ratio <= 0)) {
    throw new Error('flexRatio는 0보다 큰 양수여야 합니다');
  }

  const buttons = [...Children.toArray(children)];
  const defaultRatio = 1;

  return (
    <S.Container>
      {buttons.map((button, index) => {
        const ratio = flexRatios?.[index] ?? defaultRatio;

        return (
          <S.Wrapper key={index} $flexRatio={ratio}>
            {button}
          </S.Wrapper>
        );
      })}
    </S.Container>
  );
};

export default ButtonBar;
