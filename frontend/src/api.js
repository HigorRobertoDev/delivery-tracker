const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080'

function getToken() {
  return localStorage.getItem('token')
}

async function request(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  }

  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  })

  let data = null
  const text = await response.text()
  if (text) {
    data = JSON.parse(text)
  }

  if (!response.ok) {
    throw new Error(data?.message || 'Erro na requisição')
  }

  return data
}

export const api = {
  register: (body) =>
    request('/api/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  login: (body) =>
    request('/api/auth/login', { method: 'POST', body: JSON.stringify(body) }),
  listOrders: () => request('/api/orders'),
  getOrder: (id) => request(`/api/orders/${id}`),
  createOrder: (body) =>
    request('/api/orders', { method: 'POST', body: JSON.stringify(body) }),
  updateStatus: (id, status) =>
    request(`/api/orders/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    }),
}
