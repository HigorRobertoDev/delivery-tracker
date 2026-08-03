const STATUS_LABELS = {
  RECEBIDO: 'Recebido',
  EM_PREPARO: 'Em preparo',
  SAIU_PARA_ENTREGA: 'Saiu para entrega',
  ENTREGUE: 'Entregue',
  CANCELADO: 'Cancelado',
}

const STATUS_OPTIONS = Object.keys(STATUS_LABELS)

function formatDate(value) {
  return new Date(value).toLocaleString('pt-BR')
}

export default function OrderList({ orders, onStatusChange }) {
  if (!orders.length) {
    return <p className="muted">Nenhum pedido cadastrado ainda.</p>
  }

  return (
    <div className="order-list">
      {orders.map((order) => (
        <article key={order.id} className="order-item">
          <div className="order-item-header">
            <div>
              <h3>Pedido #{order.id}</h3>
              <p className="muted">{order.customerName}</p>
            </div>
            <span className={`status status-${order.status.toLowerCase()}`}>
              {STATUS_LABELS[order.status]}
            </span>
          </div>

          <p><strong>Endereço:</strong> {order.deliveryAddress}</p>
          <ul className="item-list">
            {order.items.map((item, index) => (
              <li key={`${order.id}-${index}`}>
                {item.quantity}x {item.name}
              </li>
            ))}
          </ul>

          <div className="order-item-footer">
            <p className="muted">Criado em {formatDate(order.createdAt)}</p>
            <label className="status-select">
              Status
              <select
                value={order.status}
                onChange={async (e) => {
                  try {
                    await onStatusChange(order.id, e.target.value)
                  } catch (err) {
                    alert(err.message)
                  }
                }}
              >
                {STATUS_OPTIONS.map((status) => (
                  <option key={status} value={status}>
                    {STATUS_LABELS[status]}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </article>
      ))}
    </div>
  )
}
