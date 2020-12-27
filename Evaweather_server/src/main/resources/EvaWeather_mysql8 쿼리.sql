-- 데이터베이스 생성
create database DB명 default CHARACTER set UTF8;

-- 계정 생성 및 DB 접속 권한 부여
create user '계정명'@'%' identified with mysql_native_password by '비밀번호';
grant all privileges on DB명.* to 계정명@'%' with grant option;
flush privileges;

-- 테이블 생성
-- 초단기 실황
create table UltraSrtNcsts (
    baseDate varchar(8) NOT NULL,
    baseTime varchar(4) NOT NULL,
    nx DECIMAL(3,0) NOT NULL,
    ny DECIMAL(3,0) NOT NULL,
    T1H DECIMAL(10,1),
    RN1 DECIMAL(10,1),
    UUU DECIMAL(10,1),
    VVV DECIMAL(10,1),
    REH DECIMAL(10,1),
    PTY DECIMAL(10,1),
    VEC DECIMAL(10,1),
    WSD DECIMAL(10,1),
    PRIMARY KEY(baseDate, baseTime, nx, ny)
);

-- 초단기 예보
create table UltraSrtFcsts (
    fcstDate varchar(8) NOT NULL,
    fcstTime varchar(4) NOT NULL,
    nx DECIMAL(3,0) NOT NULL,
    ny DECIMAL(3,0) NOT NULL,
    T1H DECIMAL(10,1),
    RN1 DECIMAL(10,1),
    SKY DECIMAL(10,1),
    UUU DECIMAL(10,1),
    VVV DECIMAL(10,1),
    REH DECIMAL(10,1),
    PTY DECIMAL(10,1),
    LGT DECIMAL(10,1),
    VEC DECIMAL(10,1),
    WSD DECIMAL(10,1),
    PRIMARY KEY(fcstDate, fcstTime, nx, ny)
);

-- 동네예보
create table VilageFcsts (
    fcstDate varchar(8) NOT NULL,
    fcstTime varchar(4) NOT NULL,
    nx DECIMAL(3,0) NOT NULL,
    ny DECIMAL(3,0) NOT NULL,
    POP DECIMAL(10,1),
    PTY DECIMAL(10,1),
    R06 DECIMAL(10,1),
    REH DECIMAL(10,1),
    S06 DECIMAL(10,1),
    SKY DECIMAL(10,1),
    T3H DECIMAL(10,1),
    TMN DECIMAL(10,1),
    TMX DECIMAL(10,1),
    UUU DECIMAL(10,1),
    VVV DECIMAL(10,1),
    WAV DECIMAL(10,1),
    VEC DECIMAL(10,1),
    WSD DECIMAL(10,1),
    PRIMARY KEY(fcstDate, fcstTime, nx, ny)
);