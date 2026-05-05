// 페이지네이션 상태
let currentPage = 0;

// 유효성 검사 상태
let validatedFormLink = null;
let validationData = null;
let isValidationInProgress = false;

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

// 연령대 전체 토글
function handleAgeAllToggle(checkbox) {
    const specifics = document.querySelectorAll('.new-age-specific');
    specifics.forEach(cb => { cb.checked = false; cb.disabled = checkbox.checked; });
}

// 거주지 전체 토글
function handleResidenceAllToggle(checkbox) {
    const specifics = document.querySelectorAll('.new-residence-specific');
    specifics.forEach(cb => { cb.checked = false; cb.disabled = checkbox.checked; });
}

// ========== 유효성 검사 관련 함수 ==========
function setValidationLoading(isLoading) {
    isValidationInProgress = isLoading;

    const button = document.getElementById('validationButton');
    const icon = document.getElementById('validationButtonIcon');
    const text = document.getElementById('validationButtonText');

    if (!button || !icon || !text) return;

    button.disabled = isLoading;
    button.classList.toggle('opacity-70', isLoading);
    button.classList.toggle('cursor-not-allowed', isLoading);

    if (isLoading) {
        icon.className = 'fas fa-spinner fa-spin mr-1';
        text.textContent = '검사 중...';
    } else {
        icon.className = 'fas fa-check-circle mr-1';
        text.textContent = '유효성 검사';
    }
}

function updateFormRequestRowAfterRegistration(formRequestId, surveyId) {
    const row = document.getElementById(`form-request-row-${formRequestId}`);
    if (!row) return false;

    const filterStatus = document.getElementById('filterStatus')?.value ?? '';

    // 현재 목록이 미등록 필터라면 등록 완료 후 행을 제거한다.
    if (filterStatus === 'false') {
        row.remove();
        return true;
    }

    const surveyCell = document.getElementById(`form-request-survey-${formRequestId}`);
    const statusCell = document.getElementById(`form-request-status-${formRequestId}`);
    const actionCell = document.getElementById(`form-request-action-${formRequestId}`);

    if (surveyCell) surveyCell.textContent = surveyId || '-';
    if (statusCell) {
        statusCell.innerHTML = '<span class="bg-green-100 text-green-700 px-2 py-0.5 rounded text-xs font-bold">등록완료</span>';
    }
    if (actionCell) {
        actionCell.innerHTML = `
            <button onclick="openSurveyEditor(${surveyId}, ${formRequestId})" class="px-3 py-1 bg-indigo-600 text-white rounded text-xs font-bold hover:bg-indigo-700">
                <i class="fas fa-eye mr-1"></i>설문 조회
            </button>
        `;
    }

    row.dataset.isRegistered = 'true';
    row.dataset.registeredSurveyId = surveyId || '';
    return true;
}

async function validateFormLink() {
    if (isValidationInProgress) return;

    const formLink = document.getElementById('newFormLink').value.trim();
    const requesterEmail = document.getElementById('newRequesterEmail').value.trim();

    if (!formLink) {
        showToast('폼 링크를 입력해주세요.', 'error');
        document.getElementById('newFormLink').focus();
        return;
    }
    try { new URL(formLink); } catch (e) {
        showToast('올바른 URL 형식을 입력해주세요.', 'error');
        document.getElementById('newFormLink').focus();
        return;
    }
    if (!requesterEmail) {
        showToast('신청자 이메일을 입력해주세요.', 'error');
        document.getElementById('newRequesterEmail').focus();
        return;
    }

    setValidationLoading(true);

    try {
        const response = await apiCall('/form-requests/validation', 'POST', {
            formLink,
            requesterEmail
        });

        if (response) {
            validatedFormLink = formLink;
            validationData = response;

            // 유효성 검사 결과 표시
            const resultContainer = document.getElementById('validationResultContainer');
            const resultText = document.getElementById('validationResultText');

            if (response.convertableCount !== undefined) {
                resultText.innerHTML = `
                    변환 가능한 문항: <strong>${response.convertableCount}</strong>개 / 
                    전체 문항: <strong>${response.totalCount || 'N/A'}</strong>개
                `;
            } else {
                resultText.textContent = '유효성 검사가 완료되었습니다.';
            }

            resultContainer.classList.remove('hidden');
            document.getElementById('validationFailureContainer').classList.add('hidden');
            showToast('유효성 검사가 완료되었습니다.', 'success');
        }
    } catch (error) {
        console.error('유효성 검사 실패:', error);
        validatedFormLink = null;
        validationData = null;
        document.getElementById('validationFailureText').textContent = '유효성 검사에 실패했습니다. 다시 시도해주세요.';
        document.getElementById('validationFailureContainer').classList.remove('hidden');
        showToast('유효성 검사에 실패했습니다.', 'error');
    } finally {
        setValidationLoading(false);
    }
}

