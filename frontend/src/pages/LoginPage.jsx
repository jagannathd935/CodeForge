import React, { useState } from 'react';
import { Link, useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Code2, ArrowRight, Lock, Mail, AlertCircle } from 'lucide-react';

export const LoginPage = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login, isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  if (isAuthenticated) {
    if (user?.role === 'ROLE_ADMIN') {
      return <Navigate to="/admin" replace />;
    }
    if (user?.role === 'ROLE_ORGANIZER') {
      return <Navigate to="/organizer" replace />;
    }
    return <Navigate to="/dashboard" replace />;
  }

  const handleLoginSuccess = (userData) => {
    if (userData.role === 'ROLE_ADMIN') {
      navigate('/admin');
    } else if (userData.role === 'ROLE_ORGANIZER') {
      navigate('/organizer');
    } else {
      navigate('/dashboard');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const userData = await login(usernameOrEmail, password);
      handleLoginSuccess(userData);
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 sm:p-6 lg:p-8 font-sans bg-slate-50">
      <div className="max-w-4xl w-full grid grid-cols-1 md:grid-cols-2 rounded-2xl overflow-hidden bg-white border border-slate-200 shadow-xl">
        
        {/* Left: Login Form */}
        <div className="p-8 sm:p-10 flex flex-col justify-center">
          <div className="flex items-center space-x-3 mb-6">
            <div className="w-10 h-10 rounded-xl bg-blue-50 border border-blue-200 flex items-center justify-center text-blue-600 shadow-2xs">
              <Code2 className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-slate-900 tracking-tight">CodeForge</h2>
              <p className="text-xs text-slate-500 font-medium">Code Today. Create Tomorrow.</p>
            </div>
          </div>

          <h3 className="text-2xl font-extrabold text-slate-900 mb-1">Welcome Back</h3>
          <p className="text-xs text-slate-500 mb-6">Sign in to continue your coding journey</p>

          {/* Tab Switcher */}
          <div className="grid grid-cols-2 p-1 bg-slate-100 rounded-xl border border-slate-200 mb-6 text-center text-sm font-semibold">
            <span className="py-2 rounded-lg bg-blue-600 text-white font-bold shadow-xs cursor-pointer">Login</span>
            <Link to="/register" className="py-2 text-slate-600 hover:text-slate-900 transition-colors">Register</Link>
          </div>

          {error && (
            <div className="mb-4 p-3 rounded-lg bg-rose-50 border border-rose-300 flex items-center gap-2 text-rose-700 text-xs font-semibold">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Email</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Mail className="w-4 h-4" />
                </div>
                <input
                  type="text"
                  required
                  value={usernameOrEmail}
                  onChange={(e) => setUsernameOrEmail(e.target.value)}
                  placeholder="johndoe@example.com"
                  className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:border-blue-600 focus:bg-white focus:ring-1 focus:ring-blue-600 transition-all font-medium"
                />
              </div>
            </div>

            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="block text-xs font-semibold text-slate-700">Password</label>
                <button
                  type="button"
                  onClick={() => alert('Password reset link sent to registered email in evaluation mode.')}
                  className="text-[11px] text-blue-600 font-semibold hover:underline"
                >
                  Forgot Password?
                </button>
              </div>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:border-blue-600 focus:bg-white focus:ring-1 focus:ring-blue-600 transition-all font-medium"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 mt-2 bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm rounded-xl transition-all shadow-md flex items-center justify-center space-x-2 disabled:opacity-50"
            >
              {loading ? (
                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              ) : (
                <>
                  <span>Login</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          <p className="mt-6 text-center text-xs text-slate-500 font-medium">
            Don't have an account?{' '}
            <Link to="/register" className="text-blue-600 font-bold hover:underline">
              Register
            </Link>
          </p>
        </div>

        {/* Right: Inspiring Quote Card (Dennis Ritchie) */}
        <div className="hidden md:flex flex-col justify-between p-10 auth-quote-card relative overflow-hidden">
          <div className="relative z-10 flex items-center justify-between text-xs quote-header font-mono">
            <span>CodeForge Platform</span>
            <span>v1.0 (MVP)</span>
          </div>

          <div className="relative z-10 my-auto py-12 text-center space-y-6 max-w-sm mx-auto">
            <blockquote className="text-xl sm:text-2xl font-serif italic leading-relaxed">
              &ldquo;The only way to learn a new programming language is by writing programs in it.&rdquo;
            </blockquote>
            <p className="text-sm font-semibold tracking-wider font-sans quote-author">
              &mdash; Dennis Ritchie
            </p>
          </div>

          <div className="relative z-10 flex items-center justify-between pt-6 border-t quote-footer text-[11px]">
            <span>Practice &bull; Improve &bull; Achieve</span>
            <span>Java &bull; Python &bull; C++</span>
          </div>
        </div>

      </div>
    </div>
  );
};
