import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { useReplaceNavigate } from './useReplaceNavigate';

export const useNavigationGuard = () => {
  const location = useLocation();
  const navigate = useReplaceNavigate();

  useEffect(() => {
    if (!location.state?.fromInternal) {
      console.log('직접 URL 접근 감지 - 홈으로 리디렉션');
      navigate('/');
    }
  }, [location, navigate]);
};