// ========== 폼 변환 요청 제출 관련 함수 ==========
async function submitFormRequest() {
    const formLink = document.getElementById('newFormLink').value.trim();
    const requesterEmail = document.getElementById('newRequesterEmail').value.trim();

    // 유효성 검사 완료 확인
    if (!validatedFormLink) {
        showToast('먼저 유효성 검사를 수행해주세요.', 'error');
        return;
    }

    // 폼 링크 일치 확인
    if (validatedFormLink !== formLink) {
        showToast('유효성 검사된 폼 링크와 현재 폼 링크가 다릅니다. 다시 유효성 검사를 수행해주세요.', 'error');
        return;
    }

    // surveyForm (필수)
    const deadline = document.getElementById('newDeadline').value;
    const gender = document.getElementById('newGender').value;
    const dueCount = document.getElementById('newDueCount').value;
    const totalCoin = document.getElementById('newTotalCoin').value;

    // 필수 필드 검증 - 마감일
    if (!deadline) {
        showToast('마감일을 입력해주세요.', 'error');
        document.getElementById('newDeadline').focus();
        return;
    }

    // 필수 필드 검증 - 목표 응답 수
    if (!dueCount || parseInt(dueCount) < 1) {
        showToast('목표 응답 수를 1 이상 입력해주세요.', 'error');
        document.getElementById('newDueCount').focus();
        return;
    }

    // 필수 필드 검증 - 총 코인
    if (!totalCoin || parseInt(totalCoin) < 0) {
        showToast('총 코인을 0 이상 입력해주세요.', 'error');
        document.getElementById('newTotalCoin').focus();
        return;
    }

    // 연령대 검증
    const ageAllChecked = document.querySelector('.new-age-checkbox[value="ALL"]').checked;
    const ageSpecificsChecked = document.querySelectorAll('.new-age-specific:checked').length > 0;

    if (!ageAllChecked && !ageSpecificsChecked) {
        showToast('연령대를 최소 1개 이상 선택해주세요.', 'error');
        return;
    }

    const ages = ageAllChecked
        ? ['ALL']
        : Array.from(document.querySelectorAll('.new-age-specific:checked')).map(cb => cb.value);

    // 거주지 검증
    const residenceAllChecked = document.querySelector('.new-residence-checkbox[value="ALL"]').checked;
    const residenceSpecificsChecked = document.querySelectorAll('.new-residence-specific:checked').length > 0;

    if (!residenceAllChecked && !residenceSpecificsChecked) {
        showToast('거주지를 최소 1개 이상 선택해주세요.', 'error');
        return;
    }

    const residences = residenceAllChecked
        ? ['ALL']
        : Array.from(document.querySelectorAll('.new-residence-specific:checked')).map(cb => cb.value);

    // 관심사 검증 (필수, 최소 1개)
    const interests = Array.from(document.querySelectorAll('.new-interest-checkbox:checked')).map(cb => cb.value);
    if (interests.length === 0) {
        showToast('관심사를 최소 1개 이상 선택해주세요.', 'error');
        return;
    }

    // screening
    const screeningContent = document.getElementById('newScreeningContent').value.trim();
    const screeningAnswer = document.getElementById('newScreeningAnswer').value;
    const screening = screeningContent
        ? { content: screeningContent, answer: screeningAnswer === '' ? null : screeningAnswer === 'true' }
        : null;

    const requestData = {
        formLink,
        requesterEmail,
        surveyForm: {
            deadline: deadline ? deadline + 'T23:59:59' : null,
            gender,
            ages: ages.length > 0 ? ages : null,
            residences: residences.length > 0 ? residences : null,
            dueCount: dueCount ? parseInt(dueCount) : null,
            totalCoin: totalCoin ? parseInt(totalCoin) : null
        },
        screening,
        interests: interests.length > 0 ? interests : null
    };

    try {
        const response = await apiCall('/form-requests', 'POST', requestData);
        if (response !== undefined) {
            showToast('폼 변환 요청이 등록되었습니다.', 'success');
            clearFormRequestInputs();
            setTimeout(loadFormRequests, 5000);
        }
    } catch (error) {
        console.error('폼 변환 요청 실패:', error);
    }
}

