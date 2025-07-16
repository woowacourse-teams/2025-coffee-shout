import { ComponentProps } from 'react';
import Headline3 from '../Headline3/Headline3';
import Description from '../Description/Description';
import * as S from './RoomActionButton.styled';

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
            <S.RoomDescription color="gray-300">{description}</S.RoomDescription>
          </S.DescriptionBox>
        ))}
      </div>
      <S.NextStepIcon src={'/images/next-step-icon.svg'} alt="next-step-icon" />
    </S.Container>
  );
};

export default RoomActionButton;
