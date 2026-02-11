/**
 * Toast Module
 * 토스트 메시지 표시 기능
 */

/**
 * 아직 준비 중 토스트 표시
 */
function showComingSoon(featureName) {
    showToast(`'${featureName}' 기능은 아직 준비 중입니다.`, 'info');
}

/**
 * 토스트 메시지 표시
 * @param {string} message - 표시할 메시지
 * @param {string} type - 토스트 타입 (info, success, error, warning)
 */
function showToast(message, type = 'info') {
    // 기존 토스트 제거
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
