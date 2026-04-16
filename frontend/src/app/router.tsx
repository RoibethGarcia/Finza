import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../features/auth/auth-context';
import { AppShell } from '../shared/ui/app-shell';
import { LoginPage } from '../features/auth/login-page';
import { RegisterPage } from '../features/auth/register-page';
import { DashboardPage } from '../features/dashboard/dashboard-page';
import { AccountsPage } from '../features/accounts/accounts-page';
import { CategoriesPage } from '../features/categories/categories-page';
import { TransactionsPage } from '../features/transactions/transactions-page';

function ProtectedLayout() {
  const { session } = useAuth();

  if (!session) {
    return <Navigate to="/login" replace />;
  }

  return (
    <AppShell>
      <Outlet />
    </AppShell>
  );
}

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  {
    path: '/',
    element: <ProtectedLayout />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'accounts', element: <AccountsPage /> },
      { path: 'categories', element: <CategoriesPage /> },
      { path: 'transactions', element: <TransactionsPage /> },
    ],
  },
]);
