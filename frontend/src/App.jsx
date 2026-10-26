import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { ToastProvider } from './components/Toast';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AdminRoute } from './components/AdminRoute';
import { OrganizerRoute } from './components/OrganizerRoute';
import { PublicRoute } from './components/PublicRoute';

import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { ProblemListPage } from './pages/ProblemListPage';
import { ProblemDetailPage } from './pages/ProblemDetailPage';
import { SubmissionsPage } from './pages/SubmissionsPage';
import { ProfilePage } from './pages/ProfilePage';
import { AdminDashboardPage } from './pages/AdminDashboardPage';
import { ContestListPage } from './pages/ContestListPage';
import { ContestDetailPage } from './pages/ContestDetailPage';
import { ContestResultsPage } from './pages/ContestResultsPage';
import { ContestAnalyticsPage } from './pages/ContestAnalyticsPage';
import { ParticipantDashboardPage } from './pages/ParticipantDashboardPage';
import { OrganizerDashboardPage } from './pages/OrganizerDashboardPage';
import { LeaderboardPage } from './pages/LeaderboardPage';

// Dynamic Router ensuring Admin, Organizer, and Participant each access their correct dashboard
const DashboardRouter = () => {
  const { user } = useAuth();
  if (user?.role === 'ROLE_ADMIN') {
    return <Navigate to="/admin" replace />;
  }
  if (user?.role === 'ROLE_ORGANIZER') {
    return <Navigate to="/organizer" replace />;
  }
  return <ParticipantDashboardPage />;
};

function AppRoutes() {
  const { loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
        <div className="w-8 h-8 border-3 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <Routes>
      {/* Public Pages */}
      <Route path="/" element={<LandingPage />} />
      <Route
        path="/login"
        element={
          <PublicRoute restricted={true}>
            <LoginPage />
          </PublicRoute>
        }
      />
      <Route
        path="/register"
        element={
          <PublicRoute restricted={true}>
            <RegisterPage />
          </PublicRoute>
        }
      />

      {/* Problems Catalogue & IDE (Browsing is public, submission requires login) */}
      <Route path="/problems" element={<ProblemListPage />} />
      <Route path="/problems/:id" element={<ProblemDetailPage />} />

      {/* Contests Hub, Live Arena, Results & Analytics */}
      <Route path="/contests" element={<ContestListPage />} />
      <Route path="/contests/:id" element={<ContestDetailPage />} />
      <Route path="/contests/:contestId/problems/:id" element={<ProblemDetailPage />} />
      <Route path="/contests/:id/results" element={<ContestResultsPage />} />
      <Route path="/contests/:id/analytics" element={<ContestAnalyticsPage />} />
      <Route path="/leaderboard" element={<LeaderboardPage />} />

      {/* Authenticated User Routes */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardRouter />
          </ProtectedRoute>
        }
      />
      <Route
        path="/submissions"
        element={
          <ProtectedRoute>
            <SubmissionsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <ProfilePage />
          </ProtectedRoute>
        }
      />

      {/* Organizer Routes */}
      <Route
        path="/organizer"
        element={
          <OrganizerRoute>
            <OrganizerDashboardPage />
          </OrganizerRoute>
        }
      />
      <Route
        path="/organizer/contests"
        element={
          <OrganizerRoute>
            <OrganizerDashboardPage />
          </OrganizerRoute>
        }
      />

      {/* Admin Routes */}
      <Route
        path="/admin"
        element={
          <AdminRoute>
            <AdminDashboardPage initialTab="dashboard" />
          </AdminRoute>
        }
      />
      <Route
        path="/admin/problems"
        element={
          <AdminRoute>
            <AdminDashboardPage initialTab="manage" />
          </AdminRoute>
        }
      />
      <Route
        path="/admin/problems/create"
        element={
          <AdminRoute>
            <AdminDashboardPage initialTab="add" />
          </AdminRoute>
        }
      />
      <Route
        path="/admin/problems/:id/edit"
        element={
          <AdminRoute>
            <AdminDashboardPage initialTab="add" />
          </AdminRoute>
        }
      />
      <Route
        path="/admin/users"
        element={
          <AdminRoute>
            <AdminDashboardPage initialTab="users" />
          </AdminRoute>
        }
      />
      <Route
        path="/admin/submissions"
        element={
          <AdminRoute>
            <AdminDashboardPage initialTab="logs" />
          </AdminRoute>
        }
      />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <ToastProvider>
          <BrowserRouter>
            <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col font-sans selection:bg-blue-600 selection:text-white">
              <Navbar />
              <main className="flex-1">
                <AppRoutes />
              </main>
            </div>
          </BrowserRouter>
        </ToastProvider>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
