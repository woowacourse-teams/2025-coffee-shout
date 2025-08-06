import styled from '@emotion/styled';

export const Container = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 15px 0;
`;

export const Wrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
  min-width: 0;
`;

export const NameWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
  padding-right: 20px;
  flex: 1;
  min-width: 0;

  h4 {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
`;
export const CrownIcon = styled.img`
  width: 30px;
  height: 30px;
  margin-bottom: 8px;
`;
