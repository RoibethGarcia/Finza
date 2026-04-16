import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { api } from '../../shared/api/client';
import { useAuth } from '../auth/auth-context';

const schema = z.object({
  name: z.string().min(2),
  type: z.enum(['INCOME', 'EXPENSE']),
});

type FormValues = z.infer<typeof schema>;

export function CategoriesPage() {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: () => api.listCategories(session!.accessToken),
    enabled: !!session,
  });
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { type: 'EXPENSE' },
  });

  const mutation = useMutation({
    mutationFn: (values: FormValues) => api.createCategory(session!.accessToken, values),
    onSuccess: () => {
      form.reset({ name: '', type: 'EXPENSE' });
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
  });

  return (
    <section className="page-grid">
      <div className="card">
        <h1>Categorías</h1>
        <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
          <input placeholder="Nombre" {...form.register('name')} />
          <select {...form.register('type')}>
            <option value="EXPENSE">EXPENSE</option>
            <option value="INCOME">INCOME</option>
          </select>
          <button type="submit">Crear categoría</button>
        </form>
      </div>

      <div className="card">
        <h2>Listado</h2>
        <ul className="stack">
          {categoriesQuery.data?.map((category) => (
            <li key={category.id}>
              <strong>{category.name}</strong> · {category.type}
            </li>
          )) ?? <li>Sin categorías aún</li>}
        </ul>
      </div>
    </section>
  );
}
