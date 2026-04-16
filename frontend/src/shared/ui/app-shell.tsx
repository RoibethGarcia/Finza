import { PropsWithChildren } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../features/auth/auth-context';

export function AppShell({ children }: PropsWithChildren) {
  const navigate = useNavigate();
  const { session, logout } = useAuth();

  return (
    <div className="layout">
      <aside className="sidebar">
        <div>
          <h1>Finza</h1>
          <p>{session?.user.fullName}</p>
        </div>
        <nav className="stack">
          <NavLink to="/">Dashboard</NavLink>
          <NavLink to="/accounts">Cuentas</NavLink>
          <NavLink to="/categories">Categorías</NavLink>
          <NavLink to="/transactions">Movimientos</NavLink>
        </nav>
        <button
          type="button"
          onClick={async () => {
            await logout();
            navigate('/login');
          }}
        >
          Cerrar sesión
        </button>
      </aside>
      <main className="content">{children}</main>
    </div>
  );
}
