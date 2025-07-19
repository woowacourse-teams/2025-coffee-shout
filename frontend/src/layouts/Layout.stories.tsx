import { ReactNode } from 'react';
import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Layout from './Layout';
import Button from '@/components/@common/Button/Button';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Description from '@/components/@common/Description/Description';
import BackButton from '@/components/@common/BackButton/BackButton';
import { ColorKey } from '@/constants/color';

type LayoutStoryArgs = {
  showTopBar: boolean;
  showBanner: boolean;
  bannerHeight: string;
  buttonCount: 0 | 1 | 2;
  buttonRatio: 'default' | '4:1';
  color: ColorKey;
  content: ReactNode;
};

const meta: Meta<LayoutStoryArgs> = {
  title: 'Layouts/Layout',
  component: Layout,
  tags: ['autodocs'],
  decorators: [
    (Story) => (
      <div
        style={{
          width: 375,
          height: 800,
          margin: '0 auto',
          border: '1px solid #eee',
          overflow: 'auto',
          background: '#fafafa',
        }}
      >
        <Story />
      </div>
    ),
  ],
  argTypes: {
    showTopBar: { control: 'boolean', name: 'TopBar 표시' },
    showBanner: { control: 'boolean', name: 'Banner 표시' },
    bannerHeight: { control: 'text', name: 'Banner Height' },
    buttonCount: { control: { type: 'radio' }, options: [0, 1, 2], name: 'ButtonBar 버튼 개수' },
    buttonRatio: {
      control: { type: 'radio' },
      options: ['default', '4:1'],
      name: '버튼 비율',
      if: { arg: 'buttonCount', eq: 2 },
    },
    color: { control: 'text', name: '배경색(ColorKey)' },
    content: { control: 'text', name: 'Content 텍스트' },
  },
  args: {
    showTopBar: true,
    showBanner: false,
    bannerHeight: '6rem',
    buttonCount: 1,
    buttonRatio: 'default',
    color: 'white',
    content: '메인 컨텐츠 영역',
  },
};

export default meta;
type Story = StoryObj<LayoutStoryArgs>;

const getButtonWidths = (buttonCount: number, ratio: string): number[] => {
  if (buttonCount === 1) return [1];

  switch (ratio) {
    case '4:1':
      return [4, 1];
    default:
      return [1, 1];
  }
};

const Template = (args: LayoutStoryArgs) => {
  const { showTopBar, showBanner, bannerHeight, buttonCount, buttonRatio, color, content } = args;
        const flexRatios = getButtonWidths(buttonCount, buttonRatio);

  return (
    <Layout color={color}>
      {showTopBar && <Layout.TopBar backButton={<BackButton onClick={() => {}} />} />}
      {showBanner && <Layout.Banner height={bannerHeight}>배너 영역</Layout.Banner>}
      <Layout.Content>{content}</Layout.Content>
      {buttonCount === 1 && (
        <Layout.ButtonBar flexRatios={flexRatios}>
          <Button variant="primary">버튼1</Button>
        </Layout.ButtonBar>
      )}
      {buttonCount === 2 && (
        <Layout.ButtonBar flexRatios={flexRatios}>
          <Button variant="primary">버튼1</Button>
          <Button variant="primary">버튼2</Button>
        </Layout.ButtonBar>
      )}
    </Layout>
  );
};

export const TopBarAndButton: Story = {
  render: Template,
  args: {
    showTopBar: true,
    showBanner: false,
    buttonCount: 1,
    buttonRatio: 'default',
    color: 'white',
    content: (
      <>
        <Headline2>타이틀 예시</Headline2>
        <Description>정보 예시</Description>
      </>
    ),
  },
  parameters: {
    controls: {
      exclude: ['showBanner', 'bannerHeight'],
    },
  },
};

export const BannerAndButton: Story = {
  render: Template,
  args: {
    showTopBar: false,
    showBanner: true,
    bannerHeight: '40%',
    buttonCount: 1,
    buttonRatio: 'default',
    color: 'white',
    content: '메인 컨텐츠 영역',
  },
};

export const ContentOnlyPointColor: Story = {
  render: Template,
  args: {
    showTopBar: false,
    showBanner: false,
    buttonCount: 0,
    buttonRatio: 'default',
    color: 'point-400',
    content: '포인트 컬러 배경의 컨텐츠만 있는 레이아웃',
  },
};
