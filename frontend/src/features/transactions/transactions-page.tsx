import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { api } from '../../shared/api/client';
import { useAuth } from '../auth/auth-context';

const transactionSchema = z.object({
  accountId: z.string().uuid(),
  categoryId: z.string().uuid(),
  type: z.enum(['INCOME', 'EXPENSE']),
  amount: z.coerce.number().positive(),
  occurredAt: z.string().min(1),
  description: z.string().optional(),
});

const transferSchema = z.object({
  sourceAccountId: z.string().uuid(),
  targetAccountId: z.string().uuid(),
  amount: z.coerce.number().positive(),
  occurredAt: z.string().min(1),
  description: z.string().optional(),
});

type TransactionFormValues = z.infer<typeof transactionSchema>;
type TransferFormValues = z.infer<typeof transferSchema>;

function defaultOccurredAt() {
  return new Date().toISOString().slice(0, 16);
}

export function TransactionsPage() {
  const { session } = useAuth();
  const queryClient = useQueryClient();

  const accountsQuery = useQuery({
    queryKey: ['accounts'],
    queryFn: () => api.listAccounts(session!.accessToken),
    enabled: !!session,
  });
  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: () => api.listCategories(session!.accessToken),
    enabled: !!session,
  });
  const transactionsQuery = useQuery({
    queryKey: ['transactions'],
    queryFn: () => api.listTransactions(session!.accessToken),
    enabled: !!session,
  });

  const transactionForm = useForm<TransactionFormValues>({
    resolver: zodResolver(transactionSchema),
    defaultValues: { type: 'EXPENSE', occurredAt: defaultOccurredAt() },
  });
  const transferForm = useForm<TransferFormValues>({
    resolver: zodResolver(transferSchema),
    defaultValues: { occurredAt: defaultOccurredAt() },
  });

  const createTransactionMutation = useMutation({
    mutationFn: (values: TransactionFormValues) => api.createTransaction(session!.accessToken, {
      ...values,
      occurredAt: new Date(values.occurredAt).toISOString(),
    }),
    onSuccess: () => {
      transactionForm.reset({ type: 'EXPENSE', occurredAt: defaultOccurredAt() });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
  const createTransferMutation = useMutation({
    mutationFn: (values: TransferFormValues) => api.createTransfer(session!.accessToken, {
      ...values,
      occurredAt: new Date(values.occurredAt).toISOString(),
    }),
    onSuccess: () => {
      transferForm.reset({ occurredAt: defaultOccurredAt() });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });

  return (
    <section className="page-grid">
      <div className="card-grid">
        <div className="card">
          <h1>Nuevo movimiento</h1>
          <form className="form-grid" onSubmit={transactionForm.handleSubmit((values) => createTransactionMutation.mutate(values))}>
            <select {...transactionForm.register('accountId')}>
              <option value="">Cuenta</option>
              {accountsQuery.data?.map((account) => <option key={account.id} value={account.id}>{account.name}</option>)}
            </select>
            <select {...transactionForm.register('categoryId')}>
              <option value="">Categoría</option>
              {categoriesQuery.data?.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
            </select>
            <select {...transactionForm.register('type')}>
              <option value="EXPENSE">EXPENSE</option>
              <option value="INCOME">INCOME</option>
            </select>
            <input type="number" step="0.01" placeholder="Monto" {...transactionForm.register('amount')} />
            <input type="datetime-local" {...transactionForm.register('occurredAt')} />
            <input placeholder="Descripción" {...transactionForm.register('description')} />
            <button type="submit">Registrar movimiento</button>
          </form>
        </div>

        <div className="card">
          <h2>Transferencia interna</h2>
          <form className="form-grid" onSubmit={transferForm.handleSubmit((values) => createTransferMutation.mutate(values))}>
            <select {...transferForm.register('sourceAccountId')}>
              <option value="">Cuenta origen</option>
              {accountsQuery.data?.map((account) => <option key={account.id} value={account.id}>{account.name}</option>)}
            </select>
            <select {...transferForm.register('targetAccountId')}>
              <option value="">Cuenta destino</option>
              {accountsQuery.data?.map((account) => <option key={account.id} value={account.id}>{account.name}</option>)}
            </select>
            <input type="number" step="0.01" placeholder="Monto" {...transferForm.register('amount')} />
            <input type="datetime-local" {...transferForm.register('occurredAt')} />
            <input placeholder="Descripción" {...transferForm.register('description')} />
            <button type="submit">Transferir</button>
          </form>
        </div>
      </div>

      <div className="card">
        <h2>Historial</h2>
        <ul className="stack">
          {transactionsQuery.data?.map((transaction) => (
            <li key={transaction.id}>
              <strong>{transaction.type}</strong> · {transaction.accountName} · {transaction.amount} {transaction.currency}
              <div>{transaction.categoryName ?? 'Sin categoría'} · {transaction.description ?? 'Sin detalle'}</div>
            </li>
          )) ?? <li>Sin movimientos aún</li>}
        </ul>
      </div>
    </section>
  );
}
