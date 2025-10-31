/**
 * API调用封装
 */
const API_BASE_URL = window.location.origin + '/api';

const API = {
    // 贷款相关
    loans: {
        getAll: () => fetch(`${API_BASE_URL}/loans`).then(r => r.json()),
        getActive: () => fetch(`${API_BASE_URL}/loans/active`).then(r => r.json()),
        getById: (id) => fetch(`${API_BASE_URL}/loans/${id}`).then(r => r.json()),
        add: (data) => fetch(`${API_BASE_URL}/loans`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        }).then(r => r.json()),
        update: (id, data) => fetch(`${API_BASE_URL}/loans/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        }).then(r => r.json()),
        delete: (id) => fetch(`${API_BASE_URL}/loans/${id}`, {method: 'DELETE'}),
        recordPayment: (id, data) => fetch(`${API_BASE_URL}/loans/${id}/payment`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        }).then(r => r.json()),
        getSummary: () => fetch(`${API_BASE_URL}/loans/summary`).then(r => r.json())
    },

    // 收入相关
    income: {
        getAll: () => fetch(`${API_BASE_URL}/income`).then(r => r.json()),
        add: (data) => fetch(`${API_BASE_URL}/income`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        }).then(r => r.json()),
        update: (id, data) => fetch(`${API_BASE_URL}/income/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        }).then(r => r.json()),
        delete: (id) => fetch(`${API_BASE_URL}/income/${id}`, {method: 'DELETE'}),
        getMonthlyTotal: () => fetch(`${API_BASE_URL}/income/monthly-total`).then(r => r.json())
    },

    // 支出相关
    expenses: {
        getAll: () => fetch(`${API_BASE_URL}/expenses`).then(r => r.json()),
        add: (data) => fetch(`${API_BASE_URL}/expenses`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        }).then(r => r.json()),
        update: (id, data) => fetch(`${API_BASE_URL}/expenses/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        }).then(r => r.json()),
        delete: (id) => fetch(`${API_BASE_URL}/expenses/${id}`, {method: 'DELETE'}),
        getMonthlyTotal: () => fetch(`${API_BASE_URL}/expenses/monthly-total`).then(r => r.json())
    },

    // 余额相关
    balance: {
        getCurrent: () => fetch(`${API_BASE_URL}/balance/current`).then(r => r.json()),
        update: (data) => fetch(`${API_BASE_URL}/balance/update`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        }).then(r => r.json()),
        getHistory: () => fetch(`${API_BASE_URL}/balance/history`).then(r => r.json())
    },

    // 首页相关
    dashboard: {
        getData: () => fetch(`${API_BASE_URL}/dashboard`).then(r => r.json()),
        getOverview: () => fetch(`${API_BASE_URL}/dashboard/overview`).then(r => r.json())
    },

    // 预测相关
    forecast: {
        getCashFlow: (months = 12) => fetch(`${API_BASE_URL}/forecast?months=${months}`).then(r => r.json()),
        getDeficit: (months = 12) => fetch(`${API_BASE_URL}/forecast/deficit?months=${months}`).then(r => r.json()),
        getTimeline: (months = 12) => fetch(`${API_BASE_URL}/forecast/timeline?months=${months}`).then(r => r.json())
    }
};

// 工具函数
const Utils = {
    formatMoney: (amount) => {
        return Number(amount).toLocaleString('zh-CN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    },
    
    formatDate: (date) => {
        if (!date) return '';
        return new Date(date).toLocaleDateString('zh-CN');
    },
    
    showLoading: () => {
        document.body.style.cursor = 'wait';
    },
    
    hideLoading: () => {
        document.body.style.cursor = 'default';
    },
    
    showError: (message) => {
        alert('错误：' + message);
    },
    
    showSuccess: (message) => {
        alert('成功：' + message);
    }
};

