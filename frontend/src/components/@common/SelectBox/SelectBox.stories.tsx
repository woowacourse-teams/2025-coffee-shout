import type { Meta, StoryObj } from '@storybook/react-webpack5';
import { useState } from 'react';
import SelectBox, { Option } from './SelectBox';

const meta = {
  title: 'Common/SelectBox',
  component: SelectBox,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
  argTypes: {
    width: {
      control: 'text',
      description: 'SelectBox의 너비',
    },
    height: {
      control: 'text',
      description: 'SelectBox의 높이',
    },
    placeholder: {
      control: 'text',
      description: '선택되지 않았을 때 표시되는 텍스트',
    },
    disabled: {
      control: 'boolean',
      description: '비활성화 상태',
    },
    onChange: {
      action: 'changed',
      description: '옵션 선택 시 실행되는 함수',
    },
    onFocus: {
      action: 'focused',
      description: '포커스 시 실행되는 함수',
    },
    onBlur: {
      action: 'blurred',
      description: '포커스 해제 시 실행되는 함수',
    },
  },
} satisfies Meta<typeof SelectBox>;

export default meta;

type Story = StoryObj<typeof SelectBox>;

const basicOptions: Option[] = [
  { value: 'apple', label: '사과' },
  { value: 'banana', label: '바나나' },
  { value: 'orange', label: '오렌지' },
  { value: 'grape', label: '포도' },
];

export const Default: Story = {
  args: {
    options: basicOptions,
    placeholder: '과일을 선택하세요',
  },
};

export const WithValue: Story = {
  args: {
    options: basicOptions,
    value: 'banana',
    placeholder: '과일을 선택하세요',
  },
};

export const Disabled: Story = {
  args: {
    options: basicOptions,
    disabled: true,
    placeholder: '비활성화된 SelectBox',
  },
};

export const Interactive = () => {
  const [selectedValue, setSelectedValue] = useState<Option>({
    value: '',
    label: '',
  });

  const coffeeOptions: Option[] = [
    { value: 'americano', label: '아이스 아메리카노' },
    { value: 'latte', label: '카페 라떼' },
    { value: 'cappuccino', label: '카푸치노' },
    { value: 'macchiato', label: '마끼아또' },
    { value: 'mocha', label: '카페 모카' },
    { value: 'espresso', label: '에스프레소', disabled: true },
  ];

  return (
    <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <div>
        <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
          커피 선택
        </label>
        <SelectBox
          options={coffeeOptions}
          value={selectedValue.label}
          onChange={setSelectedValue}
          placeholder="커피를 선택하세요"
          width="300px"
          height="32px"
        />
      </div>
    </div>
  );
};
