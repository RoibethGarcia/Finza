import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { api } from '../../shared/api/client';
import { useAuth } from '../auth/auth-context';

const schema = z.object({
  name: z.string().min(2),
  type: z.enum(['CASH', 'BANK', 'SAVINGS', 'CREDIT_LINE']),
  currency: z.string().length(3),
  openingBalance: z.coerce.number().min(0),
});

type FormValues = z.infer<typeof schema>;

export function AccountsPage() {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const accountsQuery = useQuery({
    queryKey: ['accounts'],
    queryFn: () => api.listAccounts(session!.accessToken),
    enabled: !!session,
  });
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { type: 'BANK', currency: 'UYU', openingBalance: 0 },
  });

  const mutation = useMutation({
    mutationFn: (values: FormValues) => api.createAccount(session!.accessToken, values),
    onSuccess: () => {
      form.reset({ type: 'BANK', currency: 'UYU', openingBalance: 0, name: '' });
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });

  return (
    <section className="page-grid">
      <div className="card">
        <h1>Cuentas</h1>
        <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
          <input placeholder="Nombre" {...form.register('name')} />
          <select {...form.register('type')}>
            <option value="BANK">BANK</option>
            <option value="CASH">CASH</option>
            <option value="SAVINGS">SAVINGS</option>
            <option value="CREDIT_LINE">CREDIT_LINE</option>
          </select>
          <input placeholder="Moneda" {...form.register('currency')} />
          <input type="number" step="0.01" {...form.register('openingBalance')} />
          <button type="submit">Crear cuenta</button>
        </form>
      </div>

      <div className="card">
        <h2>Listado</h2>
        <ul className="stack">
          {accountsQuery.data?.map((account) => (
            <li key={account.id}>
              <strong>{account.name}</strong> · {account.type} · {account.openingBalance} {account.currency}
            </li>
          )) ?? <li>Sin cuentas aún</li>}
        </ul>
      </div>
    </section>
  );
}
