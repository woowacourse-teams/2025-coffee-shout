import { ReactNode } from 'react';
import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Layout from './Layout';
import Button from '@/components/@common/Button/Button';
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
    content: <div>컨텐츠</div>,
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
      {showTopBar && <Layout.TopBar left={<BackButton onClick={() => {}} />} />}
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
    content: <div>컨텐츠</div>,
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

export const ModalWithLayout: Story = {
  render: () => (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000,
      }}
    >
      <div
        style={{
          background: 'white',
          borderRadius: '12px',
          width: '90%',
          maxWidth: '400px',
          maxHeight: '80vh',
          overflow: 'hidden',
          boxShadow: '0 4px 20px rgba(0,0,0,0.3)',
        }}
      >
        <Layout color="white" padding="1rem">
          <Layout.TopBar
            center={<div style={{ fontSize: '1.2rem', fontWeight: 'bold' }}>음료 변경</div>}
            right={
              <button
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '1.5rem',
                  cursor: 'pointer',
                  padding: '0.25rem',
                }}
              >
                ×
              </button>
            }
            align={['center', 'center', 'start']}
          />
          <Layout.Content>
            <div style={{ padding: '1rem' }}>
              <p
                style={{
                  margin: '0 0 1rem 0',
                  color: '#666',
                  fontSize: '0.9rem',
                }}
              >
                변경할 메뉴를 선택해주세요
              </p>

              <div
                style={{
                  border: '1px solid #ddd',
                  borderRadius: '8px',
                  padding: '0.75rem',
                  marginBottom: '1rem',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  background: '#f9f9f9',
                }}
              >
                <span>아이스 아메리카노</span>
                <span style={{ fontSize: '0.8rem' }}>▼</span>
              </div>
            </div>
          </Layout.Content>
          <Layout.ButtonBar flexRatios={[1, 1]} height="100%">
            <Button variant="secondary">취소</Button>
            <Button variant="primary" style={{ background: '#ff6b6b' }}>
              변경
            </Button>
          </Layout.ButtonBar>
        </Layout>
      </div>
    </div>
  ),
  parameters: {
    controls: {
      exclude: ['showBanner', 'bannerHeight', 'buttonCount', 'buttonRatio'],
    },
  },
};
