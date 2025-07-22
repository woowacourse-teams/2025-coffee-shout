import NextStepIcon from '@/assets/images/next-step-icon.svg';
import { ComponentProps } from 'react';
import Description from '../Description/Description';
import Headline3 from '../Headline3/Headline3';
import * as S from './RoomActionButton.styled';

type Props = {
  title: string;
  descriptions: string[];
  onClick: () => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const RoomActionButton = ({ title, descriptions, onClick, ...rest }: Props) => {
  return (
    <S.Container onClick={onClick} {...rest}>
      <Headline3>{title}</Headline3>
      <div>
        {descriptions.map((description, index) => (
          <S.DescriptionBox key={index}>
            <Description color="gray-400">{description}</Description>
          </S.DescriptionBox>
        ))}
      </div>
      <S.NextStepIcon src={NextStepIcon} alt="next-step-icon" />
    </S.Container>
  );
};

export default RoomActionButton;
