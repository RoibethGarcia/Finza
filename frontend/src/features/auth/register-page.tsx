import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './auth-context';

const schema = z.object({
  fullName: z.string().min(3, 'Ingresa tu nombre completo'),
  email: z.string().email('Ingresa un email válido'),
  birthDate: z.string().min(1, 'Ingresa tu fecha de nacimiento'),
  password: z.string().min(8, 'La contraseña debe tener al menos 8 caracteres'),
});

type FormValues = z.infer<typeof schema>;

export function RegisterPage() {
  const navigate = useNavigate();
  const { register: registerAccount } = useAuth();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(schema),
  });

  const onSubmit = handleSubmit(async (values) => {
    await registerAccount(values);
    navigate('/');
  });

  return (
    <div className="auth-page">
      <form className="card form-grid" onSubmit={onSubmit}>
        <h1>Crear cuenta</h1>
        <label>
          Nombre completo
          <input {...register('fullName')} />
          <small>{errors.fullName?.message}</small>
        </label>
        <label>
          Email
          <input type="email" {...register('email')} />
          <small>{errors.email?.message}</small>
        </label>
        <label>
          Fecha de nacimiento
          <input type="date" {...register('birthDate')} />
          <small>{errors.birthDate?.message}</small>
        </label>
        <label>
          Contraseña
          <input type="password" {...register('password')} />
          <small>{errors.password?.message}</small>
        </label>
        <button type="submit" disabled={isSubmitting}>Registrarme</button>
        <p>
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
        </p>
      </form>
    </div>
  );
}
