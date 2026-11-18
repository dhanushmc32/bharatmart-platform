import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export const RegisterPage: React.FC = () => {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '', fullName: '', phone: '' });
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await register(form);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed');
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10 bg-white p-8 rounded-xl shadow-sm border border-slate-200">
      <h1 className="text-2xl font-bold text-slate-900">Create Customer Account</h1>
      {error && <div className="mt-4 p-3 rounded-lg bg-rose-50 text-rose-700 text-sm">{error}</div>}
      <form onSubmit={handleSubmit} className="mt-6 space-y-3">
        {(['fullName', 'username', 'email', 'phone', 'password'] as const).map((field) => (
          <div key={field}>
            <label className="block text-sm font-medium text-slate-700 capitalize">{field}</label>
            <input
              type={field === 'password' ? 'password' : field === 'email' ? 'email' : 'text'}
              required={field !== 'phone'}
              value={form[field]}
              onChange={(e) => setForm({ ...form, [field]: e.target.value })}
              className="mt-1 w-full border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>
        ))}
        <button type="submit" className="w-full py-2.5 rounded-lg bg-indigo-600 text-white font-semibold hover:bg-indigo-700">
          Register
        </button>
      </form>
      <p className="mt-4 text-sm text-center text-slate-600">
        Already registered? <Link to="/login" className="text-indigo-600 font-medium">Sign In</Link>
      </p>
    </div>
  );
};
