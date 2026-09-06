import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';

export function NotFoundPage() {
  return (
    <EmptyState
      title="아직 만들지 않은 화면입니다"
      description="Phase 3b 에서 채웁니다. 지금은 디자인 갤러리만 볼 수 있습니다."
      action={
        <Button asChild variant="primary">
          <Link to="/design">디자인 갤러리로</Link>
        </Button>
      }
    />
  );
}
