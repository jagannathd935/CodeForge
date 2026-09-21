import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { DifficultyBadge } from '../components/Badges';
import {
  Trophy,
  Clock,
  Calendar,
  Users,
  CheckCircle2,
  XCircle,
  ArrowRight,
  ArrowLeft,
  Award,
  Sparkles,
  ShieldCheck,
  Zap,
  Lock,
  BarChart3,
  FileText
} from 'lucide-react';

export const ContestDetailPage = () => {
  const { id } = useParams();
  const { isAuthenticated, user } = useAuth();

  const [contest, setContest] = useState(null);
  const [standings, setStandings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('problems'); // 'problems' | 'standings'
  const [registering, setRegistering] = useState(false);
  const [registerSuccess, setRegisterSuccess] = useState(false);
  const [timeLeft, setTimeLeft] = useState('');

  useEffect(() => {
    fetchContest();
    fetchStandings();
  }, [id]);

  useEffect(() => {
    if (!contest) return;

    const timer = setInterval(() => {
      const now = new Date();
      if (contest.status === 'ACTIVE') {
        const diff = new Date(contest.endTime) - now;
        if (diff <= 0) {
          setTimeLeft('00:00:00');
        } else {
          const h = String(Math.floor(diff / (1000 * 60 * 60))).padStart(2, '0');
          const m = String(Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))).padStart(2, '0');
          const s = String(Math.floor((diff % (1000 * 60)) / 1000)).padStart(2, '0');
          setTimeLeft(`${h}:${m}:${s}`);
        }
      } else if (contest.status === 'UPCOMING') {
        const diff = new Date(contest.startTime) - now;
        if (diff <= 0) {
          setTimeLeft('Starting now');
        } else {
          const h = String(Math.floor(diff / (1000 * 60 * 60))).padStart(2, '0');
          const m = String(Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))).padStart(2, '0');
          const s = String(Math.floor((diff % (1000 * 60)) / 1000)).padStart(2, '0');
          setTimeLeft(`${h}:${m}:${s}`);
        }
      }
    }, 1000);

    return () => clearInterval(timer);
  }, [contest]);

  const fetchContest = async () => {
    setLoading(true);
    try {
      const res = await api.get(`/contests/${id}`);
      setContest(res.data.data);
    } catch (err) {
      console.error('Failed to load contest details', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchStandings = async () => {
    try {
      const res = await api.get(`/contests/${id}/leaderboard`);
      setStandings(res.data.data || []);
    } catch (err) {
      console.error('Failed to load standings', err);
    }
  };

  const handleRegister = async () => {
    if (!isAuthenticated) {
      alert('Please log in first to register for this contest.');
      return;
    }

    setRegistering(true);
    try {
      await api.post(`/contests/${id}/register`);
      setRegisterSuccess(true);
      fetchContest();
      fetchStandings();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to register');
    } finally {
      setRegistering(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-24 text-center">
        <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
        <p className="text-sm text-gray-400">Loading contest environment...</p>
      </div>
    );
  }

  if (!contest) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-24 text-center text-gray-400">
        <p className="text-lg font-semibold text-white">Contest Not Found</p>
        <Link to="/contests" className="text-xs text-blue-400 hover:underline mt-2 inline-block">
          Return to Contests Catalog
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Back Link */}
      <div className="mb-6">
        <Link
          to="/contests"
          className="inline-flex items-center gap-2 text-xs text-gray-400 hover:text-white transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to All Contests</span>
        </Link>
      </div>

      {/* Main Contest Header Banner */}
      <div className="rounded-3xl glass-panel border border-gray-800 p-6 sm:p-8 mb-8 shadow-2xl relative overflow-hidden">
        <div className="absolute top-0 right-0 w-96 h-96 bg-blue-600/10 rounded-full blur-3xl pointer-events-none -mr-20 -mt-20"></div>

        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6 relative z-10">
          <div className="space-y-3 max-w-3xl">
            <div className="flex flex-wrap items-center gap-2.5">
              {contest.status === 'ACTIVE' ? (
                <span className="px-3 py-1 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center gap-1.5 animate-pulse">
                  <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
                  LIVE NOW
                </span>
              ) : contest.status === 'UPCOMING' ? (
                <span className="px-3 py-1 rounded-full text-xs font-bold bg-blue-500/20 text-blue-400 border border-blue-500/30 flex items-center gap-1.5">
                  <Clock className="w-3.5 h-3.5" />
                  UPCOMING
                </span>
              ) : (
                <span className="px-3 py-1 rounded-full text-xs font-bold bg-gray-800 text-gray-400 border border-gray-700">
                  CONCLUDED
                </span>
              )}

              {contest.isRegistered && (
                <span className="px-3 py-1 rounded-full text-xs font-bold bg-purple-500/20 text-purple-400 border border-purple-500/30 flex items-center gap-1.5">
                  <ShieldCheck className="w-3.5 h-3.5" />
                  Registered
                </span>
              )}

              <span className="text-xs text-gray-400 font-mono">
                Duration: {contest.durationMinutes} mins
              </span>
            </div>

            <h1 className="text-2xl sm:text-4xl font-black text-white tracking-tight">
              {contest.title}
            </h1>
            <p className="text-xs sm:text-sm text-gray-300 leading-relaxed">
              {contest.description}
            </p>
          </div>

          {/* Countdown Clock & Registration Box */}
          <div className="flex flex-col items-start lg:items-end gap-3 min-w-[220px]">
            {contest.status === 'ACTIVE' && (
              <div className="p-4 rounded-2xl bg-gray-950/80 border border-emerald-500/30 text-center w-full lg:w-auto">
                <span className="text-[10px] uppercase font-bold tracking-wider text-emerald-400 block mb-1">
                  Time Remaining
                </span>
                <span className="text-3xl font-black text-white font-mono tracking-wider">
                  {timeLeft || '00:00:00'}
                </span>
              </div>
            )}

            {contest.status === 'UPCOMING' && (
              <div className="p-4 rounded-2xl bg-gray-950/80 border border-blue-500/30 text-center w-full lg:w-auto">
                <span className="text-[10px] uppercase font-bold tracking-wider text-blue-400 block mb-1">
                  Starts In
                </span>
                <span className="text-3xl font-black text-white font-mono tracking-wider">
                  {timeLeft || '00:00:00'}
                </span>
              </div>
            )}

            <div className="flex flex-wrap items-center gap-2 mt-2 w-full lg:w-auto justify-start lg:justify-end">
              <Link
                to={`/contests/${id}/results`}
                className="px-3.5 py-2 rounded-xl bg-amber-500/20 hover:bg-amber-500/30 text-amber-300 border border-amber-500/40 text-xs font-bold inline-flex items-center gap-1.5 transition-all shadow-sm"
              >
                <Trophy className="w-3.5 h-3.5" />
                <span>Official Results</span>
              </Link>
              <Link
                to={`/contests/${id}/analytics`}
                className="px-3.5 py-2 rounded-xl bg-purple-500/20 hover:bg-purple-500/30 text-purple-300 border border-purple-500/40 text-xs font-bold inline-flex items-center gap-1.5 transition-all shadow-sm"
              >
                <BarChart3 className="w-3.5 h-3.5" />
                <span>Contest Analytics</span>
              </Link>
            </div>

            {!contest.isRegistered && contest.status !== 'ENDED' && contest.status !== 'COMPLETED' ? (
              <button
                onClick={handleRegister}
                disabled={registering}
                className="w-full lg:w-auto px-6 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs transition-all shadow-[0_0_20px_rgba(59,130,246,0.4)] disabled:opacity-50"
              >
                {registering ? 'Registering...' : 'Register for Contest'}
              </button>
            ) : null}
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-3 border-b border-gray-800 pb-3 mb-6 text-sm font-semibold">
        <button
          onClick={() => setActiveTab('problems')}
          className={`pb-2 px-2 transition-all flex items-center gap-2 border-b-2 ${
            activeTab === 'problems'
              ? 'text-blue-400 border-blue-500 font-bold'
              : 'text-gray-400 border-transparent hover:text-white'
          }`}
        >
          <Zap className="w-4 h-4" />
          <span>Contest Problem Set ({contest.problems?.length || 0})</span>
        </button>

        <button
          onClick={() => setActiveTab('standings')}
          className={`pb-2 px-2 transition-all flex items-center gap-2 border-b-2 ${
            activeTab === 'standings'
              ? 'text-blue-400 border-blue-500 font-bold'
              : 'text-gray-400 border-transparent hover:text-white'
          }`}
        >
          <Trophy className="w-4 h-4" />
          <span>Live Standings ({standings.length})</span>
        </button>
      </div>

      {/* Tab 1: Problems List */}
      {activeTab === 'problems' && (
        contest.status === 'UPCOMING' ? (
          <div className="rounded-2xl glass-card border border-gray-800 p-12 text-center shadow-xl space-y-4">
            <div className="w-16 h-16 rounded-full bg-blue-500/10 border border-blue-500/20 text-blue-400 flex items-center justify-center mx-auto shadow-inner">
              <Lock className="w-8 h-8" />
            </div>
            <h3 className="text-lg font-bold text-white tracking-tight">Contest Problems are Locked</h3>
            <p className="text-xs sm:text-sm text-gray-400 max-w-lg mx-auto leading-relaxed">
              Problem statements will be revealed automatically when the contest begins in{' '}
              <span className="font-mono font-bold text-blue-400">{timeLeft || 'shortly'}</span>. Make sure to register beforehand to participate in live standings!
            </p>
            {contest.isRegistered ? (
              <div className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-semibold">
                <CheckCircle2 className="w-4 h-4" />
                <span>You are registered for this contest</span>
              </div>
            ) : (
              <button
                onClick={handleRegister}
                disabled={registering}
                className="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-bold transition-all shadow-lg"
              >
                {registering ? 'Registering...' : 'Register Now'}
              </button>
            )}
          </div>
        ) : (
          <div className="rounded-2xl overflow-hidden glass-card border border-gray-800 shadow-xl">
            <table className="w-full text-left text-sm text-gray-300">
              <thead className="bg-gray-900/90 text-[11px] uppercase tracking-wider text-gray-400 font-semibold border-b border-gray-800">
                <tr>
                  <th className="py-3.5 px-4 w-14 text-center">#</th>
                  <th className="py-3.5 px-4">Problem Name</th>
                  <th className="py-3.5 px-4 w-28">Difficulty</th>
                  <th className="py-3.5 px-4 w-24 text-center">Points</th>
                  <th className="py-3.5 px-4 w-28 text-center">Status</th>
                  <th className="py-3.5 px-4 w-28 text-center">Solve Time</th>
                  <th className="py-3.5 px-4 w-24 text-center">Attempts</th>
                  <th className="py-3.5 px-4 w-28 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-800/60 font-mono text-xs">
                {contest.problems?.map((prob, idx) => (
                  <tr key={prob.problemId} className="hover:bg-gray-800/40 transition-colors">
                    <td className="py-4 px-4 text-center font-bold text-gray-400">{prob.orderIndex || idx + 1}</td>
                    <td className="py-4 px-4 font-sans font-medium text-white text-sm">
                      <Link
                        to={`/problems/${prob.problemId}?contestId=${contest.id}`}
                        className="hover:text-blue-400 transition-colors flex items-center gap-2"
                      >
                        {prob.title}
                      </Link>
                    </td>
                    <td className="py-4 px-4 font-sans">
                      <DifficultyBadge difficulty={prob.difficulty} />
                    </td>
                    <td className="py-4 px-4 text-center font-bold text-amber-400">
                      {prob.points} pts
                    </td>
                    <td className="py-4 px-4 text-center font-sans">
                      {prob.isSolved || prob.userStatus === 'ACCEPTED' ? (
                        <span className="inline-flex items-center gap-1 text-emerald-400 font-semibold text-xs">
                          <CheckCircle2 className="w-4 h-4" /> Solved
                        </span>
                      ) : prob.userStatus === 'ATTEMPTED' || (prob.attempts && prob.attempts > 0) ? (
                        <span className="inline-flex items-center gap-1 text-rose-400 font-semibold text-xs">
                          <XCircle className="w-4 h-4" /> Attempted
                        </span>
                      ) : (
                        <span className="text-gray-500 text-xs">Unsolved</span>
                      )}
                    </td>
                    <td className="py-4 px-4 text-center text-blue-400 font-bold">
                      {prob.timeTaken || '-'}
                    </td>
                    <td className="py-4 px-4 text-center text-gray-400">
                      {prob.attempts ? `${prob.attempts} tries` : '-'}
                    </td>
                    <td className="py-4 px-4 text-right font-sans">
                      <Link
                        to={`/problems/${prob.problemId}?contestId=${contest.id}`}
                        className="px-3 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-500 text-white font-semibold text-xs inline-flex items-center gap-1.5 transition-all shadow-md"
                      >
                        <span>Solve</span>
                        <ArrowRight className="w-3.5 h-3.5" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}

      {/* Tab 2: Standings / Leaderboard */}
      {activeTab === 'standings' && (
        <div className="rounded-2xl overflow-hidden glass-card border border-gray-800 shadow-xl">
          <table className="w-full text-left text-sm text-gray-300">
            <thead className="bg-gray-900/90 text-[11px] uppercase tracking-wider text-gray-400 font-semibold border-b border-gray-800">
              <tr>
                <th className="py-3.5 px-4 w-20 text-center">Rank</th>
                <th className="py-3.5 px-4">Participant</th>
                <th className="py-3.5 px-4 w-28 text-center">Solved</th>
                <th className="py-3.5 px-4 w-32 text-center">Total Solve Time</th>
                <th className="py-3.5 px-4 w-28 text-center">Score</th>
                <th className="py-3.5 px-4 w-28 text-center">Penalty</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800/60 font-mono text-xs">
              {standings.length === 0 ? (
                <tr>
                  <td colSpan="6" className="py-12 text-center text-gray-500 font-sans">
                    No participants or submissions yet for this contest.
                  </td>
                </tr>
              ) : (
                standings.map((st) => (
                  <tr key={st.userId} className={`hover:bg-gray-800/40 transition-colors ${st.isWinner || st.rank === 1 ? 'bg-amber-500/5' : ''}`}>
                    <td className="py-4 px-4 text-center font-bold">
                      {st.rank === 1 ? (
                        <span className="px-2.5 py-1 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/40">
                          🥇 1
                        </span>
                      ) : st.rank === 2 ? (
                        <span className="px-2.5 py-1 rounded-full bg-gray-400/20 text-gray-300 border border-gray-400/40">
                          🥈 2
                        </span>
                      ) : st.rank === 3 ? (
                        <span className="px-2.5 py-1 rounded-full bg-amber-700/20 text-amber-500 border border-amber-700/40">
                          🥉 3
                        </span>
                      ) : (
                        <span className="text-gray-400">#{st.rank}</span>
                      )}
                    </td>
                    <td className="py-4 px-4 font-sans font-semibold text-white flex items-center gap-3">
                      <div className={`w-8 h-8 rounded-full border flex items-center justify-center text-xs font-black ${
                        st.isWinner || st.rank === 1
                          ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
                          : 'bg-blue-600/30 border-blue-500/40 text-blue-300'
                      }`}>
                        {st.username.substring(0, 2).toUpperCase()}
                      </div>
                      <div className="flex items-center gap-2">
                        <span>{st.username}</span>
                        {(st.isWinner || st.rank === 1) && (
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-black bg-amber-500/20 text-amber-300 border border-amber-500/30">
                            WINNER
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="py-4 px-4 text-center font-bold text-white">
                      {st.problemsSolved != null ? st.problemsSolved : '-'}
                    </td>
                    <td className="py-4 px-4 text-center text-blue-400 font-bold">
                      {st.totalSolveTime || '-'}
                    </td>
                    <td className="py-4 px-4 text-center font-black text-emerald-400 text-sm">
                      {st.score} pts
                    </td>
                    <td className="py-4 px-4 text-center text-gray-400">
                      {st.penaltyMinutes} min
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Rules & Guidelines Card */}
      {contest.rules && (
        <div className="mt-8 p-6 rounded-2xl glass-card border border-gray-800 space-y-3 shadow-lg">
          <div className="flex items-center gap-2 text-blue-400 text-xs font-bold uppercase tracking-wider">
            <FileText className="w-4 h-4" />
            <span>Contest Rules & Penalty Guidelines</span>
          </div>
          <p className="text-xs text-gray-300 leading-relaxed font-mono whitespace-pre-wrap bg-gray-950/80 p-4 rounded-xl border border-gray-800">
            {contest.rules}
          </p>
        </div>
      )}
    </div>
  );
};

export default ContestDetailPage;
