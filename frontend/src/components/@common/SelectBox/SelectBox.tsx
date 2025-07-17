import { ComponentProps, KeyboardEvent, useEffect, useRef, useState } from 'react';
import * as S from './SelectBox.styled';

export type OptionType = {
  value: string;
  label: string;
  disabled?: boolean;
};

export type Props = Omit<ComponentProps<'div'>, 'onChange'> & {
  options: OptionType[];
  value?: string;
  placeholder?: string;
  disabled?: boolean;
  width?: string;
  height?: string;
  onChange?: (value: string) => void;
  onFocus?: () => void;
  onBlur?: () => void;
};

const SelectBox = ({
  options,
  value,
  placeholder = '선택하세요',
  disabled = false,
  width = '200px',
  height = '32px',
  onChange,
  onFocus,
  onBlur,
  ...restProps
}: Props) => {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<HTMLDivElement>(null);

  const selectedOption = options.find((option) => option.value === value);

  const handleOptionClick = (optionValue: string) => {
    if (disabled) return;

    onChange?.(optionValue);
    setIsOpen(false);
    triggerRef.current?.focus();
  };

  const handleTriggerClick = () => {
    if (disabled) return;

    setIsOpen(!isOpen);
    if (!isOpen) {
      onFocus?.();
    }
  };

  const handleKeyDown = (e: KeyboardEvent) => {
    if (disabled) return;

    switch (e.key) {
      case 'Enter':
      case ' ': {
        e.preventDefault();
        setIsOpen(!isOpen);
        break;
      }
      case 'Escape': {
        setIsOpen(false);
        triggerRef.current?.focus();
        break;
      }
      case 'ArrowDown': {
        e.preventDefault();
        if (!isOpen) {
          setIsOpen(true);
        }
        break;
      }
      case 'ArrowUp': {
        e.preventDefault();
        break;
      }
    }
  };

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
        onBlur?.();
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [onBlur]);

  return (
    <S.Container ref={containerRef} width={width} height={height} {...restProps}>
      <S.Trigger
        ref={triggerRef}
        disabled={disabled}
        isOpen={isOpen}
        onClick={handleTriggerClick}
        onKeyDown={handleKeyDown}
        tabIndex={disabled ? -1 : 0}
        role="combobox"
        aria-expanded={isOpen}
        aria-haspopup="listbox"
        aria-label={selectedOption ? selectedOption.label : placeholder}
      >
        <S.SelectText hasValue={!!selectedOption} disabled={disabled}>
          {selectedOption ? selectedOption.label : placeholder}
        </S.SelectText>
        <S.ArrowIcon isOpen={isOpen} disabled={disabled} />
      </S.Trigger>

      <S.Content isOpen={isOpen} role="listbox">
        {options.map((option) => (
          <S.Item
            key={option.value}
            disabled={option.disabled}
            selected={option.value === value}
            onClick={() => !option.disabled && handleOptionClick(option.value)}
            role="option"
            aria-selected={option.value === value}
          >
            {option.label}
          </S.Item>
        ))}
      </S.Content>
    </S.Container>
  );
};

export default SelectBox;
