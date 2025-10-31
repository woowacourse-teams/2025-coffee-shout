import styled from '@emotion/styled';

const FilterBar = styled.div`
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  background: #f8f9fa;
  flex-wrap: wrap;
`;

const FilterGroup = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
`;

const FilterLabel = styled.span`
  font-size: 12px;
  color: #666;
  font-weight: 500;
  margin-right: 4px;
`;

const FilterButton = styled.button<{ active: boolean }>`
  appearance: none;
  border: 1px solid rgba(0, 0, 0, 0.12);
  background: ${({ active }) => (active ? '#1a73e8' : '#ffffff')};
  color: ${({ active }) => (active ? '#ffffff' : '#222')};
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.15s ease;

  &:hover {
    background: ${({ active }) => (active ? '#1557b0' : '#f0f0f0')};
  }
`;

type Props = {
  contexts: string[];
  selectedContext: string | null;
  selectedType: 'fetch' | 'websocket' | null;
  onContextChange: (context: string | null) => void;
  onTypeChange: (type: 'fetch' | 'websocket' | null) => void;
};

const NetworkFilterBar = ({
  contexts,
  selectedContext,
  selectedType,
  onContextChange,
  onTypeChange,
}: Props) => {
  return (
    <FilterBar>
      <FilterGroup>
        <FilterLabel>Context:</FilterLabel>
        <FilterButton
          type="button"
          active={selectedContext === null}
          onClick={() => onContextChange(null)}
        >
          All
        </FilterButton>
        {contexts.map((context) => (
          <FilterButton
            key={context}
            type="button"
            active={selectedContext === context}
            onClick={() => onContextChange(context)}
          >
            {context === 'MAIN' ? 'Main' : context}
          </FilterButton>
        ))}
      </FilterGroup>

      <FilterGroup>
        <FilterLabel>Type:</FilterLabel>
        <FilterButton
          type="button"
          active={selectedType === null}
          onClick={() => onTypeChange(null)}
        >
          All
        </FilterButton>
        <FilterButton
          type="button"
          active={selectedType === 'fetch'}
          onClick={() => onTypeChange(selectedType === 'fetch' ? null : 'fetch')}
        >
          Fetch
        </FilterButton>
        <FilterButton
          type="button"
          active={selectedType === 'websocket'}
          onClick={() => onTypeChange(selectedType === 'websocket' ? null : 'websocket')}
        >
          WebSocket
        </FilterButton>
      </FilterGroup>
    </FilterBar>
  );
};

export default NetworkFilterBar;
