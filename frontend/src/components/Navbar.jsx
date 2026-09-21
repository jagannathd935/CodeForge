import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Code2, LogOut, LayoutDashboard } from 'lucide-react';

export const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path || location.pathname.startsWith(`${path}/`);

  const initials = user?.username ? user.username.substring(0, 2).toUpperCase() : 'CF';

  const getDashboardPath = () => {
    if (user?.role === 'ROLE_ADMIN') return '/admin';
    if (user?.role === 'ROLE_ORGANIZER') return '/organizer';
    return '/dashboard';
  };

  const getRoleLabel = () => {
    if (user?.role === 'ROLE_ADMIN') return 'Admin';
    if (user?.role === 'ROLE_ORGANIZER') return 'Organizer';
    return 'Participant';
  };

  const getRoleBadgeStyle = () => {
    if (user?.role === 'ROLE_ADMIN') {
      return 'bg-purple-50 text-purple-700 border-purple-200';
    }
    if (user?.role === 'ROLE_ORGANIZER') {
      return 'bg-amber-50 text-amber-700 border-amber-200';
    }
    return 'bg-emerald-50 text-emerald-700 border-emerald-200';
  };

  const dashboardTarget = getDashboardPath();

  return (
    <nav className="sticky top-0 z-50 bg-white/95 backdrop-blur border-b border-slate-200 shadow-xs font-sans">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Brand Logo */}
          <div className="flex items-center space-x-8">
            <Link to={isAuthenticated ? dashboardTarget : "/login"} className="flex items-center space-x-3 group">
              <div className="w-9 h-9 rounded-lg bg-blue-50 border border-blue-200 flex items-center justify-center text-blue-600 group-hover:bg-blue-100 transition-colors shadow-xs">
                <Code2 className="w-5 h-5" />
              </div>
              <div className="flex flex-col">
                <span className="text-lg font-bold tracking-tight text-slate-900 group-hover:text-blue-600 transition-colors">
                  CodeForge
                </span>
                <span className="text-[10px] text-slate-500 font-mono tracking-wider -mt-1 font-semibold">
                  CAMPUS ARENA
                </span>
              </div>
            </Link>

            {/* Clean Navigation Links */}
            {isAuthenticated && (
              <div className="hidden md:flex items-center space-x-1">
                <Link
                  to={dashboardTarget}
                  className={`px-3 py-1.5 rounded-lg text-sm font-semibold flex items-center gap-1.5 transition-colors ${
                    isActive(dashboardTarget)
                      ? 'bg-blue-50 text-blue-600 border border-blue-200 shadow-xs'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                  }`}
                >
                  <LayoutDashboard className="w-4 h-4" />
                  <span>Dashboard</span>
                </Link>
                <Link
                  to="/problems"
                  className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-colors ${
                    isActive('/problems')
                      ? 'bg-blue-50 text-blue-600 border border-blue-200 shadow-xs'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                  }`}
                >
                  Problems
                </Link>
                <Link
                  to="/contests"
                  className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-colors ${
                    isActive('/contests')
                      ? 'bg-blue-50 text-blue-600 border border-blue-200 shadow-xs'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                  }`}
                >
                  Contests
                </Link>
                <Link
                  to="/submissions"
                  className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-colors ${
                    isActive('/submissions')
                      ? 'bg-blue-50 text-blue-600 border border-blue-200 shadow-xs'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                  }`}
                >
                  Submissions
                </Link>
                <Link
                  to="/leaderboard"
                  className={`px-3 py-1.5 rounded-lg text-sm font-semibold transition-colors ${
                    isActive('/leaderboard')
                      ? 'bg-blue-50 text-blue-600 border border-blue-200 shadow-xs'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                  }`}
                >
                  Leaderboard
                </Link>
              </div>
            )}
          </div>

          {/* Right Action Menu */}
          <div className="flex items-center space-x-3">
            {isAuthenticated && (
              <>
                {/* Role Identifier (Left side of avatar circle & name) */}
                <span className={`px-2.5 py-1 rounded-md text-xs font-bold border ${getRoleBadgeStyle()}`}>
                  {getRoleLabel()}
                </span>

                {/* User Avatar Circle + Name */}
                <Link to="/profile" className="flex items-center space-x-2.5 group">
                  {user?.avatarUrl ? (
                    <img
                      src={user.avatarUrl}
                      alt={user.username}
                      className="w-8 h-8 rounded-full object-cover shadow-xs group-hover:ring-2 ring-blue-500 transition-all border border-slate-300"
                    />
                  ) : (
                    <div className="w-8 h-8 rounded-full bg-slate-100 border border-slate-300 text-slate-800 flex items-center justify-center text-xs font-bold shadow-xs group-hover:ring-2 ring-blue-500 transition-all font-mono">
                      {initials}
                    </div>
                  )}
                  <span className="hidden sm:inline text-sm font-semibold text-slate-800 group-hover:text-blue-600 transition-colors">
                    {user?.username}
                  </span>
                </Link>

                {/* Logout */}
                <button
                  onClick={handleLogout}
                  title="Logout"
                  className="p-2 text-slate-500 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};
