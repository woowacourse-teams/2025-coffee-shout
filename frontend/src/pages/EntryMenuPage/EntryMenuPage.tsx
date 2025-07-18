const EntryMenuPage = () => {
  return (
    <div>
      <h1>메뉴 설정</h1>
      <p>게임에 사용할 메뉴를 선택해주세요.</p>
      <div>
        <label>
          <input type="checkbox" />
          아메리카노
        </label>
        <label>
          <input type="checkbox" />
          카페라떼
        </label>
        <label>
          <input type="checkbox" />
          카푸치노
        </label>
      </div>
      <button>완료</button>
    </div>
  );
};

export default EntryMenuPage;
