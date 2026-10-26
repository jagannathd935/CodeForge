import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function PublicRoute({ children, restricted = false }) {
  const { isAuthenticated, loading, user } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
        <div className="w-8 h-8 border-3 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (isAuthenticated && restricted) {
    if (user?.role === 'ROLE_ADMIN') return <Navigate to="/admin" replace />;
    if (user?.role === 'ROLE_ORGANIZER') return <Navigate to="/organizer" replace />;
    return <Navigate to="/problems" replace />;
  }

  return children;
}

export default PublicRoute;
