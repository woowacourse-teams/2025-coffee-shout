import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Layout from './Layout';
import Button from '../components/@common/Button/Button';
import Title from './contentLayouts/Title/Title';
import DescriptionArea from './contentLayouts/DescriptionArea/DescriptionArea';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Description from '@/components/@common/Description/Description';

const meta: Meta<any> = {
  title: 'Layouts/Layout',
  component: Layout,
  tags: ['autodocs'], // ✅ 이렇게 추가!
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
    hasBackIcon: { control: 'boolean', name: '뒤로가기 버튼' },
    showBanner: { control: 'boolean', name: 'Banner 표시' },
    bannerHeight: { control: 'text', name: 'Banner Height' },
    buttonCount: { control: { type: 'radio' }, options: [0, 1, 2], name: 'ButtonBar 버튼 개수' },
    color: { control: 'text', name: '배경색(ColorKey)' },
    content: { control: 'text', name: 'Content 텍스트' },
  },
  args: {
    showTopBar: true,
    hasBackIcon: true,
    showBanner: false,
    bannerHeight: '6rem',
    buttonCount: 1,
    color: 'white',
    contentText: '메인 컨텐츠 영역',
  },
};

export default meta;
type Story = StoryObj<any>;

const Template = ({
  showTopBar,
  hasBackIcon,
  showBanner,
  bannerHeight,
  buttonCount,
  color,
  content,
}: any) => (
  <Layout color={color}>
    {showTopBar && <Layout.Top hasBackIcon={hasBackIcon} />}
    {showBanner && <Layout.Banner height={bannerHeight}>배너 영역</Layout.Banner>}
    <Layout.Content>{content}</Layout.Content>
    {buttonCount > 0 && (
      <Layout.ButtonBar>
        {Array.from({ length: buttonCount }).map((_, i) => (
          <Button key={i} variant="primary">{`버튼${i + 1}`}</Button>
        ))}
      </Layout.ButtonBar>
    )}
  </Layout>
);

export const TopBarAndButton: Story = {
  render: Template,
  args: {
    showTopBar: true,
    hasBackIcon: true,
    showBanner: false,
    buttonCount: 1,
    color: 'white',
    content: (
      <>
        <Title>
          <Headline2>타이틀 예시</Headline2>
        </Title>
        <DescriptionArea>
          <Description>정보 예시</Description>
        </DescriptionArea>
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
    hasBackIcon: false,
    showBanner: true,
    bannerHeight: '40%',
    buttonCount: 1,
    color: 'white',
    content: '메인 컨텐츠 영역',
  },
};

export const ContentOnlyPointColor: Story = {
  render: Template,
  args: {
    showTopBar: false,
    hasBackIcon: false,
    showBanner: false,
    buttonCount: 0,
    color: 'point-400',
    content: '포인트 컬러 배경의 컨텐츠만 있는 레이아웃',
  },
};
