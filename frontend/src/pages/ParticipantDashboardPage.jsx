import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import {
  Trophy,
  Clock,
  Calendar,
  CheckCircle2,
  XCircle,
  ArrowRight,
  Flame,
  Code2,
  Zap,
  Award,
  Layers,
  Sparkles
} from 'lucide-react';
import { DifficultyBadge, StatusBadge } from '../components/Badges';

export const ParticipantDashboardPage = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [contests, setContests] = useState([]);
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const [profRes, contestsRes, subsRes] = await Promise.allSettled([
        api.get('/users/me'),
        api.get('/contests'),
        api.get('/submissions/my')
      ]);

      if (profRes.status === 'fulfilled') setProfile(profRes.value.data.data);
      if (contestsRes.status === 'fulfilled') setContests(contestsRes.value.data.data || []);
      if (subsRes.status === 'fulfilled') setSubmissions(subsRes.value.data.data || []);
    } catch (err) {
      console.error('Failed to load dashboard data', err);
    } finally {
      setLoading(false);
    }
  };

  const activeContests = contests.filter(c => c.status === 'ACTIVE' || c.status === 'LIVE');
  const upcomingContests = contests.filter(c => c.status === 'UPCOMING');
  const completedContests = contests.filter(c => c.status === 'COMPLETED' || c.status === 'ENDED');

  const solvedCount = profile?.solvedCount || submissions.filter(s => s.status === 'ACCEPTED').length;
  const totalSubmissions = profile?.totalSubmissions || submissions.length;
  const acceptanceRate = totalSubmissions > 0 ? Math.round((solvedCount * 100) / totalSubmissions) : 0;

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)] bg-[#0B0F19]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <span className="text-xs text-gray-400">Loading your coding workspace...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Personalized Welcome Banner */}
      <div className="rounded-3xl bg-gradient-to-r from-gray-900 via-gray-900/90 to-blue-950/40 border border-gray-800 p-6 sm:p-8 shadow-2xl relative overflow-hidden">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 relative z-10">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/10 border border-blue-500/30 text-blue-400 text-xs font-semibold">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Campus Competitive Coding</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
              Welcome back, {user?.username || 'Coder'}
            </h1>
            <p className="text-xs sm:text-sm text-gray-400 max-w-xl">
              Track your per-problem solving speed, join live college challenges, and master data structures & algorithms.
            </p>
          </div>

          {/* Quick Action Links */}
          <div className="flex flex-wrap items-center gap-3">
            <Link
              to="/problems"
              className="px-4 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs inline-flex items-center gap-2 transition-all shadow-[0_0_15px_rgba(59,130,246,0.3)]"
            >
              <Code2 className="w-4 h-4" />
              <span>Practice DSA</span>
            </Link>
            <Link
              to="/contests"
              className="px-4 py-2.5 rounded-xl bg-gray-800 hover:bg-gray-700 text-gray-200 font-bold text-xs inline-flex items-center gap-2 transition-all border border-gray-700"
            >
              <Trophy className="w-4 h-4 text-amber-400" />
              <span>Browse Contests</span>
            </Link>
          </div>
        </div>
      </div>

      {/* KPI Statistic Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <div className="rounded-2xl bg-gray-900/80 border border-gray-800/90 p-5 shadow-lg flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <div>
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400 block">Problems Solved</span>
            <span className="text-2xl font-black text-white font-mono">{solvedCount}</span>
          </div>
        </div>

        <div className="rounded-2xl bg-gray-900/80 border border-gray-800/90 p-5 shadow-lg flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400">
            <Trophy className="w-6 h-6" />
          </div>
          <div>
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400 block">Contests Joined</span>
            <span className="text-2xl font-black text-white font-mono">{completedContests.length + activeContests.length}</span>
          </div>
        </div>

        <div className="rounded-2xl bg-gray-900/80 border border-gray-800/90 p-5 shadow-lg flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400">
            <Zap className="w-6 h-6" />
          </div>
          <div>
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400 block">Total Submissions</span>
            <span className="text-2xl font-black text-white font-mono">{totalSubmissions}</span>
          </div>
        </div>

        <div className="rounded-2xl bg-gray-900/80 border border-gray-800/90 p-5 shadow-lg flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
            <Award className="w-6 h-6" />
          </div>
          <div>
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400 block">Acceptance Rate</span>
            <span className="text-2xl font-black text-white font-mono">{acceptanceRate}%</span>
          </div>
        </div>
      </div>

      {/* Two Column Layout: Contests & Submissions */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left: Active & Upcoming Contests */}
        <div className="lg:col-span-7 space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <Trophy className="w-4 h-4 text-amber-400" />
              <span>Campus Contests</span>
            </h2>
            <Link to="/contests" className="text-xs text-blue-400 hover:underline">
              View All Contests →
            </Link>
          </div>

          {/* Active Live Contests */}
          {activeContests.length > 0 && (
            <div className="space-y-3">
              <span className="text-[11px] font-bold uppercase tracking-wider text-emerald-400 block">
                Live Now
              </span>
              {activeContests.map(c => (
                <div key={c.id} className="rounded-2xl bg-gray-900/90 border border-emerald-500/30 p-5 shadow-xl flex items-center justify-between gap-4">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
                      <h3 className="text-sm font-bold text-white">{c.title}</h3>
                    </div>
                    <p className="text-xs text-gray-400 line-clamp-1">{c.description}</p>
                    <div className="flex items-center gap-4 text-[11px] text-gray-400 font-mono">
                      <span>Duration: {c.durationMinutes} mins</span>
                      <span>•</span>
                      <span>{c.problemCount || 0} Problems</span>
                    </div>
                  </div>
                  <Link
                    to={`/contests/${c.id}`}
                    className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs inline-flex items-center gap-1.5 transition-all shadow-md shrink-0"
                  >
                    <span>Enter Contest</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Link>
                </div>
              ))}
            </div>
          )}

          {/* Upcoming Contests */}
          {upcomingContests.length > 0 && (
            <div className="space-y-3">
              <span className="text-[11px] font-bold uppercase tracking-wider text-blue-400 block">
                Upcoming Challenges
              </span>
              {upcomingContests.map(c => (
                <div key={c.id} className="rounded-2xl bg-gray-900/90 border border-gray-800 p-5 shadow-lg flex items-center justify-between gap-4">
                  <div className="space-y-1">
                    <h3 className="text-sm font-bold text-white">{c.title}</h3>
                    <p className="text-xs text-gray-400 line-clamp-1">{c.description}</p>
                    <div className="flex items-center gap-4 text-[11px] text-gray-400 font-mono">
                      <span className="flex items-center gap-1">
                        <Clock className="w-3 h-3 text-blue-400" />
                        Starts: {new Date(c.startTime).toLocaleDateString()} {new Date(c.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                  </div>
                  <Link
                    to={`/contests/${c.id}`}
                    className="px-4 py-2 rounded-xl bg-gray-800 hover:bg-gray-700 text-white font-semibold text-xs inline-flex items-center gap-1.5 transition-all border border-gray-700 shrink-0"
                  >
                    <span>Details & Register</span>
                  </Link>
                </div>
              ))}
            </div>
          )}

          {/* Completed Contests (Recent) */}
          {completedContests.length > 0 && (
            <div className="space-y-3">
              <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400 block">
                Concluded Contests (Results & Analytics Ready)
              </span>
              {completedContests.slice(0, 2).map(c => (
                <div key={c.id} className="rounded-2xl bg-gray-900/60 border border-gray-800 p-4 shadow-sm flex items-center justify-between gap-4">
                  <div>
                    <h4 className="text-xs font-bold text-gray-200">{c.title}</h4>
                    <span className="text-[10px] text-gray-500 font-mono">
                      {new Date(c.endTime).toLocaleDateString()}
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Link
                      to={`/contests/${c.id}/results`}
                      className="px-3 py-1.5 rounded-lg bg-blue-600/20 hover:bg-blue-600/30 text-blue-400 text-xs font-semibold border border-blue-500/30 transition-colors"
                    >
                      View Results
                    </Link>
                    <Link
                      to={`/contests/${c.id}/analytics`}
                      className="px-3 py-1.5 rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-300 text-xs font-semibold border border-gray-700 transition-colors"
                    >
                      Analytics
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Right: Recent Submissions Log */}
        <div className="lg:col-span-5 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <Clock className="w-4 h-4 text-blue-400" />
              <span>Recent Submissions</span>
            </h2>
            <Link to="/submissions" className="text-xs text-blue-400 hover:underline">
              Full History →
            </Link>
          </div>

          <div className="rounded-2xl bg-gray-900/90 border border-gray-800 overflow-hidden shadow-xl">
            {submissions.length === 0 ? (
              <div className="p-8 text-center text-gray-500 text-xs font-sans">
                No submissions yet. Solve your first problem to track your per-problem solve times!
              </div>
            ) : (
              <div className="divide-y divide-gray-800/80">
                {submissions.slice(0, 6).map(sub => (
                  <div key={sub.id} className="p-3.5 hover:bg-gray-800/40 transition-colors flex items-center justify-between gap-3">
                    <div className="space-y-0.5">
                      <Link
                        to={`/problems/${sub.problemId}`}
                        className="text-xs font-semibold text-white hover:text-blue-400 transition-colors line-clamp-1"
                      >
                        {sub.problemTitle || `Problem #${sub.problemId}`}
                      </Link>
                      <div className="flex items-center gap-2 text-[10px] text-gray-400 font-mono">
                        <span className="uppercase">{sub.language}</span>
                        {sub.runtime != null && <span>• {sub.runtime}ms</span>}
                        <span>• {new Date(sub.createdAt).toLocaleDateString()}</span>
                      </div>
                    </div>
                    <div className="shrink-0">
                      <StatusBadge status={sub.status} />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
