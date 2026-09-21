import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import {
  Trophy,
  Medal,
  Award,
  Search,
  Flame,
  CheckCircle2,
  Sparkles,
  TrendingUp,
  User,
  Building2
} from 'lucide-react';

export const LeaderboardPage = () => {
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchLeaderboard();
  }, []);

  const fetchLeaderboard = async () => {
    setLoading(true);
    try {
      const res = await api.get('/users/leaderboard');
      setLeaderboard(res.data.data || []);
    } catch (err) {
      console.error('Failed to load leaderboard', err);
    } finally {
      setLoading(false);
    }
  };

  const filtered = leaderboard.filter((item) =>
    item.username.toLowerCase().includes(search.toLowerCase())
  );

  const topThree = leaderboard.slice(0, 3);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header Banner */}
      <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="p-1.5 rounded-lg bg-amber-500/10 text-amber-400 border border-amber-500/20">
              <Trophy className="w-4 h-4" />
            </span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">College Leaderboard</h1>
          </div>
          <p className="text-xs sm:text-sm text-gray-400">
            Rankings of top competitive programmers, algorithms solved, and DSA mastery across the campus.
          </p>
        </div>

        {/* Search Input */}
        <div className="relative w-full md:w-72">
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
            <Search className="w-4 h-4" />
          </div>
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by student username..."
            className="w-full pl-10 pr-4 py-2 bg-gray-900/90 border border-gray-800 rounded-xl text-xs sm:text-sm text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 transition-all"
          />
        </div>
      </div>

      {/* Top 3 Podium (Only displayed if at least 2 users exist) */}
      {!loading && topThree.length >= 2 && !search && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-10 pt-4">
          {/* Rank 2 (Silver) */}
          {topThree[1] && (
            <div className="order-2 md:order-1 rounded-2xl p-6 bg-gradient-to-b from-gray-900/90 to-gray-950/80 border border-gray-700/60 shadow-xl flex flex-col items-center text-center relative mt-0 md:mt-6">
              <div className="absolute -top-4 px-3 py-1 rounded-full bg-gray-700 text-gray-200 text-xs font-black border border-gray-500 shadow-md">
                🥈 Rank #2
              </div>
              <div className="w-16 h-16 rounded-full bg-gray-800 border-2 border-gray-400 flex items-center justify-center text-xl font-bold text-gray-200 mt-2 mb-3 shadow-lg">
                {topThree[1].username.substring(0, 2).toUpperCase()}
              </div>
              <h3 className="text-base font-bold text-white mb-1">{topThree[1].username}</h3>
              <span className="text-2xl font-black text-white font-mono mb-2">{topThree[1].score} <span className="text-xs text-gray-400 font-sans">pts</span></span>
              <div className="flex items-center gap-2 text-xs text-gray-400">
                <span>{topThree[1].solvedCount} Solved</span>
                <span>•</span>
                <span className="text-emerald-400 font-mono">{topThree[1].acceptanceRate}% Acc</span>
              </div>
            </div>
          )}

          {/* Rank 1 (Gold) */}
          {topThree[0] && (
            <div className="order-1 md:order-2 rounded-2xl p-6 bg-gradient-to-b from-amber-950/30 via-gray-900/90 to-gray-950/90 border border-amber-500/50 shadow-[0_0_35px_rgba(245,158,11,0.2)] flex flex-col items-center text-center relative -mt-2">
              <div className="absolute -top-4 px-4 py-1 rounded-full bg-amber-500 text-black text-xs font-black border border-amber-400 shadow-lg flex items-center gap-1">
                <Sparkles className="w-3.5 h-3.5 fill-black" />
                🥇 Champion #1
              </div>
              <div className="w-20 h-20 rounded-full bg-amber-500/20 border-2 border-amber-400 flex items-center justify-center text-2xl font-black text-amber-300 mt-2 mb-3 shadow-[0_0_20px_rgba(245,158,11,0.4)]">
                {topThree[0].username.substring(0, 2).toUpperCase()}
              </div>
              <h3 className="text-lg font-bold text-white mb-1">{topThree[0].username}</h3>
              <span className="text-3xl font-black text-amber-400 font-mono mb-2">{topThree[0].score} <span className="text-xs text-gray-400 font-sans">pts</span></span>
              <div className="flex items-center gap-2 text-xs text-gray-300">
                <span className="font-semibold text-white">{topThree[0].solvedCount} Solved</span>
                <span>•</span>
                <span className="text-emerald-400 font-mono font-bold">{topThree[0].acceptanceRate}% Acc</span>
              </div>
            </div>
          )}

          {/* Rank 3 (Bronze) */}
          {topThree[2] && (
            <div className="order-3 rounded-2xl p-6 bg-gradient-to-b from-gray-900/90 to-gray-950/80 border border-amber-800/40 shadow-xl flex flex-col items-center text-center relative mt-0 md:mt-8">
              <div className="absolute -top-4 px-3 py-1 rounded-full bg-amber-900/80 text-amber-300 text-xs font-black border border-amber-700 shadow-md">
                🥉 Rank #3
              </div>
              <div className="w-16 h-16 rounded-full bg-amber-950/40 border-2 border-amber-700 flex items-center justify-center text-xl font-bold text-amber-400 mt-2 mb-3 shadow-lg">
                {topThree[2].username.substring(0, 2).toUpperCase()}
              </div>
              <h3 className="text-base font-bold text-white mb-1">{topThree[2].username}</h3>
              <span className="text-2xl font-black text-white font-mono mb-2">{topThree[2].score} <span className="text-xs text-gray-400 font-sans">pts</span></span>
              <div className="flex items-center gap-2 text-xs text-gray-400">
                <span>{topThree[2].solvedCount} Solved</span>
                <span>•</span>
                <span className="text-emerald-400 font-mono">{topThree[2].acceptanceRate}% Acc</span>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Main Leaderboard Table */}
      <div className="rounded-2xl overflow-hidden glass-card border border-gray-800 shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-300">
            <thead className="bg-gray-900/90 text-[11px] uppercase tracking-wider text-gray-400 font-semibold border-b border-gray-800">
              <tr>
                <th className="py-3.5 px-4 w-20 text-center">Rank</th>
                <th className="py-3.5 px-4">Student</th>
                <th className="py-3.5 px-4">College / Organization</th>
                <th className="py-3.5 px-4 w-28 text-center">Solved</th>
                <th className="py-3.5 px-4 w-44 text-center">Tier Breakdown</th>
                <th className="py-3.5 px-4 w-24 text-center">Accuracy</th>
                <th className="py-3.5 px-4 w-28 text-center">Submissions</th>
                <th className="py-3.5 px-4 w-24 text-right font-bold">Score</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800/60 font-mono text-xs">
              {loading ? (
                <tr>
                  <td colSpan="8" className="py-12 text-center text-gray-500 font-sans text-sm">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <div className="w-6 h-6 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
                      <span>Loading college rankings...</span>
                    </div>
                  </td>
                </tr>
              ) : filtered.length === 0 ? (
                <tr>
                  <td colSpan="8" className="py-16 text-center text-gray-400 font-sans">
                    <div className="flex flex-col items-center justify-center space-y-3">
                      <div className="w-12 h-12 rounded-2xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
                        <Trophy className="w-6 h-6" />
                      </div>
                      <p className="text-base font-semibold text-white">
                        {search ? `No students found matching "${search}".` : 'College Leaderboard is currently empty.'}
                      </p>
                      <p className="text-xs text-gray-500 max-w-sm">
                        {search
                          ? 'Try searching with a different username.'
                          : 'Solve coding problems and submit accepted solutions to earn points and claim your spot on the college rankings!'}
                      </p>
                      {!search && (
                        <Link
                          to="/problems"
                          className="mt-2 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-medium text-xs transition-all shadow-md"
                        >
                          Start Solving Problems
                        </Link>
                      )}
                    </div>
                  </td>
                </tr>
              ) : (
                filtered.map((user) => (
                  <tr key={user.userId} className="hover:bg-gray-800/40 transition-colors">
                    <td className="py-4 px-4 text-center font-bold">
                      {user.rank === 1 ? (
                        <span className="px-2.5 py-1 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/40">
                          🥇 1
                        </span>
                      ) : user.rank === 2 ? (
                        <span className="px-2.5 py-1 rounded-full bg-gray-400/20 text-gray-300 border border-gray-400/40">
                          🥈 2
                        </span>
                      ) : user.rank === 3 ? (
                        <span className="px-2.5 py-1 rounded-full bg-amber-700/20 text-amber-500 border border-amber-700/40">
                          🥉 3
                        </span>
                      ) : (
                        <span className="text-gray-500">#{user.rank}</span>
                      )}
                    </td>

                    <td className="py-4 px-4 font-sans font-medium text-white flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-blue-600/20 border border-blue-500/30 flex items-center justify-center text-xs font-bold text-blue-400">
                        {user.username.substring(0, 2).toUpperCase()}
                      </div>
                      <div>
                        <div className="font-semibold text-white">{user.username}</div>
                        <div className="text-[11px] text-gray-400 font-mono">@{user.username.toLowerCase()}</div>
                      </div>
                    </td>

                    <td className="py-4 px-4 font-sans text-xs text-blue-300">
                      <div className="flex items-center gap-1.5">
                        <Building2 className="w-3.5 h-3.5 text-blue-400 flex-shrink-0" />
                        <span className="truncate max-w-[160px]">{user.organization || 'CodeForge Tech'}</span>
                      </div>
                    </td>

                    <td className="py-4 px-4 text-center font-bold text-white text-sm">
                      {user.solvedCount}
                    </td>

                    <td className="py-4 px-4 text-center font-sans">
                      <div className="flex items-center justify-center gap-2 text-[10px] font-mono font-bold">
                        <span className="px-1.5 py-0.5 rounded bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                          E: {user.easySolved}
                        </span>
                        <span className="px-1.5 py-0.5 rounded bg-amber-500/15 text-amber-400 border border-amber-500/30">
                          M: {user.mediumSolved}
                        </span>
                        <span className="px-1.5 py-0.5 rounded bg-rose-500/15 text-rose-400 border border-rose-500/30">
                          H: {user.hardSolved}
                        </span>
                      </div>
                    </td>

                    <td className="py-4 px-4 text-center text-emerald-400 font-semibold">
                      {user.acceptanceRate}%
                    </td>

                    <td className="py-4 px-4 text-center text-gray-400">
                      {user.totalSubmissions}
                    </td>

                    <td className="py-4 px-4 text-right font-black text-amber-400 text-sm">
                      {user.score}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
