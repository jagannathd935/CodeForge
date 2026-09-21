import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AdminRoute } from './components/AdminRoute';
import { OrganizerRoute } from './components/OrganizerRoute';

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

import { useAuth } from './context/AuthContext';

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
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)] bg-slate-50">
        <div className="w-10 h-10 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <Routes>
      {/* First Page Gateway: When not logged in, only Login/Register is accessible */}
      <Route
        path="/"
        element={
          isAuthenticated ? <DashboardRouter /> : <LoginPage />
        }
      />
      <Route
        path="/login"
        element={
          isAuthenticated ? <DashboardRouter /> : <LoginPage />
        }
      />
      <Route
        path="/register"
        element={
          isAuthenticated ? <DashboardRouter /> : <RegisterPage />
        }
      />

      {/* Role-Aware Dashboard Router */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardRouter />
          </ProtectedRoute>
        }
      />

      {/* Organizer Studio */}
      <Route
        path="/organizer"
        element={
          <OrganizerRoute>
            <OrganizerDashboardPage />
          </OrganizerRoute>
        }
      />

      {/* Admin Portal */}
      <Route
        path="/admin"
        element={
          <AdminRoute>
            <AdminDashboardPage />
          </AdminRoute>
        }
      />

      {/* Problem Bank & IDE */}
      <Route
        path="/problems"
        element={
          <ProtectedRoute>
            <ProblemListPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/problems/:id"
        element={
          <ProtectedRoute>
            <ProblemDetailPage />
          </ProtectedRoute>
        }
      />

      {/* Contest Solving Mode */}
      <Route
        path="/contests/:contestId/problems/:id"
        element={
          <ProtectedRoute>
            <ProblemDetailPage />
          </ProtectedRoute>
        }
      />

      {/* Contests Hub, Live Arena, Results & Analytics */}
      <Route
        path="/contests"
        element={
          <ProtectedRoute>
            <ContestListPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/contests/:id"
        element={
          <ProtectedRoute>
            <ContestDetailPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/contests/:id/results"
        element={
          <ProtectedRoute>
            <ContestResultsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/contests/:id/analytics"
        element={
          <ProtectedRoute>
            <ContestAnalyticsPage />
          </ProtectedRoute>
        }
      />

      {/* Leaderboard */}
      <Route
        path="/leaderboard"
        element={
          <ProtectedRoute>
            <LeaderboardPage />
          </ProtectedRoute>
        }
      />

      {/* Submissions History & Profile */}
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

      {/* Fallback */}
      <Route
        path="*"
        element={isAuthenticated ? <DashboardRouter /> : <Navigate to="/login" replace />}
      />
    </Routes>
  );
}

export function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <BrowserRouter>
          <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col font-sans selection:bg-blue-600 selection:text-white">
            <Navbar />
            <main className="flex-1">
              <AppRoutes />
            </main>
          </div>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
