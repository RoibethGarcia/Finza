import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './auth-context';

const schema = z.object({
  email: z.string().email('Ingresa un email válido'),
  password: z.string().min(8, 'La contraseña debe tener al menos 8 caracteres'),
});

type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(schema),
  });

  const onSubmit = handleSubmit(async (values) => {
    await login(values);
    navigate('/');
  });

  return (
    <div className="auth-page">
      <form className="card form-grid" onSubmit={onSubmit}>
        <h1>Entrar a Finza</h1>
        <label>
          Email
          <input type="email" {...register('email')} />
          <small>{errors.email?.message}</small>
        </label>
        <label>
          Contraseña
          <input type="password" {...register('password')} />
          <small>{errors.password?.message}</small>
        </label>
        <button type="submit" disabled={isSubmitting}>Iniciar sesión</button>
        <p>
          ¿No tienes cuenta? <Link to="/register">Regístrate</Link>
        </p>
      </form>
    </div>
  );
}
