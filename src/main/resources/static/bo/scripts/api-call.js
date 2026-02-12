const API_BASE_URL = '/v1';

// API 호출 함수
async function apiCall(url, method = 'GET', data = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        credentials: 'include'
    };
    if (data && method !== 'GET') options.body = JSON.stringify(data);
    try {
        const response = await fetch(API_BASE_URL + url, options);
        if (response.status === 401) { showToast('로그인이 필요합니다.', 'error'); return null; }
        if (response.status === 403) { showToast('접근 권한이 없습니다.', 'error'); return null; }
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'API 호출 실패');
        return result.result;
    } catch (error) {
        console.error('API Error:', error);
        showToast('오류가 발생했습니다: ' + error.message, 'error');
        throw error;
    }
}

// 페이지네이션 API 호출 함수
async function apiCallPaged(url, method = 'GET') {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        credentials: 'include'
    };

    try {
        const response = await fetch(API_BASE_URL + url, options);

        if (!response.ok) {
            const result = await response.json();
            throw new Error(result.message || 'API 호출 실패');
        }

        return await response.json();
    } catch (error) {
        console.error('API Error:', error);
        showToast('오류가 발생했습니다: ' + error.message, 'error');
        throw error;
    }
}