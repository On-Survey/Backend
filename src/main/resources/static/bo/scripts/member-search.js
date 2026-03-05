document.addEventListener('DOMContentLoaded', function() {
    const inputs = ['searchEmail', 'searchPhone', 'searchMemberId', 'searchName'];
    inputs.forEach(id => {
        document.getElementById(id).addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchMembers();
            }
        });
    });
});

async function searchMembers() {
    const email = document.getElementById('searchEmail').value.trim();
    const phone = document.getElementById('searchPhone').value.trim();
    const memberId = document.getElementById('searchMemberId').value.trim();
    const name = document.getElementById('searchName').value.trim();
    if (!email && !phone && !memberId && !name) {
        showToast('검색 조건을 하나 이상 입력해주세요.', 'warning');
        return;
    }
    const params = new URLSearchParams();
    if (email) params.append('email', email);
    if (phone) params.append('phoneNumber', phone);
    if (memberId) params.append('memberId', memberId);
    if (name) params.append('name', name);
    try {
        const response = await apiCall('/admin/search?' + params.toString());
        if (response) renderMemberSearchResults(response.members || response);
    } catch (error) {
        console.error('Failed to search members:', error);
    }
}

function renderMemberSearchResults(members) {
    const tbody = document.getElementById('memberSearchTableBody');
    if (!members || members.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="px-6 py-8 text-center text-slate-500">검색 결과가 없습니다.</td></tr>';
        return;
    }
    tbody.innerHTML = members.map(member =>
        '<tr class="hover:bg-slate-50">' +
        '<td class="px-6 py-4 text-slate-600">' + member.id + '</td>' +
        '<td class="px-6 py-4 text-slate-600">' + (member.userKey || '-') + '</td>' +
        '<td class="px-6 py-4 font-medium text-slate-800">' + (member.name || '-') + '</td>' +
        '<td class="px-6 py-4 text-slate-600">' + (member.email || '-') + '</td>' +
        '<td class="px-6 py-4 text-slate-600">' + (member.phoneNumber || '-') + '</td>' +
        '<td class="px-6 py-4 text-slate-600">' + (member.birthDay || '-') + '</td>' +
        '<td class="px-6 py-4 text-slate-600">' + (member.gender || '-') + '</td>' +
        '<td class="px-6 py-4"><span class="px-2 py-0.5 rounded text-xs font-bold ' + getStatusColor(member.status) + '">' + (member.status || '-') + '</span></td>' +
        '<td class="px-6 py-4 text-slate-600">' + (member.coin ? member.coin.toLocaleString() : 0) + '</td>' +
        '</tr>'
    ).join('');
}

function getStatusColor(status) {
    const statusColors = {
        'ACTIVE': 'bg-green-100 text-green-700',
        'INACTIVE': 'bg-slate-100 text-slate-600',
        'SUSPENDED': 'bg-red-100 text-red-700',
        'TOSS_CONNECT_OUT': 'bg-orange-100 text-orange-700'
    };
    return statusColors[status] || 'bg-slate-100 text-slate-600';
}

function clearMemberSearch() {
    document.getElementById('searchEmail').value = '';
    document.getElementById('searchPhone').value = '';
    document.getElementById('searchMemberId').value = '';
    document.getElementById('searchName').value = '';
    document.getElementById('memberSearchTableBody').innerHTML = '';
}

// 어드민 등록
async function registerAdmin() {
    const userKey = document.getElementById('adminUserKey').value.trim();
    const username = document.getElementById('adminUsername').value.trim();
    const password = document.getElementById('adminPassword').value.trim();
    const name = document.getElementById('adminName').value.trim();

    if (!userKey || !username || !password || !name) {
        showToast('모든 필드를 입력해주세요.', 'warning');
        return;
    }

    if (!confirm(`어드민 계정을 등록하시겠습니까?\n\n아이디: ${username}\n이름: ${name}`)) {
        return;
    }

    try {
        const response = await fetch('/v1/bo/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({
                userKey: parseInt(userKey),
                username: username,
                password: password,
                name: name
            })
        });

        const result = await response.json();

        if (response.ok) {
            showToast(`어드민 계정이 등록되었습니다: ${username}`, 'success');
            clearAdminForm();
        } else {
            showToast(result.message || '어드민 등록에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('Admin register error:', error);
        showToast('오류가 발생했습니다: ' + error.message, 'error');
    }
}

function clearAdminForm() {
    document.getElementById('adminUserKey').value = '';
    document.getElementById('adminUsername').value = '';
    document.getElementById('adminPassword').value = '';
    document.getElementById('adminName').value = '';
}