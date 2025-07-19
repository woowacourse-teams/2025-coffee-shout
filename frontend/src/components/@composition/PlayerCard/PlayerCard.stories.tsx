import Headline4 from '@/components/@common/Headline4/Headline4';
import type { Meta, StoryObj } from '@storybook/react-webpack5';
import PlayerCard from './PlayerCard';

const meta = {
  title: 'Composition/PlayerCard',
  component: PlayerCard,
  tags: ['autodocs'],
} satisfies Meta<typeof PlayerCard>;

export default meta;

type Story = StoryObj<typeof PlayerCard>;

export const WithText: Story = {
  args: {
    name: '홍길동',
    iconColor: 'red',
    children: <Headline4>10%</Headline4>,
  },
};

export const WithIcon: Story = {
  args: {
    name: '김철수',
    iconColor: 'red',
    // TODO: 커피 아이콘 결정되면 이미지 대체할 부분 (juice 이미지는 임시용)
    children: <img src="/images/juice.svg" alt="juice" />,
  },
};

export const LongNameWithText: Story = {
  args: {
    name: '매우매우매우매우긴이름을가진플레이어',
    iconColor: 'red',
    children: <Headline4>15%</Headline4>,
  },
};

export const LongNameWithIcon: Story = {
  args: {
    name: '아주아주아주아주아주긴이름의사용자님',
    iconColor: 'red',
    // TODO: 커피 아이콘 결정되면 이미지 대체할 부분 (juice 이미지는 임시용)
    children: <img src="/images/juice.svg" alt="juice" />,
  },
};

export const MultipleCards: Story = {
  render: () => (
    <>
      {Array.from({ length: 6 }, (_, index) => (
        <PlayerCard key={index} name="이영희" iconColor="red">
          <Headline4>20점</Headline4>
        </PlayerCard>
      ))}
    </>
  ),
};

export const DifferentProfileIcons: Story = {
  // TODO: 색상별 이미지 추가 시 스토리 수정 필요 (아이콘 UI 테스트는 PlayerCard에서 진행함 - 아이콘 컴포넌트가 따로 없기 때문)
  render: () => (
    <>
      <PlayerCard name="빨간색" iconColor="red">
        <Headline4>25%</Headline4>
      </PlayerCard>
      <PlayerCard name="파란색" iconColor="red">
        <Headline4>30%</Headline4>
      </PlayerCard>
      <PlayerCard name="초록색" iconColor="red">
        <Headline4>15%</Headline4>
      </PlayerCard>
    </>
  ),
};
