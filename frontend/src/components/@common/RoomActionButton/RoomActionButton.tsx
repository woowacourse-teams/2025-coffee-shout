import { ComponentProps } from 'react';
import Headline3 from '../Headline3/Headline3';
import * as S from './RoomActionButton.styled';
import Description from '../Description/Description';

type Props = {
  title: string;
  descriptions: string[];
} & ComponentProps<'button'>;

const RoomActionButton = ({ title, descriptions }: Props) => {
  return (
    <S.Container>
      <Headline3>{title}</Headline3>
      <div>
        {descriptions.map((description, index) => (
          <S.DescriptionBox key={index}>
            <Description color="gray-400">{description}</Description>
          </S.DescriptionBox>
        ))}
      </div>
      <S.NextStepIcon src={'/images/next-step-icon.svg'} alt="next-step-icon" />
    </S.Container>
  );
};

export default RoomActionButton;
