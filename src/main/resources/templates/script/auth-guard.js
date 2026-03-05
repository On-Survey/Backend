(async function checkAuth() {
    try {
        // 백엔드에 "나 로그인 되어있니?" 라고 물어보는 가벼운 API
        // 쿠키가 없다면 서버는 401 Unauthorized를 리턴해야 함
        const res = await fetch('/v1/bo/admin/me', {
            method: 'GET',
            credentials: 'include'
        }
    );

        if (res.status === 401 || res.status === 403) {
            // 권한 없으면 로그인 페이지로 튕겨내기
            alert('로그인이 필요한 서비스입니다.');
            // 현재가 iframe 내부라면 부모 창을 이동시킴
            if (window.self !== window.top) {
                window.top.location.href = 'login.html';
            } else {
                window.location.href = 'login.html';
            }
        }
    } catch (e) {
        // 서버 에러 시에도 안전하게 로그인 페이지로
        window.location.href = 'login.html';
    }
})();