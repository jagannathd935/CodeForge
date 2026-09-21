import React, { useState } from 'react';
import { Link, useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Code2, ArrowRight, Lock, User, Mail, AlertCircle, Building2, ShieldCheck, Flag, Users } from 'lucide-react';

export const RegisterPage = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [organization, setOrganization] = useState('');
  const [role, setRole] = useState('ROLE_PARTICIPANT');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { register, isAuthenticated, user } = useAuth();
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!organization.trim()) {
      setError('Please provide your Organization or College Name.');
      return;
    }

    setLoading(true);

    try {
      const userData = await register(username, email, password, organization.trim(), role);
      if (userData.role === 'ROLE_ADMIN') {
        navigate('/admin');
      } else if (userData.role === 'ROLE_ORGANIZER') {
        navigate('/organizer');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 sm:p-6 lg:p-8 font-sans bg-slate-50">
      <div className="max-w-4xl w-full grid grid-cols-1 md:grid-cols-2 rounded-2xl overflow-hidden bg-white border border-slate-200 shadow-xl">
        
        {/* Left: Register Form */}
        <div className="p-8 sm:p-10 flex flex-col justify-center">
          <div className="flex items-center space-x-3 mb-5">
            <div className="w-10 h-10 rounded-xl bg-blue-50 border border-blue-200 flex items-center justify-center text-blue-600 shadow-2xs">
              <Code2 className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-slate-900 tracking-tight">CodeForge</h2>
              <p className="text-xs text-slate-500 font-medium">Code Today. Create Tomorrow.</p>
            </div>
          </div>

          <h3 className="text-2xl font-extrabold text-slate-900 mb-1">Create Account</h3>
          <p className="text-xs text-slate-500 mb-5">Join CodeForge to practice and compete</p>

          {/* Tab Switcher */}
          <div className="grid grid-cols-2 p-1 bg-slate-100 rounded-xl border border-slate-200 mb-5 text-center text-sm font-semibold">
            <Link to="/login" className="py-2 text-slate-600 hover:text-slate-900 transition-colors">Login</Link>
            <span className="py-2 rounded-lg bg-blue-600 text-white font-bold shadow-xs cursor-pointer">Register</span>
          </div>

          {error && (
            <div className="mb-4 p-3 rounded-lg bg-rose-50 border border-rose-300 flex items-center gap-2 text-rose-700 text-xs font-semibold">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-3">
            {/* Role Switcher */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">Select Role</label>
              <div className="grid grid-cols-3 gap-2">
                <button
                  type="button"
                  onClick={() => setRole('ROLE_PARTICIPANT')}
                  className={`py-1.5 px-2 rounded-xl text-xs font-bold border transition-all text-center flex flex-col items-center gap-0.5 ${
                    role === 'ROLE_PARTICIPANT'
                      ? 'bg-emerald-50 border-emerald-500 text-emerald-700 shadow-xs'
                      : 'bg-slate-50 border-slate-200 text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                  }`}
                >
                  <Users className="w-3.5 h-3.5" />
                  <span>Participant</span>
                </button>
                <button
                  type="button"
                  onClick={() => setRole('ROLE_ORGANIZER')}
                  className={`py-1.5 px-2 rounded-xl text-xs font-bold border transition-all text-center flex flex-col items-center gap-0.5 ${
                    role === 'ROLE_ORGANIZER'
                      ? 'bg-amber-50 border-amber-500 text-amber-700 shadow-xs'
                      : 'bg-slate-50 border-slate-200 text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                  }`}
                >
                  <Flag className="w-3.5 h-3.5" />
                  <span>Organizer</span>
                </button>
                <button
                  type="button"
                  onClick={() => setRole('ROLE_ADMIN')}
                  className={`py-1.5 px-2 rounded-xl text-xs font-bold border transition-all text-center flex flex-col items-center gap-0.5 ${
                    role === 'ROLE_ADMIN'
                      ? 'bg-purple-50 border-purple-500 text-purple-700 shadow-xs'
                      : 'bg-slate-50 border-slate-200 text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                  }`}
                >
                  <ShieldCheck className="w-3.5 h-3.5" />
                  <span>Admin</span>
                </button>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Organization / College *</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Building2 className="w-4 h-4" />
                </div>
                <input
                  type="text"
                  required
                  value={organization}
                  onChange={(e) => setOrganization(e.target.value)}
                  placeholder="e.g. Stanford, MIT, College of Engg"
                  className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:border-blue-600 focus:bg-white transition-all font-medium"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Username *</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                    <User className="w-3.5 h-3.5" />
                  </div>
                  <input
                    type="text"
                    required
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="johndoe"
                    className="w-full pl-8 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:border-blue-600 focus:bg-white transition-all font-medium"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Email *</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                    <Mail className="w-3.5 h-3.5" />
                  </div>
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="john@example.com"
                    className="w-full pl-8 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:border-blue-600 focus:bg-white transition-all font-medium"
                  />
                </div>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Password *</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  type="password"
                  required
                  minLength={6}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="At least 6 characters"
                  className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:border-blue-600 focus:bg-white transition-all font-medium"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 mt-2 bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs rounded-xl transition-all shadow-md flex items-center justify-center space-x-2 disabled:opacity-50"
            >
              {loading ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              ) : (
                <>
                  <span>Create Account</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          <p className="mt-5 text-center text-xs text-slate-500 font-medium">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-600 font-bold hover:underline">
              Login
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
