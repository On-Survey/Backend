// 페이지네이션 상태
let currentPage = 0;
let totalPages = 0;

// 페이지 로드 시 데이터 불러오기
document.addEventListener('DOMContentLoaded', function() {
    loadFormRequests();
});

// 필터 초기화
function resetFilters() {
    document.getElementById('searchEmail').value = '';
    document.getElementById('filterStatus').value = '';
    document.getElementById('pageSize').value = '10';
    currentPage = 0;
    loadFormRequests();
}

// ========== 설문 변환 요청 관련 함수 ==========
async function loadFormRequests(page = 0) {
    try {
        const email = document.getElementById('searchEmail').value.trim();
        const status = document.getElementById('filterStatus').value;
        const size = document.getElementById('pageSize').value;

        currentPage = page;

        // URL 파라미터 구성
        const params = new URLSearchParams();
        params.append('page', page);
        params.append('size', size);
        if (email) params.append('email', email);
        if (status !== '') params.append('isRegistered', status);

        const response = await apiCallPaged(`/form-requests?${params.toString()}`);
        if (response) {
            renderFormRequests(response.result);
            renderPagination(response);
        }
    } catch (error) {
        console.error('Failed to load form requests:', error);
    }
}

// 날짜 포맷팅 함수
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    const date = new Date(dateTimeStr);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function renderFormRequests(requests) {
    const tbody = document.getElementById('formRequestTableBody');

    console.log(requests);

    if (!requests || requests.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" class="px-6 py-8 text-center text-slate-500">검색 조건에 맞는 설문 변환 요청이 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = requests.map(req => `
        <tr class="hover:bg-slate-50">
            <td class="px-6 py-4 text-slate-600">${req.id}</td>
            <td class="px-6 py-4">
                <a href="${req.formLink}" target="_blank" class="text-indigo-600 hover:underline truncate max-w-xs block" title="${req.formLink}">
                    ${req.formLink.length > 40 ? req.formLink.substring(0, 40) + '...' : req.formLink}
                </a>
            </td>
            <td class="px-6 py-4 text-slate-600">${req.requesterEmail || '-'}</td>
            <td class="px-6 py-4 text-slate-600">${req.questionCount || '-'}</td>
            <td class="px-6 py-4 text-slate-600">${req.targetResponseCount || '-'}</td>
            <td class="px-6 py-4 text-slate-600">${req.deadline || '-'}</td>
            <td class="px-6 py-4 text-slate-600">${req.price ? req.price.toLocaleString() + '원' : '-'}</td>
            <td class="px-6 py-4">
                ${req.isRegistered
                    ? `<span class="bg-green-100 text-green-700 px-2 py-0.5 rounded text-xs font-bold">등록완료</span>`
                    : `<span class="bg-orange-100 text-orange-700 px-2 py-0.5 rounded text-xs font-bold">미등록</span>`
                }
            </td>
            <td class="px-6 py-4 text-slate-600 text-xs">${formatDateTime(req.createdAt)}</td>
            <td class="px-6 py-4 text-center">
                ${req.isRegistered && req.registeredSurveyId
                    ? `<button onclick="openSurveyEditor(${req.registeredSurveyId}, ${req.id})" class="px-3 py-1 bg-indigo-600 text-white rounded text-xs font-bold hover:bg-indigo-700">
                        <i class="fas fa-eye mr-1"></i>설문 조회
                       </button>`
                    : `<button onclick="createSurveyFromRequest(${req.id}, '${req.formLink}')" class="px-3 py-1 bg-green-600 text-white rounded text-xs font-bold hover:bg-green-700">
                        <i class="fas fa-plus mr-1"></i>설문 생성
                       </button>`
                }
            </td>
        </tr>
    `).join('');
}

// 페이지네이션 렌더링
function renderPagination(pageData) {
    const { pageNumber, totalPages, totalElements, pageSize, last } = pageData;

    // hasNext, hasPrevious 계산
    const hasPrevious = pageNumber > 0;
    const hasNext = !last;

    // 페이지 정보 표시
    const startItem = totalElements > 0 ? pageNumber * pageSize + 1 : 0;
    const endItem = Math.min((pageNumber + 1) * pageSize, totalElements);
    document.getElementById('pageInfo').innerHTML =
        `총 <strong>${totalElements}</strong>건 중 <strong>${startItem}-${endItem}</strong>건 표시`;

    // 페이지네이션 버튼
    const pagination = document.getElementById('pagination');
    let html = '';

    // 이전 버튼
    html += `<button onclick="loadFormRequests(${pageNumber - 1})"
                     class="px-3 py-1 rounded text-sm ${hasPrevious ? 'bg-slate-200 hover:bg-slate-300' : 'bg-slate-100 text-slate-400 cursor-not-allowed'}"
                     ${!hasPrevious ? 'disabled' : ''}>
                <i class="fas fa-chevron-left"></i>
             </button>`;

    // 페이지 번호들
    const maxVisiblePages = 5;
    let startPage = Math.max(0, pageNumber - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages);

    if (endPage - startPage < maxVisiblePages) {
        startPage = Math.max(0, endPage - maxVisiblePages);
    }

    if (startPage > 0) {
        html += `<button onclick="loadFormRequests(0)" class="px-3 py-1 rounded text-sm bg-slate-200 hover:bg-slate-300">1</button>`;
        if (startPage > 1) {
            html += `<span class="px-2 text-slate-400">...</span>`;
        }
    }

    for (let i = startPage; i < endPage; i++) {
        html += `<button onclick="loadFormRequests(${i})"
                         class="px-3 py-1 rounded text-sm ${i === pageNumber ? 'bg-indigo-600 text-white' : 'bg-slate-200 hover:bg-slate-300'}">
                    ${i + 1}
                 </button>`;
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
            html += `<span class="px-2 text-slate-400">...</span>`;
        }
        html += `<button onclick="loadFormRequests(${totalPages - 1})" class="px-3 py-1 rounded text-sm bg-slate-200 hover:bg-slate-300">${totalPages}</button>`;
    }

    // 다음 버튼
    html += `<button onclick="loadFormRequests(${pageNumber + 1})"
                     class="px-3 py-1 rounded text-sm ${hasNext ? 'bg-slate-200 hover:bg-slate-300' : 'bg-slate-100 text-slate-400 cursor-not-allowed'}"
                     ${!hasNext ? 'disabled' : ''}>
                <i class="fas fa-chevron-right"></i>
             </button>`;

    pagination.innerHTML = html;
}

// 설문 생성 - 모달 열기
function createSurveyFromRequest(requestId, formLink) {
    document.getElementById('modalFormRequestId').value = requestId;
    document.getElementById('modalFormLink').href = formLink;
    document.getElementById('modalFormLink').innerText = formLink;
    document.getElementById('modalSurveyTitle').value = '';
    document.getElementById('modalSurveyDescription').value = '';
    document.getElementById('surveyCreateModal').classList.remove('hidden');
}

// 모달 닫기
function closeSurveyModal() {
    document.getElementById('surveyCreateModal').classList.add('hidden');
}

// 설문 생성 및 폼 등록 처리
async function submitCreateSurvey() {
    const title = document.getElementById('modalSurveyTitle').value.trim();
    const description = document.getElementById('modalSurveyDescription').value.trim();
    const formRequestId = document.getElementById('modalFormRequestId').value;

    if (!title) {
        showToast('설문 제목을 입력해주세요.', 'error');
        document.getElementById('modalSurveyTitle').focus();
        return;
    }

    try {
        // 1. 설문 생성
        const surveyResponse = await apiCall('/survey-form/surveys', 'POST', {
            title: title,
            description: description
        });

        if (!surveyResponse || !surveyResponse.surveyId) {
            showToast('설문 생성에 실패했습니다.', 'error');
            return;
        }

        const surveyId = surveyResponse.surveyId;
        showToast(`설문이 생성되었습니다. (ID: ${surveyId})`, 'success');

        // 2. 폼 요청 등록 처리
        await apiCall(`/form-requests/${formRequestId}/register?surveyId=${surveyId}`, 'POST');
        showToast('폼이 설문에 등록되었습니다.', 'success');

        // 3. 모달 닫기 및 목록 새로고침
        closeSurveyModal();
        loadFormRequests();

        // 4. 설문 편집 페이지로 이동 (선택사항)
        if (confirm('생성된 설문의 문항을 편집하시겠습니까?')) {
            const params = new URLSearchParams({
                surveyId: surveyId,
                formRequestId: formRequestId,
                title: title,
                description: description
            });
            navigateToSurvey(params.toString());
        }
    } catch (error) {
        console.error('설문 생성/등록 실패:', error);
    }
}

// 설문 조회/수정 - 기존 설문 빌더에서 해당 설문 열기 (formRequestId 전달)
function openSurveyEditor(surveyId, formRequestId) {
    const params = new URLSearchParams({
        surveyId: surveyId,
        formRequestId: formRequestId
    });
    navigateToSurvey(params.toString());
}

// 부모 iframe에서 설문 페이지로 이동
function navigateToSurvey(queryString) {
    const surveyUrl = `/v1/bo/survey?${queryString}`;

    // 부모 창(index.html)에 메시지를 보내서 iframe src 변경 요청
    if (window.parent && window.parent !== window) {
        window.parent.postMessage({
            type: 'navigate',
            url: surveyUrl,
            from: 'form-request'
        }, '*');
    } else {
        // iframe이 아닌 경우 직접 이동
        window.location.href = surveyUrl;
    }
}
