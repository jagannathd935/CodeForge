import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import {
  Trophy,
  Clock,
  Calendar,
  Users,
  ArrowRight,
  PlusCircle,
  X,
  CheckCircle2,
  AlertCircle,
  Zap,
  Trash2
} from 'lucide-react';

export const ContestListPage = () => {
  const { isAuthenticated, isAdmin } = useAuth();
  const navigate = useNavigate();

  const [contests, setContests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL'); // 'ALL' | 'ACTIVE' | 'UPCOMING' | 'ENDED'

  // Create Contest Modal State
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [availableProblems, setAvailableProblems] = useState([]);
  const [creating, setCreating] = useState(false);
  const [formError, setFormError] = useState('');
  const [formSuccess, setFormSuccess] = useState('');

  // Form Fields
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [durationMinutes, setDurationMinutes] = useState(120);
  const [selectedProblems, setSelectedProblems] = useState([]); // [{ problemId, points, orderIndex }]

  useEffect(() => {
    fetchContests();
  }, []);

  const fetchContests = async () => {
    setLoading(true);
    try {
      const res = await api.get('/contests');
      setContests(res.data.data || []);
    } catch (err) {
      console.error('Failed to load contests', err);
    } finally {
      setLoading(false);
    }
  };

  const openCreateModal = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    setFormError('');
    setFormSuccess('');
    setShowCreateModal(true);

    // Set default start/end times: now and +2 hours
    const now = new Date();
    const twoHoursLater = new Date(now.getTime() + 2 * 60 * 60 * 1000);
    const formatDt = (d) => {
      const pad = (n) => String(n).padStart(2, '0');
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    };
    setStartTime(formatDt(now));
    setEndTime(formatDt(twoHoursLater));

    // Fetch available problems for the contest
    try {
      const res = await api.get('/problems');
      const probs = res.data.data || [];
      setAvailableProblems(probs);
      // Auto-select first 3 problems by default if available
      if (probs.length > 0) {
        setSelectedProblems(
          probs.slice(0, 3).map((p, idx) => ({
            problemId: p.id,
            points: (idx + 1) * 100,
            orderIndex: idx + 1,
          }))
        );
      }
    } catch (err) {
      console.error('Failed to load problems', err);
    }
  };

  const toggleProblemSelection = (problemId) => {
    const exists = selectedProblems.some((p) => p.problemId === problemId);
    if (exists) {
      setSelectedProblems(selectedProblems.filter((p) => p.problemId !== problemId));
    } else {
      const order = selectedProblems.length + 1;
      setSelectedProblems([
        ...selectedProblems,
        {
          problemId,
          points: order * 100,
          orderIndex: order,
        },
      ]);
    }
  };

  const updateProblemPoints = (problemId, points) => {
    setSelectedProblems(
      selectedProblems.map((p) =>
        p.problemId === problemId ? { ...p, points: parseInt(points) || 100 } : p
      )
    );
  };

  const handleCreateContest = async (e) => {
    e.preventDefault();
    setFormError('');

    if (!title.trim()) {
      setFormError('Please provide a contest title');
      return;
    }
    if (!startTime || !endTime) {
      setFormError('Start time and end time are required');
      return;
    }
    if (new Date(endTime) <= new Date(startTime)) {
      setFormError('End time must be after start time');
      return;
    }
    if (selectedProblems.length === 0) {
      setFormError('Please select at least 1 problem for this contest');
      return;
    }

    setCreating(true);
    try {
      const payload = {
        title: title.trim(),
        description: description.trim() || 'Official college algorithmic challenge.',
        startTime: startTime + ':00',
        endTime: endTime + ':00',
        durationMinutes: parseInt(durationMinutes) || 120,
        problems: selectedProblems,
      };

      await api.post('/contests', payload);
      setFormSuccess('Contest created successfully!');
      fetchContests();
      setTimeout(() => {
        setShowCreateModal(false);
        setTitle('');
        setDescription('');
      }, 1000);
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to create contest');
    } finally {
      setCreating(false);
    }
  };

  const handleDeleteContest = async (id, e) => {
    e.preventDefault();
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this contest?')) return;

    try {
      await api.delete(`/admin/contests/${id}`);
      fetchContests();
    } catch (err) {
      alert('Failed to delete contest: ' + (err.response?.data?.message || err.message));
    }
  };

  const filteredContests = contests.filter((c) => {
    if (filter === 'ALL') return true;
    return c.status === filter;
  });

  const getStatusBadge = (status) => {
    switch (status) {
      case 'ACTIVE':
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center gap-1.5 animate-pulse">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
            LIVE NOW
          </span>
        );
      case 'UPCOMING':
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-blue-500/20 text-blue-400 border border-blue-500/30 flex items-center gap-1.5">
            <Clock className="w-3 h-3" />
            UPCOMING
          </span>
        );
      case 'ENDED':
      default:
        return (
          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-gray-800 text-gray-400 border border-gray-700">
            CONCLUDED
          </span>
        );
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header Banner */}
      <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="p-1.5 rounded-lg bg-blue-500/10 text-blue-400 border border-blue-500/20">
              <Trophy className="w-4 h-4" />
            </span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">Coding Contests</h1>
          </div>
          <p className="text-xs sm:text-sm text-gray-400">
            Compete in live coding rounds, benchmark your problem-solving speed, and rise on the college leaderboard.
          </p>
        </div>

        {/* Action Controls: Filter & Create Contest */}
        <div className="flex items-center gap-3 self-start md:self-auto flex-wrap">
          {/* Filter Pills */}
          <div className="flex items-center p-1 bg-gray-900 rounded-xl border border-gray-800 text-xs font-medium">
            {['ALL', 'ACTIVE', 'UPCOMING', 'ENDED'].map((tab) => (
              <button
                key={tab}
                onClick={() => setFilter(tab)}
                className={`px-3 py-1.5 rounded-lg transition-all ${
                  filter === tab
                    ? 'bg-blue-600 text-white font-semibold shadow-md'
                    : 'text-gray-400 hover:text-white hover:bg-gray-800/60'
                }`}
              >
                {tab === 'ACTIVE' ? 'Live' : tab.charAt(0) + tab.slice(1).toLowerCase()}
              </button>
            ))}
          </div>

          {/* Create Contest Button - Admin Only */}
          {isAdmin && (
            <button
              onClick={openCreateModal}
              className="flex items-center gap-2 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-semibold text-xs transition-all shadow-[0_0_20px_rgba(59,130,246,0.35)]"
            >
              <PlusCircle className="w-4 h-4" />
              <span>Create Contest</span>
            </button>
          )}
        </div>
      </div>

      {/* Contest Grid */}
      {loading ? (
        <div className="py-24 text-center text-gray-500">
          <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
          <p className="text-sm">Loading contests...</p>
        </div>
      ) : filteredContests.length === 0 ? (
        <div className="p-12 text-center rounded-2xl glass-panel border border-gray-800 text-gray-400 space-y-4">
          <Trophy className="w-12 h-12 text-gray-600 mx-auto" />
          <div>
            <p className="text-base font-semibold text-white">No Contests Available</p>
            <p className="text-xs text-gray-500 mt-1">
              {isAdmin
                ? 'No contests have been created yet. Click below to host your first organization contest!'
                : 'No contests scheduled yet. Your college administrator will announce contests here soon.'}
            </p>
          </div>
          {isAdmin && (
            <button
              onClick={openCreateModal}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-semibold text-xs transition-all shadow-md"
            >
              <PlusCircle className="w-4 h-4" />
              <span>Create First Contest</span>
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
          {filteredContests.map((contest) => (
            <div
              key={contest.id}
              className={`rounded-2xl p-6 border transition-all duration-300 flex flex-col justify-between ${
                contest.status === 'ACTIVE'
                  ? 'bg-gradient-to-b from-gray-900 via-gray-900/90 to-blue-950/20 border-blue-500/40 shadow-[0_0_25px_rgba(59,130,246,0.12)] hover:border-blue-400'
                  : 'bg-gray-900/60 border-gray-800 hover:border-gray-700'
              }`}
            >
              <div>
                {/* Header info */}
                <div className="flex items-center justify-between gap-2 mb-3">
                  {getStatusBadge(contest.status)}
                  <span className="text-xs text-gray-400 font-mono flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5 text-gray-500" />
                    {contest.durationMinutes} mins
                  </span>
                </div>

                {/* Title */}
                <h3 className="text-lg font-bold text-white mb-1.5 hover:text-blue-400 transition-colors">
                  <Link to={`/contests/${contest.id}`}>{contest.title}</Link>
                </h3>

                <p className="text-xs text-gray-400 mb-5 line-clamp-2 leading-relaxed">
                  {contest.description || 'College algorithm and problem solving challenge.'}
                </p>

                {/* Streamlined Stats Row */}
                <div className="flex items-center gap-5 text-xs text-gray-400 border-t border-b border-gray-800/80 py-3 mb-5">
                  <div className="flex items-center gap-1.5">
                    <Zap className="w-3.5 h-3.5 text-amber-400" />
                    <span className="font-semibold text-gray-200">{contest.problemCount || 0}</span> Problems
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Users className="w-3.5 h-3.5 text-blue-400" />
                    <span className="font-semibold text-gray-200">{contest.participantCount || 0}</span> Participants
                  </div>
                  <div className="flex items-center gap-1.5 ml-auto text-[11px] text-gray-400">
                    <Calendar className="w-3 h-3 text-gray-500" />
                    <span>
                      {new Date(contest.startTime).toLocaleDateString([], {
                        month: 'short',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </span>
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center justify-between">
                {isAdmin ? (
                  <button
                    onClick={(e) => handleDeleteContest(contest.id, e)}
                    title="Delete Contest (Admin)"
                    className="p-1.5 rounded-lg text-gray-500 hover:text-red-400 hover:bg-red-500/10 transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                ) : (
                  <div />
                )}

                <Link
                  to={`/contests/${contest.id}`}
                  className={`px-4 py-2 rounded-xl text-xs font-semibold inline-flex items-center gap-2 transition-all ${
                    contest.status === 'ACTIVE'
                      ? 'bg-blue-600 hover:bg-blue-500 text-white shadow-[0_0_15px_rgba(59,130,246,0.35)]'
                      : 'bg-gray-800 hover:bg-gray-700 text-gray-200'
                  }`}
                >
                  <span>{contest.status === 'ACTIVE' ? 'Enter Contest' : 'View Details'}</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Contest Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn">
          <div className="relative w-full max-w-2xl bg-[#0F172A] border border-gray-800 rounded-3xl p-6 sm:p-8 shadow-2xl overflow-y-auto max-h-[90vh]">
            {/* Close Button */}
            <button
              onClick={() => setShowCreateModal(false)}
              className="absolute top-6 right-6 p-2 rounded-xl text-gray-400 hover:text-white hover:bg-gray-800/80 transition-colors"
            >
              <X className="w-5 h-5" />
            </button>

            {/* Modal Header */}
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 rounded-xl bg-blue-600/20 text-blue-400 flex items-center justify-center border border-blue-500/30">
                <Trophy className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-xl font-bold text-white tracking-tight">Create New Contest</h2>
                <p className="text-xs text-gray-400">Schedule a coding contest for your college peers</p>
              </div>
            </div>

            {/* Feedback Alerts */}
            {formError && (
              <div className="mb-5 p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 flex-shrink-0" />
                <span>{formError}</span>
              </div>
            )}
            {formSuccess && (
              <div className="mb-5 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
                <span>{formSuccess}</span>
              </div>
            )}

            {/* Form */}
            <form onSubmit={handleCreateContest} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1">Contest Title *</label>
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="e.g. Spring Algorithm Challenge 2026"
                  className="w-full bg-gray-900 border border-gray-700/80 rounded-xl px-3.5 py-2 text-xs text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 transition-colors"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1">Description</label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Brief overview, eligibility, or rules..."
                  rows={2}
                  className="w-full bg-gray-900 border border-gray-700/80 rounded-xl px-3.5 py-2 text-xs text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 transition-colors"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1">Start Time *</label>
                  <input
                    type="datetime-local"
                    value={startTime}
                    onChange={(e) => setStartTime(e.target.value)}
                    className="w-full bg-gray-900 border border-gray-700/80 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-blue-500 transition-colors"
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1">End Time *</label>
                  <input
                    type="datetime-local"
                    value={endTime}
                    onChange={(e) => setEndTime(e.target.value)}
                    className="w-full bg-gray-900 border border-gray-700/80 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-blue-500 transition-colors"
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1">Duration (mins)</label>
                  <input
                    type="number"
                    min="15"
                    max="1440"
                    value={durationMinutes}
                    onChange={(e) => setDurationMinutes(e.target.value)}
                    className="w-full bg-gray-900 border border-gray-700/80 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-blue-500 transition-colors"
                  />
                </div>
              </div>

              {/* Problem Selection */}
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-2">
                  Select Contest Problems ({selectedProblems.length} selected) *
                </label>
                <div className="max-h-48 overflow-y-auto rounded-xl bg-gray-950 border border-gray-800 p-2 space-y-1.5">
                  {availableProblems.length === 0 ? (
                    <p className="text-xs text-gray-500 p-2 text-center">Loading problem catalog...</p>
                  ) : (
                    availableProblems.map((prob) => {
                      const isSelected = selectedProblems.some((p) => p.problemId === prob.id);
                      const currentItem = selectedProblems.find((p) => p.problemId === prob.id);
                      return (
                        <div
                          key={prob.id}
                          className={`flex items-center justify-between p-2 rounded-lg text-xs transition-colors ${
                            isSelected ? 'bg-blue-950/40 border border-blue-500/30' : 'hover:bg-gray-900/60 border border-transparent'
                          }`}
                        >
                          <label className="flex items-center gap-2.5 cursor-pointer flex-1">
                            <input
                              type="checkbox"
                              checked={isSelected}
                              onChange={() => toggleProblemSelection(prob.id)}
                              className="rounded border-gray-700 text-blue-600 focus:ring-0 focus:ring-offset-0"
                            />
                            <span className="font-medium text-white">{prob.title}</span>
                            <span className="text-[10px] text-gray-400 font-mono">({prob.difficulty})</span>
                          </label>

                          {isSelected && (
                            <div className="flex items-center gap-1.5 ml-2">
                              <span className="text-[10px] text-gray-400">Pts:</span>
                              <input
                                type="number"
                                min="10"
                                max="1000"
                                step="10"
                                value={currentItem?.points || 100}
                                onChange={(e) => updateProblemPoints(prob.id, e.target.value)}
                                className="w-16 bg-gray-900 border border-gray-700 rounded px-2 py-0.5 text-xs text-white text-center"
                              />
                            </div>
                          )}
                        </div>
                      );
                    })
                  )}
                </div>
              </div>

              {/* Submit Buttons */}
              <div className="pt-4 flex items-center justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-gray-400 hover:text-white hover:bg-gray-800 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={creating}
                  className="px-6 py-2 rounded-xl text-xs font-bold bg-blue-600 hover:bg-blue-500 text-white shadow-lg transition-all disabled:opacity-50"
                >
                  {creating ? 'Publishing...' : 'Publish Contest'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
