import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import {
  Trophy,
  Clock,
  Calendar,
  Users,
  PlusCircle,
  X,
  Edit2,
  Trash2,
  CheckCircle2,
  AlertCircle,
  ArrowRight,
  Eye,
  Flag,
  FileCode,
  Sparkles,
  BarChart3,
  Award,
  Search
} from 'lucide-react';
import { DifficultyBadge } from '../components/Badges';

export const OrganizerDashboardPage = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('contests'); // 'contests' | 'problems'
  const [contests, setContests] = useState([]);
  const [availableProblems, setAvailableProblems] = useState([]);
  const [problemSearch, setProblemSearch] = useState('');
  const [loading, setLoading] = useState(true);

  // Modal State for Create / Edit Contest
  const [showModal, setShowModal] = useState(false);
  const [editingContestId, setEditingContestId] = useState(null);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [rules, setRules] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [durationMinutes, setDurationMinutes] = useState(120);
  const [penaltyMinutes, setPenaltyMinutes] = useState(20);
  const [selectedProblems, setSelectedProblems] = useState([]); // [{ problemId, points, orderIndex }]
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  // Modal State for Create / Edit Problem
  const [showProblemModal, setShowProblemModal] = useState(false);
  const [editingProblemId, setEditingProblemId] = useState(null);
  const [probTitle, setProbTitle] = useState('');
  const [probDifficulty, setProbDifficulty] = useState('EASY');
  const [probTopicsStr, setProbTopicsStr] = useState('');
  const [probDescription, setProbDescription] = useState('');
  const [probConstraints, setProbConstraints] = useState('');
  const [probStarterCode, setProbStarterCode] = useState(
    'import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        // Write your solution here\n    }\n}'
  );
  const [probTimeLimit, setProbTimeLimit] = useState(1000);
  const [probMemoryLimit, setProbMemoryLimit] = useState(256);
  const [probTestCases, setProbTestCases] = useState([
    { input: '', expectedOutput: '', isHidden: false },
    { input: '', expectedOutput: '', isHidden: true }
  ]);
  const [probSaving, setProbSaving] = useState(false);
  const [probMessage, setProbMessage] = useState({ type: '', text: '' });

  useEffect(() => {
    fetchMyContests();
    fetchProblems();
  }, []);

  const fetchMyContests = async () => {
    setLoading(true);
    try {
      const res = await api.get('/organizer/contests');
      setContests(res.data.data || []);
    } catch (err) {
      console.error('Failed to load organizer contests', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchProblems = async () => {
    try {
      const res = await api.get('/problems');
      setAvailableProblems(res.data.data || []);
    } catch (err) {
      console.error('Failed to load problems', err);
    }
  };

  const openCreateModal = () => {
    setEditingContestId(null);
    setTitle('');
    setDescription('');
    setRules('1. Individual participation only.\n2. Standard CodeForge per-problem solve time tracking enabled.\n3. Each wrong submission adds penalty minutes.');
    const now = new Date();
    now.setMinutes(now.getMinutes() + 15);
    const end = new Date(now.getTime() + 120 * 60000);
    setStartTime(now.toISOString().slice(0, 16));
    setEndTime(end.toISOString().slice(0, 16));
    setDurationMinutes(120);
    setPenaltyMinutes(20);
    setSelectedProblems([]);
    setMessage({ type: '', text: '' });
    setShowModal(true);
  };

  const openEditModal = async (c) => {
    setEditingContestId(c.id);
    setTitle(c.title);
    setDescription(c.description || '');
    setRules(c.rules || '');
    setStartTime(c.startTime ? c.startTime.slice(0, 16) : '');
    setEndTime(c.endTime ? c.endTime.slice(0, 16) : '');
    setDurationMinutes(c.durationMinutes || 120);
    setPenaltyMinutes(c.penaltyMinutesPerWrong || 20);

    try {
      const detailRes = await api.get(`/contests/${c.id}`);
      const detail = detailRes.data.data;
      if (detail && detail.problems) {
        setSelectedProblems(
          detail.problems.map((p, idx) => ({
            problemId: p.problemId,
            points: p.points || 100,
            orderIndex: p.orderIndex || idx + 1
          }))
        );
      }
    } catch (err) {
      setSelectedProblems([]);
    }

    setMessage({ type: '', text: '' });
    setShowModal(true);
  };

  const toggleProblemSelection = (problemId) => {
    if (selectedProblems.some(p => p.problemId === problemId)) {
      setSelectedProblems(selectedProblems.filter(p => p.problemId !== problemId));
    } else {
      setSelectedProblems([
        ...selectedProblems,
        { problemId, points: 100, orderIndex: selectedProblems.length + 1 }
      ]);
    }
  };

  const updateProblemPoints = (problemId, points) => {
    setSelectedProblems(
      selectedProblems.map(p =>
        p.problemId === problemId ? { ...p, points: parseInt(points) || 100 } : p
      )
    );
  };

  const handleSaveContest = async (e) => {
    e.preventDefault();
    if (!title.trim() || !startTime || !endTime) {
      setMessage({ type: 'error', text: 'Title, start time, and end time are required' });
      return;
    }

    setSaving(true);
    setMessage({ type: '', text: '' });

    const payload = {
      title,
      description,
      rules,
      startTime: new Date(startTime).toISOString(),
      endTime: new Date(endTime).toISOString(),
      durationMinutes: parseInt(durationMinutes) || 120,
      penaltyMinutesPerWrong: parseInt(penaltyMinutes) || 20,
      problems: selectedProblems.map((p, idx) => ({
        problemId: p.problemId,
        points: p.points,
        orderIndex: idx + 1
      }))
    };

    try {
      if (editingContestId) {
        await api.put(`/organizer/contests/${editingContestId}`, payload);
        setMessage({ type: 'success', text: 'Contest updated successfully!' });
      } else {
        await api.post('/organizer/contests', payload);
        setMessage({ type: 'success', text: 'Contest published successfully!' });
      }
      fetchMyContests();
      setTimeout(() => setShowModal(false), 800);
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.response?.data?.message || 'Failed to save contest'
      });
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteContest = async (id, contestTitle) => {
    if (window.confirm(`Are you sure you want to delete "${contestTitle}"? All associated participant rankings will be removed.`)) {
      try {
        await api.delete(`/organizer/contests/${id}`);
        fetchMyContests();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to delete contest');
      }
    }
  };

  // Problem Authoring Handlers
  const openCreateProblemModal = () => {
    setEditingProblemId(null);
    setProbTitle('');
    setProbDifficulty('EASY');
    setProbTopicsStr('');
    setProbDescription('');
    setProbConstraints('');
    setProbStarterCode(
      'import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        // Write your solution here\n    }\n}'
    );
    setProbTimeLimit(1000);
    setProbMemoryLimit(256);
    setProbTestCases([
      { input: '', expectedOutput: '', isHidden: false },
      { input: '', expectedOutput: '', isHidden: true }
    ]);
    setProbMessage({ type: '', text: '' });
    setShowProblemModal(true);
  };

  const openEditProblemModal = async (prob) => {
    setEditingProblemId(prob.id);
    setProbTitle(prob.title);
    setProbDifficulty(prob.difficulty || 'EASY');
    setProbTopicsStr((prob.topics || []).join(', '));
    setProbDescription(prob.description || '');
    setProbConstraints(prob.constraints || '');
    setProbStarterCode(prob.starterCode || '');
    setProbTimeLimit(prob.timeLimit || 1000);
    setProbMemoryLimit(prob.memoryLimit || 256);

    try {
      const tcRes = await api.get(`/problems/${prob.id}/test-cases`);
      const tcs = tcRes.data.data || [];
      if (tcs.length > 0) {
        setProbTestCases(tcs.map(t => ({ input: t.input, expectedOutput: t.expectedOutput, isHidden: t.isHidden })));
      } else {
        setProbTestCases([
          { input: '', expectedOutput: '', isHidden: false },
          { input: '', expectedOutput: '', isHidden: true }
        ]);
      }
    } catch (err) {
      setProbTestCases([
        { input: '', expectedOutput: '', isHidden: false },
        { input: '', expectedOutput: '', isHidden: true }
      ]);
    }

    setProbMessage({ type: '', text: '' });
    setShowProblemModal(true);
  };

  const handleAddProblemTestCase = () => {
    setProbTestCases([...probTestCases, { input: '', expectedOutput: '', isHidden: false }]);
  };

  const handleRemoveProblemTestCase = (index) => {
    setProbTestCases(probTestCases.filter((_, i) => i !== index));
  };

  const handleProblemTestCaseChange = (index, field, value) => {
    const updated = [...probTestCases];
    updated[index][field] = value;
    setProbTestCases(updated);
  };

  const handleSaveProblem = async (e) => {
    e.preventDefault();
    if (!probTitle.trim() || !probDescription.trim()) {
      setProbMessage({ type: 'error', text: 'Problem title and description are required!' });
      return;
    }

    setProbSaving(true);
    setProbMessage({ type: '', text: '' });

    const topics = probTopicsStr
      .split(',')
      .map(t => t.trim())
      .filter(Boolean);

    const validTestCases = probTestCases.filter(
      tc => tc.input.trim() && tc.expectedOutput.trim()
    );

    const payload = {
      title: probTitle,
      difficulty: probDifficulty,
      description: probDescription,
      constraints: probConstraints,
      starterCode: probStarterCode,
      timeLimit: parseInt(probTimeLimit) || 1000,
      memoryLimit: parseInt(probMemoryLimit) || 256,
      topics,
      testCases: validTestCases
    };

    try {
      if (editingProblemId) {
        await api.put(`/problems/${editingProblemId}`, payload);
        setProbMessage({ type: 'success', text: `Problem "${probTitle}" updated successfully!` });
      } else {
        await api.post('/problems', payload);
        setProbMessage({ type: 'success', text: `Problem "${probTitle}" created successfully with ${validTestCases.length} test cases!` });
      }
      fetchProblems();
      setTimeout(() => setShowProblemModal(false), 900);
    } catch (err) {
      setProbMessage({
        type: 'error',
        text: err.response?.data?.message || 'Failed to save problem.'
      });
    } finally {
      setProbSaving(false);
    }
  };

  const handleDeleteProblem = async (id, pTitle) => {
    if (window.confirm(`Are you sure you want to permanently delete problem "${pTitle}"?`)) {
      try {
        await api.delete(`/problems/${id}`);
        fetchProblems();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to delete problem.');
      }
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Organizer Header */}
      <div className="rounded-3xl bg-gradient-to-r from-gray-900 via-gray-900/90 to-amber-950/30 border border-gray-800 p-6 sm:p-8 shadow-2xl relative overflow-hidden">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 relative z-10">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-400 text-xs font-semibold">
              <Flag className="w-3.5 h-3.5" />
              <span>Contest Organizer Studio</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
              Manage Your Campus Contests & Problems
            </h1>
            <p className="text-xs sm:text-sm text-gray-400 max-w-xl">
              Author coding challenges with hidden test cases, host collegiate competitions, configure point weightings, and monitor live solve times.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3 shrink-0">
            <button
              onClick={openCreateModal}
              className="px-4 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-gray-950 font-bold text-xs inline-flex items-center gap-2 transition-all shadow-[0_0_20px_rgba(245,158,11,0.3)]"
            >
              <PlusCircle className="w-4 h-4" />
              <span>Create Contest</span>
            </button>
            <button
              onClick={openCreateProblemModal}
              className="px-4 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs inline-flex items-center gap-2 transition-all shadow-[0_0_20px_rgba(59,130,246,0.3)]"
            >
              <FileCode className="w-4 h-4" />
              <span>Create Problem</span>
            </button>
            <Link
              to="/contests"
              className="px-4 py-2.5 rounded-xl bg-gray-800 hover:bg-gray-700 text-gray-200 font-semibold text-xs inline-flex items-center gap-1.5 transition-all border border-gray-700"
            >
              <Trophy className="w-4 h-4 text-amber-400" />
              <span>All Contests</span>
            </Link>
            <Link
              to="/leaderboard"
              className="px-4 py-2.5 rounded-xl bg-gray-800 hover:bg-gray-700 text-gray-200 font-semibold text-xs inline-flex items-center gap-1.5 transition-all border border-gray-700"
            >
              <Award className="w-4 h-4 text-blue-400" />
              <span>Leaderboard</span>
            </Link>
          </div>
        </div>
      </div>

      {/* KPI Statistic Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <div className="rounded-2xl bg-gray-900/80 border border-gray-800/90 p-5 shadow-lg flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
            <Trophy className="w-6 h-6" />
          </div>
          <div>
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400 block">Hosted Contests</span>
            <span className="text-2xl font-black text-white font-mono">{contests.length}</span>
          </div>
        </div>

        <div className="rounded-2xl bg-gray-900/80 border border-gray-800/90 p-5 shadow-lg flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <Sparkles className="w-6 h-6" />
          </div>
          <div>
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400 block">Live Rounds</span>
            <span className="text-2xl font-black text-emerald-400 font-mono">
              {contests.filter(c => c.status === 'LIVE' || c.status === 'ACTIVE').length}
            </span>
          </div>
        </div>

        <div className="rounded-2xl bg-gray-900/80 border border-gray-800/90 p-5 shadow-lg flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400 block">Total Contestants</span>
            <span className="text-2xl font-black text-white font-mono">
              {contests.reduce((acc, c) => acc + (c.participantCount || 0), 0)}
            </span>
          </div>
        </div>

        <div className="rounded-2xl bg-gray-900/80 border border-gray-800/90 p-5 shadow-lg flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400">
            <FileCode className="w-6 h-6" />
          </div>
          <div>
            <span className="text-[11px] font-bold uppercase tracking-wider text-gray-400 block">Available Problems</span>
            <span className="text-2xl font-black text-white font-mono">{availableProblems.length}</span>
          </div>
        </div>
      </div>

      {/* Studio View Selector Tabs */}
      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-gray-800 pb-2">
        <div className="flex items-center gap-2">
          <button
            onClick={() => setActiveTab('contests')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all inline-flex items-center gap-2 ${
              activeTab === 'contests'
                ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                : 'text-gray-400 hover:text-gray-200 hover:bg-gray-800/40'
            }`}
          >
            <Trophy className="w-3.5 h-3.5" />
            <span>My Contests ({contests.length})</span>
          </button>

          <button
            onClick={() => setActiveTab('problems')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all inline-flex items-center gap-2 ${
              activeTab === 'problems'
                ? 'bg-blue-500/20 text-blue-300 border border-blue-500/40'
                : 'text-gray-400 hover:text-gray-200 hover:bg-gray-800/40'
            }`}
          >
            <FileCode className="w-3.5 h-3.5" />
            <span>Problem Bank ({availableProblems.length})</span>
          </button>
        </div>

        {activeTab === 'problems' && (
          <div className="flex items-center gap-2">
            <div className="relative">
              <Search className="w-3.5 h-3.5 text-gray-500 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={problemSearch}
                onChange={e => setProblemSearch(e.target.value)}
                placeholder="Filter problems..."
                className="pl-8 pr-3 py-1.5 rounded-lg bg-gray-900 border border-gray-800 text-xs text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
              />
            </div>
            <button
              onClick={openCreateProblemModal}
              className="px-3 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-500 text-white font-semibold text-xs inline-flex items-center gap-1.5 transition-colors"
            >
              <PlusCircle className="w-3.5 h-3.5" />
              <span>New Problem</span>
            </button>
          </div>
        )}
      </div>

      {/* TAB 1: Organizer Contests Table */}
      {activeTab === 'contests' && (
        <div className="rounded-2xl bg-gray-900/90 border border-gray-800 overflow-hidden shadow-xl">
          <div className="p-5 border-b border-gray-800 flex items-center justify-between">
            <h2 className="text-sm font-bold text-white flex items-center gap-2">
              <Trophy className="w-4 h-4 text-amber-400" />
              <span>My Organized Contests ({contests.length})</span>
            </h2>
            <span className="text-xs text-gray-400">
              Real-time participant & per-problem tracking
            </span>
          </div>

          {loading ? (
            <div className="p-12 text-center text-gray-400 text-xs font-mono">
              Loading your contest studio...
            </div>
          ) : contests.length === 0 ? (
            <div className="p-12 text-center text-gray-400 text-sm space-y-3">
              <p className="font-semibold text-white">No contests created yet.</p>
              <p className="text-xs text-gray-500">
                Launch your first collegiate contest and start monitoring live solve times.
              </p>
              <button
                onClick={openCreateModal}
                className="px-4 py-2 rounded-xl bg-amber-500 hover:bg-amber-400 text-gray-950 font-bold text-xs inline-flex items-center gap-2"
              >
                <PlusCircle className="w-4 h-4" /> Create Contest
              </button>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs text-gray-300">
                <thead className="bg-gray-950/80 text-[10px] uppercase font-bold text-gray-400 tracking-wider border-b border-gray-800">
                  <tr>
                    <th className="py-3 px-4">Contest</th>
                    <th className="py-3 px-4 w-28 text-center">Status</th>
                    <th className="py-3 px-4 w-32 text-center">Problems</th>
                    <th className="py-3 px-4 w-32 text-center">Participants</th>
                    <th className="py-3 px-4 w-44">Start Time</th>
                    <th className="py-3 px-4 w-60 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-800/60 font-mono">
                  {contests.map(c => (
                    <tr key={c.id} className="hover:bg-gray-800/30 transition-colors">
                      <td className="py-3.5 px-4 font-sans font-semibold text-white">
                        <Link to={`/contests/${c.id}`} className="hover:text-blue-400 transition-colors">
                          {c.title}
                        </Link>
                      </td>
                      <td className="py-3.5 px-4 text-center font-sans">
                        {c.status === 'LIVE' || c.status === 'ACTIVE' ? (
                          <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                            LIVE
                          </span>
                        ) : c.status === 'UPCOMING' ? (
                          <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-blue-500/20 text-blue-400 border border-blue-500/30">
                            UPCOMING
                          </span>
                        ) : (
                          <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-gray-800 text-gray-400 border border-gray-700">
                            CONCLUDED
                          </span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-center font-bold text-gray-300">
                        {c.problemCount || 0}
                      </td>
                      <td className="py-3.5 px-4 text-center font-bold text-blue-400">
                        {c.participantCount || 0}
                      </td>
                      <td className="py-3.5 px-4 text-gray-400 font-sans text-xs">
                        {new Date(c.startTime).toLocaleDateString()} {new Date(c.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </td>
                      <td className="py-3.5 px-4 text-right font-sans space-x-1.5">
                        <Link
                          to={`/contests/${c.id}`}
                          className="px-2.5 py-1.5 rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-300 hover:text-white transition-colors text-xs font-semibold inline-flex items-center gap-1"
                        >
                          <Eye className="w-3 h-3" /> View
                        </Link>
                        <button
                          onClick={() => openEditModal(c)}
                          className="px-2.5 py-1.5 rounded-lg bg-blue-600/20 hover:bg-blue-600/30 text-blue-400 border border-blue-500/30 transition-colors text-xs font-semibold inline-flex items-center gap-1"
                        >
                          <Edit2 className="w-3 h-3" /> Edit
                        </button>
                        <button
                          onClick={() => handleDeleteContest(c.id, c.title)}
                          title="Delete Contest"
                          className="p-1.5 rounded-lg text-gray-400 hover:text-red-400 hover:bg-red-500/10 transition-colors inline-block"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* TAB 2: Problem Bank Management */}
      {activeTab === 'problems' && (
        <div className="rounded-2xl bg-gray-900/90 border border-gray-800 overflow-hidden shadow-xl">
          <div className="p-5 border-b border-gray-800 flex items-center justify-between">
            <h2 className="text-sm font-bold text-white flex items-center gap-2">
              <FileCode className="w-4 h-4 text-blue-400" />
              <span>Available Problem Bank ({availableProblems.length})</span>
            </h2>
            <button
              onClick={openCreateProblemModal}
              className="px-3.5 py-1.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs inline-flex items-center gap-1.5 shadow-md"
            >
              <PlusCircle className="w-3.5 h-3.5" />
              <span>Add Problem</span>
            </button>
          </div>

          {availableProblems.length === 0 ? (
            <div className="p-12 text-center text-gray-400 text-sm space-y-3">
              <p className="font-semibold text-white">No problems in bank yet.</p>
              <p className="text-xs text-gray-500">
                Author your first coding problem with sample and hidden test cases.
              </p>
              <button
                onClick={openCreateProblemModal}
                className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs inline-flex items-center gap-2"
              >
                <PlusCircle className="w-4 h-4" /> Create Problem
              </button>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs text-gray-300">
                <thead className="bg-gray-950/80 text-[10px] uppercase font-bold text-gray-400 tracking-wider border-b border-gray-800">
                  <tr>
                    <th className="py-3 px-4 w-12">#</th>
                    <th className="py-3 px-4">Problem Title</th>
                    <th className="py-3 px-4 w-28">Difficulty</th>
                    <th className="py-3 px-4">Topics</th>
                    <th className="py-3 px-4 w-52 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-800/60 font-mono">
                  {availableProblems
                    .filter(p => !problemSearch || p.title.toLowerCase().includes(problemSearch.toLowerCase()))
                    .map((p, idx) => (
                      <tr key={p.id} className="hover:bg-gray-800/30 transition-colors">
                        <td className="py-3.5 px-4 text-gray-500 font-mono">{idx + 1}</td>
                        <td className="py-3.5 px-4 font-sans font-semibold text-white">
                          <Link to={`/problems/${p.id}`} className="hover:text-blue-400 transition-colors">
                            {p.title}
                          </Link>
                        </td>
                        <td className="py-3.5 px-4 font-sans">
                          <DifficultyBadge difficulty={p.difficulty} />
                        </td>
                        <td className="py-3.5 px-4 font-sans">
                          <div className="flex flex-wrap gap-1">
                            {(p.topics || []).slice(0, 3).map((t, tIdx) => (
                              <span key={tIdx} className="px-2 py-0.5 rounded-md text-[10px] bg-gray-800 text-gray-300 border border-gray-700/60">
                                {t}
                              </span>
                            ))}
                          </div>
                        </td>
                        <td className="py-3.5 px-4 text-right font-sans space-x-1.5">
                          <Link
                            to={`/problems/${p.id}`}
                            className="px-2.5 py-1.5 rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-300 hover:text-white transition-colors text-xs font-semibold inline-flex items-center gap-1"
                          >
                            <Eye className="w-3 h-3" /> Solve
                          </Link>
                          <button
                            onClick={() => openEditProblemModal(p)}
                            className="px-2.5 py-1.5 rounded-lg bg-blue-600/20 hover:bg-blue-600/30 text-blue-400 border border-blue-500/30 transition-colors text-xs font-semibold inline-flex items-center gap-1"
                          >
                            <Edit2 className="w-3 h-3" /> Edit
                          </button>
                          <button
                            onClick={() => handleDeleteProblem(p.id, p.title)}
                            title="Delete Problem"
                            className="p-1.5 rounded-lg text-gray-400 hover:text-red-400 hover:bg-red-500/10 transition-colors inline-block"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </td>
                      </tr>
                    ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Create / Edit Contest Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm overflow-y-auto">
          <div className="rounded-3xl bg-gray-900 border border-gray-800 w-full max-w-2xl p-6 sm:p-8 space-y-6 shadow-2xl relative my-8">
            <div className="flex items-center justify-between border-b border-gray-800 pb-4">
              <h3 className="text-lg font-bold text-white flex items-center gap-2">
                <Flag className="w-5 h-5 text-amber-400" />
                <span>{editingContestId ? 'Edit Contest' : 'Create New Contest'}</span>
              </h3>
              <button
                onClick={() => setShowModal(false)}
                className="text-gray-400 hover:text-white p-1 rounded-lg hover:bg-gray-800 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {message.text && (
              <div
                className={`p-3 rounded-xl text-xs font-semibold ${
                  message.type === 'success'
                    ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                    : 'bg-red-500/10 text-red-400 border border-red-500/30'
                }`}
              >
                {message.text}
              </div>
            )}

            <form onSubmit={handleSaveContest} className="space-y-4 text-xs">
              <div className="space-y-1">
                <label className="text-gray-300 font-semibold block">Contest Title *</label>
                <input
                  type="text"
                  value={title}
                  onChange={e => setTitle(e.target.value)}
                  placeholder="e.g. ACM Inter-Collegiate Algorithmic Cup 2026"
                  required
                  className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white focus:outline-none focus:border-blue-500"
                />
              </div>

              <div className="space-y-1">
                <label className="text-gray-300 font-semibold block">Description</label>
                <textarea
                  value={description}
                  onChange={e => setDescription(e.target.value)}
                  rows={2}
                  placeholder="Overview and eligible campus departments..."
                  className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white focus:outline-none focus:border-blue-500"
                />
              </div>

              <div className="space-y-1">
                <label className="text-gray-300 font-semibold block">Contest Rules & Scoring Policy</label>
                <textarea
                  value={rules}
                  onChange={e => setRules(e.target.value)}
                  rows={2}
                  placeholder="Rules on submissions, penalties, test cases..."
                  className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white focus:outline-none focus:border-blue-500"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="text-gray-300 font-semibold block">Start Time *</label>
                  <input
                    type="datetime-local"
                    value={startTime}
                    onChange={e => setStartTime(e.target.value)}
                    required
                    className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white focus:outline-none focus:border-blue-500"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-gray-300 font-semibold block">End Time *</label>
                  <input
                    type="datetime-local"
                    value={endTime}
                    onChange={e => setEndTime(e.target.value)}
                    required
                    className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white focus:outline-none focus:border-blue-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="text-gray-300 font-semibold block">Duration (Minutes)</label>
                  <input
                    type="number"
                    value={durationMinutes}
                    onChange={e => setDurationMinutes(e.target.value)}
                    min={15}
                    className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white focus:outline-none focus:border-blue-500"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-gray-300 font-semibold block">Penalty per Wrong Attempt (Mins)</label>
                  <input
                    type="number"
                    value={penaltyMinutes}
                    onChange={e => setPenaltyMinutes(e.target.value)}
                    min={0}
                    className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white focus:outline-none focus:border-blue-500"
                  />
                </div>
              </div>

              {/* Problem Selection Section */}
              <div className="space-y-2 pt-2 border-t border-gray-800">
                <label className="text-gray-300 font-semibold block">
                  Select Contest Problems ({selectedProblems.length} selected)
                </label>
                <div className="max-h-48 overflow-y-auto rounded-xl border border-gray-800 divide-y divide-gray-800/80 bg-gray-950/60 p-2 space-y-1">
                  {availableProblems.map(p => {
                    const selected = selectedProblems.find(sp => sp.problemId === p.id);
                    return (
                      <div
                        key={p.id}
                        className={`flex items-center justify-between p-2 rounded-lg transition-colors ${
                          selected ? 'bg-blue-600/10 border border-blue-500/30' : 'hover:bg-gray-800/40'
                        }`}
                      >
                        <div
                          onClick={() => toggleProblemSelection(p.id)}
                          className="flex items-center gap-2 cursor-pointer flex-1"
                        >
                          <input
                            type="checkbox"
                            checked={!!selected}
                            onChange={() => {}}
                            className="rounded border-gray-700 text-blue-600 focus:ring-0"
                          />
                          <span className="font-semibold text-white">{p.title}</span>
                          <DifficultyBadge difficulty={p.difficulty} />
                        </div>

                        {selected && (
                          <div className="flex items-center gap-2">
                            <span className="text-[10px] text-gray-400">Points:</span>
                            <input
                              type="number"
                              value={selected.points}
                              onChange={e => updateProblemPoints(p.id, e.target.value)}
                              className="w-16 px-2 py-1 rounded bg-gray-900 border border-gray-700 text-white text-xs text-center font-mono"
                            />
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-800">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 rounded-xl bg-gray-800 hover:bg-gray-700 text-gray-300 font-semibold text-xs transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="px-5 py-2 rounded-xl bg-amber-500 hover:bg-amber-400 text-gray-950 font-bold text-xs transition-all shadow-md disabled:opacity-50"
                >
                  {saving ? 'Saving...' : editingContestId ? 'Update Contest' : 'Publish Contest'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Create / Edit Problem Modal */}
      {showProblemModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm overflow-y-auto">
          <div className="rounded-3xl bg-gray-900 border border-gray-800 w-full max-w-3xl p-6 sm:p-8 space-y-6 shadow-2xl relative my-8">
            <div className="flex items-center justify-between border-b border-gray-800 pb-4">
              <h3 className="text-lg font-bold text-white flex items-center gap-2">
                <FileCode className="w-5 h-5 text-blue-400" />
                <span>{editingProblemId ? `Edit Problem (#${editingProblemId})` : 'Create New Problem'}</span>
              </h3>
              <button
                onClick={() => setShowProblemModal(false)}
                className="text-gray-400 hover:text-white p-1 rounded-lg hover:bg-gray-800 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {probMessage.text && (
              <div
                className={`p-3 rounded-xl text-xs font-semibold ${
                  probMessage.type === 'success'
                    ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                    : 'bg-red-500/10 text-red-400 border border-red-500/30'
                }`}
              >
                {probMessage.text}
              </div>
            )}

            <form onSubmit={handleSaveProblem} className="space-y-4 text-xs">
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div className="sm:col-span-2 space-y-1">
                  <label className="text-gray-300 font-semibold block">Problem Title *</label>
                  <input
                    type="text"
                    required
                    value={probTitle}
                    onChange={e => setProbTitle(e.target.value)}
                    placeholder="e.g. Valid Parentheses"
                    className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white focus:outline-none focus:border-blue-500"
                  />
                </div>

                <div className="space-y-1">
                  <label className="text-gray-300 font-semibold block">Difficulty</label>
                  <select
                    value={probDifficulty}
                    onChange={e => setProbDifficulty(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white focus:outline-none focus:border-blue-500"
                  >
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-gray-300 font-semibold block">Topics (comma separated)</label>
                <input
                  type="text"
                  value={probTopicsStr}
                  onChange={e => setProbTopicsStr(e.target.value)}
                  placeholder="e.g. Stack, Strings, Hash Table"
                  className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white focus:outline-none focus:border-blue-500"
                />
              </div>

              <div className="space-y-1">
                <label className="text-gray-300 font-semibold block">Description *</label>
                <textarea
                  rows={4}
                  required
                  value={probDescription}
                  onChange={e => setProbDescription(e.target.value)}
                  placeholder="Problem description, input/output requirements, and examples..."
                  className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white font-mono focus:outline-none focus:border-blue-500 leading-relaxed"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div className="space-y-1">
                  <label className="text-gray-300 font-semibold block">Time Limit (ms)</label>
                  <input
                    type="number"
                    value={probTimeLimit}
                    onChange={e => setProbTimeLimit(e.target.value)}
                    min={100}
                    className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white focus:outline-none focus:border-blue-500"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-gray-300 font-semibold block">Memory Limit (MB)</label>
                  <input
                    type="number"
                    value={probMemoryLimit}
                    onChange={e => setProbMemoryLimit(e.target.value)}
                    min={64}
                    className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white focus:outline-none focus:border-blue-500"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-gray-300 font-semibold block">Constraints</label>
                  <input
                    type="text"
                    value={probConstraints}
                    onChange={e => setProbConstraints(e.target.value)}
                    placeholder="1 <= n <= 10^5"
                    className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white font-mono focus:outline-none focus:border-blue-500"
                  />
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-gray-300 font-semibold block">Starter Code Template</label>
                <textarea
                  rows={4}
                  value={probStarterCode}
                  onChange={e => setProbStarterCode(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl bg-gray-950 border border-gray-800 text-white font-mono focus:outline-none focus:border-blue-500"
                />
              </div>

              {/* Test Cases Manager */}
              <div className="space-y-3 pt-2 border-t border-gray-800">
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-gray-300 font-semibold block">Judge Test Cases ({probTestCases.length})</span>
                    <span className="text-[11px] text-gray-500">Provide input and expected stdout</span>
                  </div>
                  <button
                    type="button"
                    onClick={handleAddProblemTestCase}
                    className="px-2.5 py-1 rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-300 text-[11px] font-semibold inline-flex items-center gap-1"
                  >
                    <PlusCircle className="w-3 h-3" /> Add Test Case
                  </button>
                </div>

                <div className="space-y-2 max-h-56 overflow-y-auto pr-1">
                  {probTestCases.map((tc, idx) => (
                    <div key={idx} className="p-3 bg-gray-950 border border-gray-800 rounded-xl space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-gray-400">Test Case #{idx + 1}</span>
                        <div className="flex items-center gap-3">
                          <label className="flex items-center gap-1 text-[11px] text-gray-400 cursor-pointer">
                            <input
                              type="checkbox"
                              checked={tc.isHidden}
                              onChange={e => handleProblemTestCaseChange(idx, 'isHidden', e.target.checked)}
                              className="rounded border-gray-700 text-blue-600 focus:ring-0"
                            />
                            <span>Hidden Case</span>
                          </label>
                          {probTestCases.length > 1 && (
                            <button
                              type="button"
                              onClick={() => handleRemoveProblemTestCase(idx)}
                              className="text-gray-500 hover:text-red-400 transition-colors"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          )}
                        </div>
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                        <textarea
                          rows={2}
                          value={tc.input}
                          onChange={e => handleProblemTestCaseChange(idx, 'input', e.target.value)}
                          placeholder="Input (stdin)..."
                          className="w-full p-2 bg-gray-900 border border-gray-800 rounded-lg text-white font-mono text-[11px] resize-none"
                        />
                        <textarea
                          rows={2}
                          value={tc.expectedOutput}
                          onChange={e => handleProblemTestCaseChange(idx, 'expectedOutput', e.target.value)}
                          placeholder="Expected output (stdout)..."
                          className="w-full p-2 bg-gray-900 border border-gray-800 rounded-lg text-white font-mono text-[11px] resize-none"
                        />
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-800">
                <button
                  type="button"
                  onClick={() => setShowProblemModal(false)}
                  className="px-4 py-2 rounded-xl bg-gray-800 hover:bg-gray-700 text-gray-300 font-semibold text-xs transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={probSaving}
                  className="px-5 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs transition-all shadow-md disabled:opacity-50"
                >
                  {probSaving ? 'Saving...' : editingProblemId ? 'Update Problem' : 'Save Problem'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
