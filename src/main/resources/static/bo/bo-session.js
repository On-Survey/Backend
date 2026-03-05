/**
 * 백오피스 세션 관리 모듈
 * - 401 에러 핸들링 (fetch interceptor)
 * - 로그아웃 처리
 */
(function() {
    'use strict';

    let isRedirecting = false;

    /**
     * 세션 만료 처리
     */
    function handleSessionExpired() {
        if (isRedirecting) return;
        isRedirecting = true;

        showToast('세션이 만료되었습니다. 다시 로그인해주세요.', 'warning');
        setTimeout(() => {
            window.location.href = '';
        }, 2000);
    }

    /**
     * 로그아웃 처리
     */
    async function logout() {
        if (!confirm('로그아웃 하시겠습니까?')) {
            return;
        }

        try {
            await fetch('/v1/bo/auth/logout', {
                method: 'POST',
                credentials: 'include'
            });
        } catch (error) {
            console.error('[Session] Logout error:', error);
        }

        window.location.href = '';
    }

    /**
     * 토스트 메시지 표시
     */
    function showToast(message, type) {
        if (typeof window.showToast === 'function') {
            window.showToast(message, type);
            return;
        }

        // 폴백: 기본 토스트 구현
        const existingToast = document.querySelector('.toast-message');
        if (existingToast) {
            existingToast.remove();
        }

        const toast = document.createElement('div');
        toast.className = 'toast-message fixed top-20 right-8 z-50 px-6 py-3 rounded-lg shadow-lg transition-all duration-300';

        const colors = {
            'info': 'bg-blue-500 text-white',
            'success': 'bg-green-500 text-white',
            'error': 'bg-red-500 text-white',
            'warning': 'bg-orange-500 text-white'
        };
        toast.className += ' ' + (colors[type] || colors['info']);
        toast.innerText = message;

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    /**
     * Fetch API 래퍼 - 401 에러 자동 처리
     */
    function setupFetchInterceptor() {
        const originalFetch = window.fetch;

        window.fetch = async function(...args) {
            const response = await originalFetch.apply(this, args);

            // 401 에러 시 세션 만료 처리 (로그인 요청 제외)
            const url = args[0]?.toString() || '';
            const isAuthEndpoint = url.includes('/auth/login');

            if (response.status === 401 && !isAuthEndpoint) {
                handleSessionExpired();
            }

            return response;
        };
    }

    /**
     * iframe 메시지 리스너 설정
     */
    function setupIframeMessageListener() {
        window.addEventListener('message', function(event) {
            if (event.origin !== window.location.origin) return;

            const data = event.data;

            if (data && data.type === 'sessionExpired') {
                handleSessionExpired();
            }
        });
    }

    /**
     * 초기화
     */
    function init() {
        setupFetchInterceptor();
        setupIframeMessageListener();

        // 전역 함수로 노출
        window.boSession = {
            logout: logout
        };
    }

    // DOM 로드 후 초기화
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
