import { useEffect, useState } from 'react'
import { api } from '../api'
import { useAuth } from '../context/AuthContext'
import CreateOrderForm from '../components/CreateOrderForm'
import OrderList from '../components/OrderList'

export default function OrdersPage() {
  const { user, logout } = useAuth()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)

  async function loadOrders() {
    setError('')
    try {
      const data = await api.listOrders()
      setOrders(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadOrders()
  }, [])

  async function handleCreated(order) {
    setOrders((current) => [order, ...current])
    setShowForm(false)
  }

  async function handleStatusChange(id, status) {
    const updated = await api.updateStatus(id, status)
    setOrders((current) => current.map((order) => (order.id === id ? updated : order)))
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="brand">Delivery Tracker</p>
          <p className="muted">Olá, {user?.name}</p>
        </div>
        <div className="topbar-actions">
          <button type="button" className="secondary" onClick={() => setShowForm((v) => !v)}>
            {showForm ? 'Fechar formulário' : 'Novo pedido'}
          </button>
          <button type="button" className="ghost" onClick={logout}>
            Sair
          </button>
        </div>
      </header>

      <main className="content">
        {showForm && (
          <section className="panel">
            <h2>Novo pedido</h2>
            <CreateOrderForm onCreated={handleCreated} onCancel={() => setShowForm(false)} />
          </section>
        )}

        <section className="panel">
          <div className="panel-header">
            <h2>Pedidos</h2>
            <button type="button" className="ghost" onClick={loadOrders}>
              Atualizar
            </button>
          </div>

          {error && <div className="alert">{error}</div>}
          {loading ? <p className="muted">Carregando pedidos...</p> : (
            <OrderList orders={orders} onStatusChange={handleStatusChange} />
          )}
        </section>
      </main>
    </div>
  )
}
