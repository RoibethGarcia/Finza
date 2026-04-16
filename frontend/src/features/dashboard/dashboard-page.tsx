import { useQuery } from '@tanstack/react-query';
import { api } from '../../shared/api/client';
import { useAuth } from '../auth/auth-context';

export function DashboardPage() {
  const { session } = useAuth();
  const dashboardQuery = useQuery({
    queryKey: ['dashboard'],
    queryFn: () => api.getDashboard(session!.accessToken),
    enabled: !!session,
  });

  if (dashboardQuery.isLoading) {
    return <div className="card">Cargando dashboard...</div>;
  }

  if (dashboardQuery.error || !dashboardQuery.data) {
    return <div className="card">No se pudo cargar el dashboard.</div>;
  }

  return (
    <section className="page-grid">
      <div className="card">
        <h1>Dashboard</h1>
        <p>
          Período: {dashboardQuery.data.periodStart} → {dashboardQuery.data.periodEnd}
        </p>
      </div>

      <div className="card-grid">
        {dashboardQuery.data.currencies.map((currency) => (
          <article className="card" key={currency.currency}>
            <h2>{currency.currency}</h2>
            <p>Ingresos: {currency.totalIncome}</p>
            <p>Gastos: {currency.totalExpense}</p>
            <p>Neto: {currency.netAmount}</p>
            <p>Disponible: {currency.availableBalance}</p>
          </article>
        ))}
      </div>

      <div className="card">
        <h2>Balances por cuenta</h2>
        <table>
          <thead>
            <tr>
              <th>Cuenta</th>
              <th>Moneda</th>
              <th>Saldo actual</th>
              <th>Neto del período</th>
            </tr>
          </thead>
          <tbody>
            {dashboardQuery.data.accounts.map((account) => (
              <tr key={account.accountId}>
                <td>{account.accountName}</td>
                <td>{account.currency}</td>
                <td>{account.currentBalance}</td>
                <td>{account.periodNet}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="card">
        <h2>Movimientos recientes</h2>
        <ul className="stack">
          {dashboardQuery.data.recentTransactions.map((transaction) => (
            <li key={transaction.transactionId}>
              <strong>{transaction.type}</strong> · {transaction.accountName} · {transaction.amount} {transaction.currency}
              <div>{transaction.categoryName ?? 'Sin categoría'} · {transaction.description ?? 'Sin detalle'}</div>
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
