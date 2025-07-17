import styled from '@emotion/styled';
import Headline4 from '../Headline4/Headline4';
import { useState } from 'react';

const OPTIONS = ['참가자', '룰렛', '미니게임'];

const ToggleButton = () => {
  const [selectedIndex, setSelectedIndex] = useState(0);

  return (
    <ToggleContainer>
      <Track>
        {OPTIONS.map((label, idx) => (
          <div
            key={label}
            onClick={() => setSelectedIndex(idx)}
            style={{
              cursor: 'pointer',
              zIndex: 1,
              flex: 1,
              textAlign: 'center',
            }}
          >
            <Headline4 color={selectedIndex === idx ? 'white' : 'gray-400'}>{label}</Headline4>
          </div>
        ))}
        <Thumb index={selectedIndex} />
      </Track>
    </ToggleContainer>
  );
};

export default ToggleButton;

const ToggleContainer = styled.button`
  width: 100%;
  height: 42px;
  background-color: ${({ theme }) => theme.color.gray[100]};
  cursor: pointer;
  border-radius: 20px;
  padding: 2px;
`;

const Track = styled.div`
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const Thumb = styled.div<{ index: number }>`
  width: 33.33%;
  height: 100%;
  border-radius: 20px;
  background-color: ${({ theme }) => theme.color.point[400]};
  position: absolute;
  top: 0;
  left: ${({ index }) => `${index * 33.33}%`};
  transition: left 0.2s;
  z-index: 0;
`;
