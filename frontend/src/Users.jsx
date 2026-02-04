import React, { useEffect, useState } from 'react'

export default function Users() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const apiBase = import.meta.env.VITE_API_BASE_URL || ''

  useEffect(() => {
    fetch(apiBase + '/users')
      .then((r) => r.json())
      .then((data) => {
        // ensure array
        setUsers(Array.isArray(data) ? data : [])
        setLoading(false)
      })
      .catch((err) => {
        console.error('fetch /users failed', err)
        setLoading(false)
      })
  }, [])

  const total = users.reduce((s, u) => s + (u.balance || 0), 0)

  if (loading) return <div className="mt-3">Loading…</div>

  return (
    <div>
      <table className="table mt-3">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Balance</th>
          </tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.id}</td>
              <td>{u.name}</td>
              <td>{Number(u.balance).toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="mt-2">
        <strong>Total:</strong> {total.toFixed(2)}
      </div>
    </div>
  )
}
