import { useNavigate, NavigateOptions } from 'react-router-dom';

export const useReplaceNavigate = () => {
  const navigate = useNavigate();

  return (to: string | number, options?: NavigateOptions) => {
    if (typeof to === 'number') {
      navigate(to);
      return;
    }

    navigate(to, { ...options, replace: true });
  };
};
