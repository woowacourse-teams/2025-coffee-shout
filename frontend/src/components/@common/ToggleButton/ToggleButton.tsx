import Headline4 from '../Headline4/Headline4';
import * as S from './ToggleButton.styled';

type Props = {
  options: string[];
  selectedOption: string;
  onSelectOption: (option: string) => void;
};

const ToggleButton = ({ options, selectedOption, onSelectOption }: Props) => {
  const selectedIndex = options.indexOf(selectedOption);

  return (
    <S.Container>
      <S.Track>
        {options.map((option, idx) => (
          <S.Option key={option} onClick={() => onSelectOption(option)}>
            <Headline4 color={selectedIndex === idx ? 'white' : 'gray-400'}>{option}</Headline4>
          </S.Option>
        ))}
        <S.Thumb index={selectedIndex} optionCount={options.length} />
      </S.Track>
    </S.Container>
  );
};

export default ToggleButton;
