import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axios';
import { DifficultyBadge } from '../components/Badges';
import {
  BarChart3,
  Users,
  Clock,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Zap,
  TrendingUp,
  ArrowLeft,
  Flame,
  Award,
  ShieldCheck,
  Target
} from 'lucide-react';

export const ContestAnalyticsPage = () => {
  const { id } = useParams();
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchAnalytics();
  }, [id]);

  const fetchAnalytics = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get(`/contests/${id}/analytics`);
      setAnalytics(res.data.data);
    } catch (err) {
      console.error('Failed to load contest analytics', err);
      setError(err.response?.data?.message || 'Failed to load contest analytics.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)] bg-[#0B0F19]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-4 border-purple-500 border-t-transparent rounded-full animate-spin"></div>
          <span className="text-xs text-gray-400">Computing contest analytics & solve metrics...</span>
        </div>
      </div>
    );
  }

  if (error || !analytics) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center">
        <div className="p-8 rounded-2xl glass-card border border-gray-800 text-gray-400 space-y-4">
          <BarChart3 className="w-12 h-12 text-gray-600 mx-auto" />
          <h2 className="text-lg font-bold text-white">Analytics Unavailable</h2>
          <p className="text-xs text-gray-400">{error || 'No analytics data available for this contest.'}</p>
          <Link
            to={`/contests/${id}`}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-blue-600 text-white text-xs font-semibold hover:bg-blue-500 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Return to Contest</span>
          </Link>
        </div>
      </div>
    );
  }

  const {
    contestTitle,
    totalParticipants,
    totalSubmissions,
    acceptedSubmissions,
    wrongSubmissions,
    compilationErrors,
    averageSolveTime,
    mostDifficultProblem,
    easiestProblem,
    fastestSolution,
    problemMetrics,
    scoreDistribution
  } = analytics;

  const acceptanceRate =
    totalSubmissions > 0
      ? Math.round((acceptedSubmissions / totalSubmissions) * 100)
      : 0;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header & Back Link */}
      <div>
        <Link
          to={`/contests/${id}`}
          className="inline-flex items-center gap-2 text-xs text-gray-400 hover:text-white transition-colors mb-3"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Contest Page</span>
        </Link>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight flex items-center gap-3">
              <span>{contestTitle} &mdash; Analytics</span>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-purple-500/20 text-purple-300 border border-purple-500/30">
                Insights
              </span>
            </h1>
            <p className="text-xs text-gray-400 mt-1">
              Section 12 Contest Result Analytics: solve rates, problem difficulty metrics, and score distribution
            </p>
          </div>

          <div className="flex items-center gap-3">
            <Link
              to={`/contests/${id}/results`}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-amber-600/20 hover:bg-amber-600/30 text-amber-300 border border-amber-500/30 text-xs font-bold transition-all shadow-sm"
            >
              <Award className="w-4 h-4" />
              <span>View Leaderboard Results</span>
            </Link>
          </div>
        </div>
      </div>

      {/* KPI Stats Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 sm:gap-6">
        <div className="p-5 rounded-2xl bg-gray-900/70 border border-gray-800 relative overflow-hidden">
          <div className="flex items-center justify-between mb-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400">Participants</span>
            <Users className="w-4 h-4 text-blue-400" />
          </div>
          <div className="text-2xl sm:text-3xl font-black text-white font-mono">{totalParticipants}</div>
          <span className="text-[10px] text-gray-500 mt-1 block">Joined contestants</span>
        </div>

        <div className="p-5 rounded-2xl bg-gray-900/70 border border-gray-800 relative overflow-hidden">
          <div className="flex items-center justify-between mb-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400">Total Submissions</span>
            <Target className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-2xl sm:text-3xl font-black text-white font-mono">{totalSubmissions}</div>
          <span className="text-[10px] text-gray-500 mt-1 block">Runs executed during contest</span>
        </div>

        <div className="p-5 rounded-2xl bg-gray-900/70 border border-gray-800 relative overflow-hidden">
          <div className="flex items-center justify-between mb-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400">Acceptance Rate</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl sm:text-3xl font-black text-emerald-400 font-mono">{acceptanceRate}%</div>
          <span className="text-[10px] text-gray-500 mt-1 block">{acceptedSubmissions} accepted of {totalSubmissions}</span>
        </div>

        <div className="p-5 rounded-2xl bg-gray-900/70 border border-gray-800 relative overflow-hidden">
          <div className="flex items-center justify-between mb-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400">Avg Solve Time</span>
            <Clock className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-xl sm:text-2xl font-black text-amber-400 font-mono">{averageSolveTime || 'N/A'}</div>
          <span className="text-[10px] text-gray-500 mt-1 block">Across all accepted solutions</span>
        </div>
      </div>

      {/* Highlights: Easiest vs Hardest Problem */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 sm:gap-6">
        <div className="p-5 rounded-2xl bg-emerald-950/20 border border-emerald-500/30 space-y-2">
          <div className="flex items-center gap-2 text-emerald-400 text-xs font-bold uppercase tracking-wider">
            <Zap className="w-4 h-4" />
            <span>Highest Solve Rate (Easiest)</span>
          </div>
          <p className="text-base sm:text-lg font-bold text-white tracking-tight">
            {easiestProblem || 'All problems solved evenly'}
          </p>
          <span className="text-[11px] text-gray-400 block">
            Most accessible problem with the greatest success percentage
          </span>
        </div>

        <div className="p-5 rounded-2xl bg-rose-950/20 border border-rose-500/30 space-y-2">
          <div className="flex items-center gap-2 text-rose-400 text-xs font-bold uppercase tracking-wider">
            <Flame className="w-4 h-4" />
            <span>Most Challenging Problem</span>
          </div>
          <p className="text-base sm:text-lg font-bold text-white tracking-tight">
            {mostDifficultProblem || 'Evenly balanced challenge'}
          </p>
          <span className="text-[11px] text-gray-400 block">
            Lowest solve rate or required the highest number of attempts
          </span>
        </div>

        <div className="p-5 rounded-2xl bg-blue-950/20 border border-blue-500/30 space-y-2">
          <div className="flex items-center gap-2 text-blue-400 text-xs font-bold uppercase tracking-wider">
            <TrendingUp className="w-4 h-4" />
            <span>Fastest Solution</span>
          </div>
          <p className="text-base sm:text-lg font-bold text-white tracking-tight font-mono">
            {fastestSolution || 'No accepted submissions yet'}
          </p>
          <span className="text-[11px] text-gray-400 block">
            Record speed solve recorded from contest start
          </span>
        </div>
      </div>

      {/* Problem Performance Breakdown Table */}
      <div className="space-y-3">
        <h2 className="text-base font-bold text-white flex items-center gap-2">
          <BarChart3 className="w-4 h-4 text-purple-400" />
          <span>Per-Problem Solve-Rate & Velocity Metrics</span>
        </h2>

        <div className="rounded-2xl overflow-hidden glass-card border border-gray-800 shadow-xl">
          <table className="w-full text-left text-xs font-mono">
            <thead className="bg-gray-900/90 text-[10px] uppercase tracking-wider text-gray-400 font-semibold border-b border-gray-800">
              <tr>
                <th className="py-3 px-4">Problem Name</th>
                <th className="py-3 px-4 w-28">Difficulty</th>
                <th className="py-3 px-4 w-28 text-center">Submissions</th>
                <th className="py-3 px-4 w-28 text-center">Accepted</th>
                <th className="py-3 px-4 w-44">Solve Rate %</th>
                <th className="py-3 px-4 w-32 text-center">Avg Time</th>
                <th className="py-3 px-4 w-32 text-right">Fastest Time</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800/60">
              {(!problemMetrics || problemMetrics.length === 0) ? (
                <tr>
                  <td colSpan="7" className="py-8 text-center text-gray-500 font-sans">
                    No problem metrics available yet.
                  </td>
                </tr>
              ) : (
                problemMetrics.map((pm) => {
                  const rate = Math.round(pm.successPercentage || 0);

                  return (
                    <tr key={pm.problemId} className="hover:bg-gray-800/30 transition-colors">
                      <td className="py-3.5 px-4 font-sans font-medium text-white">
                        <Link to={`/problems/${pm.problemId}`} className="hover:text-blue-400 transition-colors">
                          {pm.problemTitle}
                        </Link>
                      </td>
                      <td className="py-3.5 px-4 font-sans">
                        <DifficultyBadge difficulty={pm.difficulty} />
                      </td>
                      <td className="py-3.5 px-4 text-center text-gray-300">
                        {pm.attempts || 0}
                      </td>
                      <td className="py-3.5 px-4 text-center text-emerald-400 font-bold">
                        {pm.successfulSolutions || 0}
                      </td>
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-2">
                          <div className="flex-1 h-2 bg-gray-800 rounded-full overflow-hidden">
                            <div
                              className={`h-full rounded-full ${
                                rate >= 70
                                  ? 'bg-emerald-500'
                                  : rate >= 40
                                  ? 'bg-blue-500'
                                  : 'bg-rose-500'
                              }`}
                              style={{ width: `${rate}%` }}
                            ></div>
                          </div>
                          <span className="w-9 text-right font-bold text-gray-300 text-[11px]">
                            {rate}%
                          </span>
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-center text-amber-400">
                        {pm.averageSolveTime || '-'}
                      </td>
                      <td className="py-3.5 px-4 text-right text-blue-400 font-bold">
                        {pm.fastestSolveTime || '-'}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Score Distribution Breakdown */}
      {scoreDistribution && Object.keys(scoreDistribution).length > 0 && (
        <div className="p-6 rounded-2xl glass-card border border-gray-800 space-y-4 shadow-xl">
          <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
            <Target className="w-4 h-4 text-blue-400" />
            <span>Score Distribution Across Participants</span>
          </h2>

          <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-3">
            {Object.entries(scoreDistribution).map(([range, count]) => (
              <div
                key={range}
                className="p-3 rounded-xl bg-gray-950/80 border border-gray-800/80 text-center"
              >
                <span className="text-[10px] uppercase font-bold text-gray-500 block font-mono">
                  {range} pts
                </span>
                <span className="text-lg font-black text-white font-mono mt-0.5 block">
                  {count}
                </span>
                <span className="text-[10px] text-gray-500">
                  {count === 1 ? 'user' : 'users'}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default ContestAnalyticsPage;
