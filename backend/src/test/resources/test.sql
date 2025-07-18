
-- 메뉴 테이블
CREATE TABLE IF NOT EXISTS menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    image VARCHAR(255)
    );

-- 룸 테이블 (@NaturalId + @Embedded 조합)
CREATE TABLE IF NOT EXISTS room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    join_code VARCHAR(255),  -- JoinCode 내부 value 필드
    room_state VARCHAR(50) DEFAULT 'READY'
    );

-- 테스트용 메뉴 데이터
INSERT INTO menu (name, image) VALUES
   ('아메리카노', 'americano.jpg'),
   ('카페라떼', 'latte.jpg'),
   ('카푸치노', 'cappuccino.jpg'),
   ('에스프레소', 'espresso.jpg'),
   ('프라푸치노', 'frappuccino.jpg');

