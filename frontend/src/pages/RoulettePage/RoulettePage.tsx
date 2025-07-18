const RoulettePage = () => {
  return (
    <div>
      <h1>룰렛</h1>
      <p>룰렛을 돌려서 미니게임을 선택해보세요!</p>
      <div
        style={{
          width: '200px',
          height: '200px',
          border: '2px solid #ccc',
          borderRadius: '50%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <span>룰렛</span>
      </div>
      <button>룰렛 돌리기</button>
    </div>
  );
};

export default RoulettePage;
