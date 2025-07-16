import App from '../src/App';
import { render, screen } from '@testing-library/react';

describe('App', () => {
  test('renders without crashing', () => {
    render(<App />);
    // App 컴포넌트가 렌더링되는지 확인
    expect(screen.getByText('Hello, World!')).toBeInTheDocument();
  });
});
