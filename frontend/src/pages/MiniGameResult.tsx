const MiniGameResult = () => {
  return (
    <div>
      <h1>미니게임 결과</h1>
      <p>게임 결과를 확인하세요!</p>
      <div style={{ padding: '20px', backgroundColor: '#e8f5e8', borderRadius: '8px' }}>
        <h3>결과</h3>
        <p>최종 점수: 1500점</p>
        <p>순위: 1위</p>
      </div>
      <button>다음 게임</button>
      <button>로비로 돌아가기</button>
    </div>
  );
};

export default MiniGameResult;
