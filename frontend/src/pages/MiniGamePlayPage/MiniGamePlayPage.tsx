const MiniGamePlayPage = () => {
  return (
    <div>
      <h1>미니게임 진행</h1>
      <p>미니게임이 진행되는 화면입니다.</p>
      <div style={{ padding: '20px', backgroundColor: '#f0f0f0', borderRadius: '8px' }}>
        <h3>게임 진행 중...</h3>
        <p>시간: 30초</p>
        <p>점수: 100점</p>
      </div>
      <button>게임 종료</button>
    </div>
  );
};

export default MiniGamePlayPage;
