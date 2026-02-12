/**
 * Navigation Module
 * 페이지 네비게이션 및 히스토리 관리
 */

// 네비게이션 히스토리 스택 (뒤로가기 지원용)
let navigationHistory = [];
let currentPageId = 'dashboard';

// 페이지 타이틀 매핑
const PAGE_TITLES = {
    'dashboard': '데이터 모니터링',
    'builder': '설문 제작 및 관리',
    'review': '무료 설문 검수',
    'promotion': '리워드 및 프로모션 관리',
    'formRequest': '설문 변환 요청',
    'memberSearch': '사용자 검색'
};

/**
 * 사이드바 토글 함수
 */
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const toggleIcon = document.getElementById('toggle-icon');

    sidebar.classList.toggle('collapsed');

    // 아이콘 방향 변경
    if (sidebar.classList.contains('collapsed')) {
        toggleIcon.classList.remove('fa-angles-left');
        toggleIcon.classList.add('fa-angles-right');
    } else {
        toggleIcon.classList.remove('fa-angles-right');
        toggleIcon.classList.add('fa-angles-left');
    }
}

/**
 * 페이지 전환 함수
 */
function showPage(pageId, element, addToHistory = true) {
    // 히스토리에 현재 페이지 추가 (뒤로가기용)
    if (addToHistory && currentPageId !== pageId) {
        navigationHistory.push({
            pageId: currentPageId,
            iframeSrc: getCurrentIframeSrc(currentPageId)
        });

        // History API로 브라우저 히스토리에도 추가
        history.pushState({ pageId: pageId }, '', `#${pageId}`);
    }

    currentPageId = pageId;

    // 1. 모든 페이지 섹션 숨기기
    const sections = document.querySelectorAll('.page-section');
    sections.forEach(sec => sec.classList.add('hidden'));

    // 2. 선택된 페이지 보여주기
    const targetPage = document.getElementById('page-' + pageId);
    if (targetPage) {
        targetPage.classList.remove('hidden');
    }

    // 3. 헤더 타이틀 변경
    const pageTitleEl = document.getElementById('page-title');
    if (pageTitleEl) {
        pageTitleEl.innerText = PAGE_TITLES[pageId] || pageId;
    }

    // 4. 네비게이션 활성화 상태 변경 (사이드바 클릭 시에만 element 존재)
    if (element) {
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => item.classList.remove('active'));
        element.classList.add('active');
    } else {
        // element가 없으면 pageId로 찾아서 활성화
        updateNavActiveState(pageId);
    }

    // 5. 뒤로가기 버튼 업데이트
    updateBackButton();
}

/**
 * 현재 iframe의 src 가져오기
 */
function getCurrentIframeSrc(pageId) {
    const pageEl = document.getElementById('page-' + pageId);
    const iframe = pageEl?.querySelector('iframe');
    return iframe?.src || null;
}

/**
 * 네비게이션 활성화 상태 업데이트
 */
function updateNavActiveState(pageId) {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => item.classList.remove('active'));

    navItems.forEach(item => {
        const onclick = item.getAttribute('onclick');
        if (onclick && onclick.includes(`'${pageId}'`)) {
            item.classList.add('active');
        }
    });
}

/**
 * iframe 내부에서 다른 페이지로 이동 요청 처리
 */
function navigateIframe(pageId, iframeSrc) {
    // 히스토리에 현재 상태 저장
    navigationHistory.push({
        pageId: currentPageId,
        iframeSrc: getCurrentIframeSrc(currentPageId)
    });

    currentPageId = pageId;

    // History API로 브라우저 히스토리에도 추가
    history.pushState({ pageId: pageId, iframeSrc: iframeSrc }, '', `#${pageId}`);

    // 1. 모든 페이지 섹션 숨기기
    const sections = document.querySelectorAll('.page-section');
    sections.forEach(sec => sec.classList.add('hidden'));

    // 2. 선택된 페이지 보여주기 및 iframe src 변경
    const pageEl = document.getElementById('page-' + pageId);
    pageEl.classList.remove('hidden');

    const iframe = pageEl.querySelector('iframe');
    if (iframe && iframeSrc) {
        iframe.src = iframeSrc;
    }

    // 3. 헤더 타이틀 변경
    const pageTitleEl = document.getElementById('page-title');
    if (pageTitleEl) {
        pageTitleEl.innerText = PAGE_TITLES[pageId] || pageId;
    }

    // 4. 네비게이션 활성화 상태 변경
    updateNavActiveState(pageId);

    // 5. 뒤로가기 버튼 표시
    updateBackButton();
}

/**
 * 뒤로가기 버튼 표시/숨김
 */
function updateBackButton() {
    const backBtn = document.getElementById('back-btn');
    if (backBtn) {
        if (navigationHistory.length > 0) {
            backBtn.classList.remove('hidden');
        } else {
            backBtn.classList.add('hidden');
        }
    }
}

/**
 * 뒤로가기 함수
 */
function goBack() {
    if (navigationHistory.length > 0) {
        const prev = navigationHistory.pop();

        currentPageId = prev.pageId;

        // 1. 모든 페이지 섹션 숨기기
        const sections = document.querySelectorAll('.page-section');
        sections.forEach(sec => sec.classList.add('hidden'));

        // 2. 이전 페이지 보여주기
        const pageEl = document.getElementById('page-' + prev.pageId);
        pageEl.classList.remove('hidden');

        // iframe src 복원
        if (prev.iframeSrc) {
            const iframe = pageEl.querySelector('iframe');
            if (iframe) {
                iframe.src = prev.iframeSrc;
            }
        }

        // 3. 헤더 타이틀 변경
        const pageTitleEl = document.getElementById('page-title');
        if (pageTitleEl) {
            pageTitleEl.innerText = PAGE_TITLES[prev.pageId] || prev.pageId;
        }

        // 4. 네비게이션 활성화 상태 변경
        updateNavActiveState(prev.pageId);

        // 5. 뒤로가기 버튼 업데이트
        updateBackButton();

        return true;
    }
    return false;
}

// iframe에서 오는 메시지 처리
window.addEventListener('message', function(event) {
    // 보안: 같은 origin에서 온 메시지만 처리
    if (event.origin !== window.location.origin) return;

    const data = event.data;

    if (data && data.type === 'navigate') {
        // form-request에서 survey 페이지로 이동 요청
        if (data.from === 'form-request' && data.url) {
            navigateIframe('builder', data.url);
        }
    }

    if (data && data.type === 'goBack') {
        // iframe에서 뒤로가기 요청
        goBack();
    }
});

// 브라우저 뒤로가기/앞으로가기 버튼 처리
window.addEventListener('popstate', function(event) {
    if (event.state && event.state.pageId) {
        showPage(event.state.pageId, null, false);

        if (event.state.iframeSrc) {
            const pageEl = document.getElementById('page-' + event.state.pageId);
            const iframe = pageEl?.querySelector('iframe');
            if (iframe) {
                iframe.src = event.state.iframeSrc;
            }
        }
    }
});
