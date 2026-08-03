import { useState } from 'react'
import { api } from '../api'

const emptyItem = () => ({ name: '', quantity: 1 })

export default function CreateOrderForm({ onCreated, onCancel }) {
  const [customerName, setCustomerName] = useState('')
  const [deliveryAddress, setDeliveryAddress] = useState('')
  const [items, setItems] = useState([emptyItem()])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function updateItem(index, field, value) {
    setItems((current) =>
      current.map((item, i) => (i === index ? { ...item, [field]: value } : item)),
    )
  }

  function addItem() {
    setItems((current) => [...current, emptyItem()])
  }

  function removeItem(index) {
    setItems((current) => (current.length === 1 ? current : current.filter((_, i) => i !== index)))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      const order = await api.createOrder({
        customerName,
        deliveryAddress,
        items: items.map((item) => ({
          name: item.name,
          quantity: Number(item.quantity),
        })),
      })
      onCreated(order)
      setCustomerName('')
      setDeliveryAddress('')
      setItems([emptyItem()])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="stack">
      {error && <div className="alert">{error}</div>}

      <label>
        Cliente
        <input
          type="text"
          value={customerName}
          onChange={(e) => setCustomerName(e.target.value)}
          required
          placeholder="Nome do cliente"
        />
      </label>

      <label>
        Endereço de entrega
        <input
          type="text"
          value={deliveryAddress}
          onChange={(e) => setDeliveryAddress(e.target.value)}
          required
          placeholder="Rua, número, bairro"
        />
      </label>

      <div className="items-block">
        <div className="panel-header">
          <h3>Itens</h3>
          <button type="button" className="ghost" onClick={addItem}>
            Adicionar item
          </button>
        </div>

        {items.map((item, index) => (
          <div key={index} className="item-row">
            <input
              type="text"
              value={item.name}
              onChange={(e) => updateItem(index, 'name', e.target.value)}
              required
              placeholder="Nome do item"
            />
            <input
              type="number"
              min="1"
              value={item.quantity}
              onChange={(e) => updateItem(index, 'quantity', e.target.value)}
              required
            />
            <button type="button" className="ghost" onClick={() => removeItem(index)}>
              Remover
            </button>
          </div>
        ))}
      </div>

      <div className="form-actions">
        <button type="button" className="ghost" onClick={onCancel}>
          Cancelar
        </button>
        <button type="submit" disabled={loading}>
          {loading ? 'Salvando...' : 'Criar pedido'}
        </button>
      </div>
    </form>
  )
}