// 폼 변환 요청 입력 필드 초기화
function clearFormRequestInputs() {
    document.getElementById('newFormLink').value = '';
    document.getElementById('newRequesterEmail').value = '';
    document.getElementById('newDeadline').value = '';
    document.getElementById('newGender').value = 'ALL';
    document.getElementById('newDueCount').value = '';
    document.getElementById('newTotalCoin').value = '';
    document.getElementById('newScreeningContent').value = '';
    document.getElementById('newScreeningAnswer').value = '';
    document.querySelectorAll('.new-age-checkbox, .new-residence-checkbox, .new-interest-checkbox')
        .forEach(cb => { cb.checked = false; cb.disabled = false; });

    // 유효성 검사 상태 초기화
    validatedFormLink = null;
    validationData = null;
    document.getElementById('validationResultContainer').classList.add('hidden');
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
        <tr id="form-request-row-${req.id}" data-request-id="${req.id}" class="hover:bg-slate-50">
            <td class="px-6 py-4 text-slate-600">${req.id}</td>
            <td class="px-6 py-4">
                <a href="${req.formLink}" target="_blank" class="text-indigo-600 hover:underline truncate max-w-xs block" title="${req.formLink}">
                    ${req.formLink.length > 40 ? req.formLink.substring(0, 40) + '...' : req.formLink}
                </a>
            </td>
            <td class="px-6 py-4 text-slate-600">${req.requesterEmail || '-'}</td>
            <td id="form-request-survey-${req.id}" class="px-6 py-4 text-slate-600">${req.registeredSurveyId || '-'}</td>
            <td class="px-6 py-4 text-slate-600">${req.questionCount || '-'}</td>
            <td class="px-6 py-4 text-slate-600">${req.targetResponseCount || '-'}</td>
            <td class="px-6 py-4 text-slate-600">${req.price ? req.price.toLocaleString() + '원' : '-'}</td>
            <td id="form-request-status-${req.id}" class="px-6 py-4">
                ${req.isRegistered
                    ? `<span class="bg-green-100 text-green-700 px-2 py-0.5 rounded text-xs font-bold">등록완료</span>`
                    : `<span class="bg-orange-100 text-orange-700 px-2 py-0.5 rounded text-xs font-bold">미등록</span>`
                }
            </td>
            <td class="px-6 py-4 text-slate-600 text-xs">${formatDateTime(req.createdAt)}</td>
            <td id="form-request-action-${req.id}" class="px-6 py-4 text-center">
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
        console.log(1);
        // 3. 테이블 행 즉시 갱신
        updateFormRequestRowAfterRegistration(formRequestId, surveyId);

        // 4. 모달 닫기
        closeSurveyModal();

        // 5. 설문 편집 페이지로 이동 (선택사항)
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
