import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axios';
import {
  Trophy,
  Medal,
  Award,
  Clock,
  CheckCircle2,
  XCircle,
  MinusCircle,
  ArrowLeft,
  ChevronDown,
  ChevronUp,
  Users,
  BarChart3,
  Building2,
  Sparkles,
  ShieldCheck,
  Calendar
} from 'lucide-react';

export const ContestResultsPage = () => {
  const { id } = useParams();
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expandedUser, setExpandedUser] = useState(null);

  useEffect(() => {
    fetchResults();
  }, [id]);

  const fetchResults = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get(`/contests/${id}/results`);
      setResults(res.data.data);
      if (res.data.data?.winner) {
        setExpandedUser(res.data.data.winner.username);
      }
    } catch (err) {
      console.error('Failed to load contest results', err);
      setError(err.response?.data?.message || 'Failed to load contest results.');
    } finally {
      setLoading(false);
    }
  };

  const toggleExpand = (username) => {
    setExpandedUser(expandedUser === username ? null : username);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)] bg-[#0B0F19]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <span className="text-xs text-gray-400">Loading official contest results...</span>
        </div>
      </div>
    );
  }

  if (error || !results) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center">
        <div className="p-8 rounded-2xl glass-card border border-gray-800 text-gray-400 space-y-4">
          <Trophy className="w-12 h-12 text-gray-600 mx-auto" />
          <h2 className="text-lg font-bold text-white">Results Unavailable</h2>
          <p className="text-xs text-gray-400">{error || 'No results data found for this contest.'}</p>
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

  const winner = results?.winner;
  const participants = results?.participants || results?.results || [];
  const contestTitle = results?.contestTitle || 'Contest';
  const totalParticipants = results?.totalParticipants || participants.length;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Navigation & Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <Link
            to={`/contests/${id}`}
            className="inline-flex items-center gap-2 text-xs text-gray-400 hover:text-white transition-colors mb-2"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Contest Page</span>
          </Link>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight flex items-center gap-3">
            <span>Official Results: {contestTitle}</span>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30">
              Verified
            </span>
          </h1>
          <p className="text-xs text-gray-400 mt-1">
            Section 10 Official Post-Contest Rankings & Per-Problem Solve-Time Breakdown
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link
            to={`/contests/${id}/analytics`}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-purple-600/20 hover:bg-purple-600/30 text-purple-300 border border-purple-500/30 text-xs font-bold transition-all shadow-sm"
          >
            <BarChart3 className="w-4 h-4" />
            <span>View Contest Analytics</span>
          </Link>
        </div>
      </div>

      {/* Winner Spotlight Card */}
      {winner && (
        <div className="relative rounded-3xl p-6 sm:p-8 bg-gradient-to-br from-amber-950/30 via-gray-900 to-gray-950 border border-amber-500/30 shadow-2xl overflow-hidden">
          <div className="absolute top-0 right-0 w-96 h-96 bg-amber-500/10 rounded-full blur-3xl pointer-events-none -mr-20 -mt-20"></div>

          <div className="flex flex-col md:flex-row items-center justify-between gap-6 relative z-10">
            <div className="flex items-center gap-5">
              <div className="relative">
                <div className="w-20 h-20 sm:w-24 sm:h-24 rounded-2xl bg-gradient-to-tr from-amber-600 to-yellow-400 p-0.5 shadow-xl">
                  <div className="w-full h-full rounded-2xl bg-gray-950 flex items-center justify-center text-amber-400 font-black text-2xl sm:text-3xl">
                    {winner.username.substring(0, 2).toUpperCase()}
                  </div>
                </div>
                <div className="absolute -bottom-2 -right-2 w-8 h-8 rounded-full bg-amber-500 flex items-center justify-center text-gray-950 shadow-md">
                  <Trophy className="w-4 h-4 fill-current" />
                </div>
              </div>

              <div>
                <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/40 text-[11px] font-black uppercase tracking-wider mb-2">
                  <Sparkles className="w-3.5 h-3.5" />
                  CONTEST CHAMPION (RANK #1)
                </div>
                <h2 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
                  {winner.username}
                </h2>
                <p className="text-xs text-gray-400 flex items-center gap-1.5 mt-1">
                  <Building2 className="w-3.5 h-3.5 text-blue-400" />
                  <span>{winner.organization || 'CodeForge Tech'}</span>
                </p>
              </div>
            </div>

            <div className="grid grid-cols-3 gap-4 sm:gap-6 bg-gray-950/80 p-4 sm:p-5 rounded-2xl border border-gray-800 w-full md:w-auto text-center">
              <div>
                <span className="text-[10px] uppercase font-bold text-gray-500 block">Total Score</span>
                <span className="text-xl sm:text-2xl font-black text-emerald-400 font-mono">
                  {winner.score} pts
                </span>
              </div>
              <div className="border-x border-gray-800 px-3 sm:px-6">
                <span className="text-[10px] uppercase font-bold text-gray-500 block">Problems</span>
                <span className="text-xl sm:text-2xl font-black text-white font-mono">
                  {winner.problemsSolved}
                </span>
              </div>
              <div>
                <span className="text-[10px] uppercase font-bold text-gray-500 block">Total Time</span>
                <span className="text-lg sm:text-xl font-bold text-blue-400 font-mono">
                  {winner.totalSolveTime || '0m 00s'}
                </span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Participants Matrix & Solve Time Breakdown */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-white flex items-center gap-2">
            <Medal className="w-4 h-4 text-blue-400" />
            <span>Final Participant Rankings & Solve Times</span>
            <span className="text-xs font-mono text-gray-500">({participants?.length || 0} participants)</span>
          </h2>
          <span className="text-xs text-gray-400">Click any participant row to view their per-problem matrix</span>
        </div>

        <div className="rounded-2xl overflow-hidden glass-card border border-gray-800 shadow-xl">
          <div className="divide-y divide-gray-800/80">
            {participants?.length === 0 ? (
              <div className="p-8 text-center text-gray-500 text-xs">
                No participant records found for this contest.
              </div>
            ) : (
              participants.map((part) => {
                const isExpanded = expandedUser === part.username;
                const isChampion = part.isWinner || part.rank === 1;

                return (
                  <div key={part.userId} className="transition-colors">
                    {/* Main Row */}
                    <div
                      onClick={() => toggleExpand(part.username)}
                      className={`p-4 sm:p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 cursor-pointer hover:bg-gray-800/40 transition-colors ${
                        isChampion ? 'bg-amber-500/5' : ''
                      }`}
                    >
                      <div className="flex items-center gap-4">
                        {/* Rank Badge */}
                        <div className="w-10 text-center font-black">
                          {part.rank === 1 ? (
                            <span className="px-2.5 py-1 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/40 text-xs">
                              🥇 1
                            </span>
                          ) : part.rank === 2 ? (
                            <span className="px-2.5 py-1 rounded-full bg-gray-400/20 text-gray-300 border border-gray-400/40 text-xs">
                              🥈 2
                            </span>
                          ) : part.rank === 3 ? (
                            <span className="px-2.5 py-1 rounded-full bg-amber-700/20 text-amber-500 border border-amber-700/40 text-xs">
                              🥉 3
                            </span>
                          ) : (
                            <span className="text-gray-400 font-mono text-xs">#{part.rank}</span>
                          )}
                        </div>

                        {/* Avatar & User */}
                        <div className="flex items-center gap-3">
                          <div className={`w-9 h-9 rounded-xl flex items-center justify-center text-xs font-black ${
                            isChampion
                              ? 'bg-amber-500/20 border border-amber-500/40 text-amber-300'
                              : 'bg-blue-600/20 border border-blue-500/30 text-blue-300'
                          }`}>
                            {part.username.substring(0, 2).toUpperCase()}
                          </div>

                          <div>
                            <div className="flex items-center gap-2">
                              <span className="font-bold text-white text-sm">{part.username}</span>
                              {isChampion && (
                                <span className="px-2 py-0.5 rounded-full text-[10px] font-black bg-amber-500/20 text-amber-300 border border-amber-500/30">
                                  WINNER
                                </span>
                              )}
                            </div>
                            <span className="text-[11px] text-gray-400 flex items-center gap-1">
                              <Building2 className="w-3 h-3 text-gray-500" />
                              {part.organization || 'CodeForge Tech'}
                            </span>
                          </div>
                        </div>
                      </div>

                      {/* Score Metrics & Toggle */}
                      <div className="flex items-center justify-between sm:justify-end gap-6 sm:gap-8 font-mono text-xs">
                        <div>
                          <span className="text-[10px] uppercase text-gray-500 block">Solved</span>
                          <span className="font-bold text-white">{part.problemsSolved} Solved</span>
                        </div>

                        <div>
                          <span className="text-[10px] uppercase text-gray-500 block">Total Solve Time</span>
                          <span className="font-bold text-blue-400 flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                            {part.totalSolveTime || '0m 00s'}
                          </span>
                        </div>

                        <div>
                          <span className="text-[10px] uppercase text-gray-500 block">Final Score</span>
                          <span className="font-black text-emerald-400 text-sm">
                            {part.score} pts
                          </span>
                        </div>

                        <div className="text-gray-400 hover:text-white">
                          {isExpanded ? <ChevronUp className="w-5 h-5" /> : <ChevronDown className="w-5 h-5" />}
                        </div>
                      </div>
                    </div>

                    {/* Collapsible Per-Problem Breakdown Matrix */}
                    {isExpanded && (
                      <div className="bg-gray-950/70 p-4 sm:p-6 border-t border-gray-800/80">
                        <div className="mb-3 flex items-center justify-between">
                          <h4 className="text-xs font-bold text-gray-300 uppercase tracking-wider flex items-center gap-2">
                            <span>Per-Problem Solve-Time Breakdown Matrix</span>
                          </h4>
                          <span className="text-[11px] text-gray-500">
                            Recorded from contest start to accepted submission
                          </span>
                        </div>

                        <div className="rounded-xl overflow-hidden border border-gray-800 bg-[#0B0F19]">
                          <table className="w-full text-left text-xs font-mono">
                            <thead className="bg-gray-900/90 text-[10px] uppercase tracking-wider text-gray-400 font-semibold border-b border-gray-800">
                              <tr>
                                <th className="py-2.5 px-4">Problem</th>
                                <th className="py-2.5 px-4 w-32">Result</th>
                                <th className="py-2.5 px-4 w-36">Solve Time</th>
                                <th className="py-2.5 px-4 w-28 text-center">Attempts</th>
                                <th className="py-2.5 px-4 w-28 text-right">Points</th>
                              </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-800/60 font-mono">
                              {(!part.problemBreakdown && !part.problemBreakdowns) || (part.problemBreakdown || part.problemBreakdowns).length === 0 ? (
                                <tr>
                                  <td colSpan="5" className="py-4 text-center text-gray-500 font-sans">
                                    No problem submissions recorded.
                                  </td>
                                </tr>
                              ) : (
                                (part.problemBreakdown || part.problemBreakdowns).map((pb) => {
                                  const isAcc = pb.status === 'ACCEPTED';
                                  const isWa = pb.status === 'WRONG_ANSWER' || pb.status === 'ATTEMPTED';

                                  return (
                                    <tr key={pb.problemId} className="hover:bg-gray-800/30">
                                      <td className="py-3 px-4 font-sans font-medium text-white">
                                        {pb.problemTitle}
                                      </td>
                                      <td className="py-3 px-4 font-sans">
                                        {isAcc ? (
                                          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                                            <CheckCircle2 className="w-3.5 h-3.5" />
                                            ACCEPTED
                                          </span>
                                        ) : isWa ? (
                                          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-rose-500/20 text-rose-400 border border-rose-500/30">
                                            <XCircle className="w-3.5 h-3.5" />
                                            FAILED
                                          </span>
                                        ) : (
                                          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-medium bg-gray-800 text-gray-400">
                                            <MinusCircle className="w-3.5 h-3.5" />
                                            UNSOLVED
                                          </span>
                                        )}
                                      </td>
                                      <td className="py-3 px-4 text-blue-400 font-bold">
                                        {isAcc ? pb.solveTime : '-'}
                                      </td>
                                      <td className="py-3 px-4 text-center text-gray-300">
                                        {pb.attempts || 0} {pb.attempts === 1 ? 'try' : 'tries'}
                                      </td>
                                      <td className="py-3 px-4 text-right font-bold text-emerald-400">
                                        +{pb.pointsAwarded || 0}
                                      </td>
                                    </tr>
                                  );
                                })
                              )}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    )}
                  </div>
                );
              })
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContestResultsPage;
