### 사다드림 기능 요구사항 마인드맵

사다드림 기능에 대한 마인드맵입니다.

```plantuml
@startmindmap
<style>
mindmapDiagram {
  .green {
    BackgroundColor lightgreen
  }
  .red {
    BackgroundColor #FFBBCC
  }
}
</style>

+ 기능 요구사항
++ 배송 <<green>>
+++ 배송 확인

++ 결제 <<green>>
++ 사용자 관리 <<green>>
++ 요청 물품 등록 <<green>>

++ 주문(요청) <<green>>
+++ 요청 승락
+++ 요청 취소

++ 관리자 페이지 <<green>>
+++ 사용자 관리
++++ 회원 정보 관리
++++ 여행자 관리
+++++ 여행 관리
+++ 상품 등록 및 관리
++++ 주문/반품/취소 관리
+++ 게시물 및 댓글 관리
+++ 문의 사항 답글


-- 회원 관리 <<red>>
--- 회원 가입
--- 로그인
---- 소셜 로그인
--- 여행자 등록
--- 회원 탈퇴
--- 로그아웃

-- 상품 관리 <<red>>
--- 상품 등록
--- 상품 수정
--- 상품 삭제
--- 장바구니

-- 여행 <<red>>
--- 여행 등록
--- 여행 수정
--- 여행 삭제

-- 메시지 <<red>>
--- 채팅
--- 메시지

-- 알림 <<red>>

@endmindmap
```