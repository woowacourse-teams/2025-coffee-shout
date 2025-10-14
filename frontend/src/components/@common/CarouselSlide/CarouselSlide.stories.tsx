import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CarouselSlide from './CarouselSlide';
import RankingItem from '@/components/@common/RankingItem/RankingItem';
import ProbabilityTag from '../ProbabilityTag/ProbabilityTag';

const meta: Meta<typeof CarouselSlide> = {
  title: 'Common/CarouselSlide',
  component: CarouselSlide,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
  decorators: [
    (Story) => (
      <div
        style={{
          background: '#ff6b6b',
          borderRadius: '20px',
          width: '375px',
          height: '200px',
        }}
      >
        <Story />
      </div>
    ),
  ],
  argTypes: {
    title: {
      control: 'text',
      description: '슬라이드 제목',
    },
  },
  args: {
    title: '슬라이드 제목',
    children: (
      <div>
        <p style={{ color: 'white', marginBottom: '1rem' }}>슬라이드 내용입니다.</p>
        <p style={{ color: 'white' }}>여기에 다양한 컴포넌트를 넣을 수 있습니다.</p>
      </div>
    ),
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const WithRankingItems: Story = {
  args: {
    title: '이번달 TOP3 당첨자',
    children: (
      <div>
        <RankingItem rank={1} name="세라" count={20} />
        <RankingItem rank={2} name="민수" count={15} />
        <RankingItem rank={3} name="지영" count={12} />
      </div>
    ),
  },
};

export const WithTextContent: Story = {
  args: {
    title: '최저 확률 우승자',
    children: <ProbabilityTag probability={5} />,
  },
};
