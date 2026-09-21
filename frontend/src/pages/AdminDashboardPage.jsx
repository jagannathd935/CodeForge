import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { DifficultyBadge } from '../components/Badges';
import {
  Code2,
  LayoutDashboard,
  FolderCode,
  PlusCircle,
  Users,
  Terminal,
  ArrowLeft,
  Trash2,
  Edit2,
  CheckCircle2,
  AlertCircle,
  Bold,
  Italic,
  List,
  Code,
  Eye,
  EyeOff,
  Server,
  Activity,
  Cpu
} from 'lucide-react';

export const AdminDashboardPage = () => {
  // Navigation tabs: 'dashboard' | 'manage' | 'add' | 'users' | 'logs'
  const [activeTab, setActiveTab] = useState('add');
  
  // Problem Form Sub-tabs: 'details' | 'testcases'
  const [problemTab, setProblemTab] = useState('details');

  const [stats, setStats] = useState(null);
  const [problems, setProblems] = useState([]);
  const [usersList, setUsersList] = useState([]);
  const [systemLogs, setSystemLogs] = useState(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  // Form State
  const [editingProblemId, setEditingProblemId] = useState(null);
  const [title, setTitle] = useState('');
  const [difficulty, setDifficulty] = useState('EASY');
  const [topicsStr, setTopicsStr] = useState('');
  const [description, setDescription] = useState('');
  const [constraints, setConstraints] = useState('');
  const [starterCode, setStarterCode] = useState(
    'import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        // Write your code here\n    }\n}'
  );
  const [testCases, setTestCases] = useState([
    { input: '', expectedOutput: '', isHidden: false },
    { input: '', expectedOutput: '', isHidden: true }
  ]);

  useEffect(() => {
    if (activeTab === 'dashboard') fetchStats();
    if (activeTab === 'manage') fetchProblems();
    if (activeTab === 'users') fetchUsers();
    if (activeTab === 'logs') fetchSystemLogs();
  }, [activeTab]);

  const fetchStats = async () => {
    try {
      const res = await api.get('/admin/stats');
      setStats(res.data.data);
    } catch (err) {
      console.error('Failed to load admin stats', err);
    }
  };

  const fetchProblems = async () => {
    setLoading(true);
    try {
      const res = await api.get('/problems');
      setProblems(res.data.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/users');
      setUsersList(res.data.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchSystemLogs = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/logs');
      setSystemLogs(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const resetProblemForm = () => {
    setEditingProblemId(null);
    setTitle('');
    setDifficulty('EASY');
    setTopicsStr('');
    setDescription('');
    setConstraints('');
    setStarterCode(
      'import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        // Write your code here\n    }\n}'
    );
    setTestCases([
      { input: '', expectedOutput: '', isHidden: false },
      { input: '', expectedOutput: '', isHidden: true }
    ]);
    setProblemTab('details');
  };

  const handleEditProblem = (prob) => {
    setEditingProblemId(prob.id);
    setTitle(prob.title);
    setDifficulty(prob.difficulty);
    setTopicsStr(prob.topics ? prob.topics.join(', ') : '');
    setDescription(prob.description || '');
    setConstraints(prob.constraints || '');
    setStarterCode(prob.starterCode || '');
    if (prob.sampleTestCases && prob.sampleTestCases.length > 0) {
      setTestCases(prob.sampleTestCases.map(tc => ({
        input: tc.input || '',
        expectedOutput: tc.expectedOutput || '',
        isHidden: tc.isHidden || false
      })));
    }
    setActiveTab('add');
    setProblemTab('details');
  };

  const handleDeleteProblem = async (id) => {
    if (!window.confirm('Are you sure you want to delete this problem?')) return;
    try {
      await api.delete(`/admin/problems/${id}`);
      setMessage({ type: 'success', text: 'Problem deleted successfully.' });
      fetchProblems();
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to delete problem.' });
    }
  };

  const handleSaveProblem = async (e) => {
    if (e) e.preventDefault();
    if (!title.trim() || !description.trim()) {
      setMessage({ type: 'error', text: 'Please fill in the problem title and description.' });
      setProblemTab('details');
      return;
    }

    const topics = topicsStr
      .split(',')
      .map(s => s.trim())
      .filter(Boolean);

    const validTestCases = testCases.filter(
      tc => tc.input.trim() !== '' || tc.expectedOutput.trim() !== ''
    );

    const payload = {
      title,
      difficulty,
      topics,
      description,
      constraints,
      starterCode,
      testCases: validTestCases
    };

    setSaving(true);
    setMessage({ type: '', text: '' });

    try {
      if (editingProblemId) {
        await api.put(`/admin/problems/${editingProblemId}`, payload);
        setMessage({ type: 'success', text: `Problem "${title}" updated successfully.` });
      } else {
        await api.post('/admin/problems', payload);
        setMessage({ type: 'success', text: `Problem "${title}" created successfully.` });
      }
      resetProblemForm();
      fetchProblems();
      setActiveTab('manage');
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.response?.data?.message || 'Failed to save problem. Please check input values.'
      });
    } finally {
      setSaving(false);
    }
  };

  // Test Case management
  const addTestCaseRow = () => {
    setTestCases([...testCases, { input: '', expectedOutput: '', isHidden: false }]);
  };

  const updateTestCase = (index, field, value) => {
    const updated = [...testCases];
    updated[index][field] = value;
    setTestCases(updated);
  };

  const removeTestCaseRow = (index) => {
    if (testCases.length <= 1) return;
    setTestCases(testCases.filter((_, i) => i !== index));
  };

  // Markdown helper toolbar
  const insertMarkdown = (prefix, suffix = '') => {
    setDescription(prev => prev + `${prefix}${suffix}`);
  };

  // User management
  const handleToggleUserStatus = async (userId) => {
    try {
      const res = await api.patch(`/admin/users/${userId}/toggle-status`);
      setMessage({ type: 'success', text: res.data.message || 'User status updated successfully.' });
      fetchUsers();
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to update user status.' });
    }
  };

  const handleUpdateUserRole = async (userId, newRole) => {
    try {
      const res = await api.patch(`/admin/users/${userId}/role`, { role: newRole });
      setMessage({ type: 'success', text: res.data.message || 'User role updated.' });
      fetchUsers();
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to update user role.' });
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex flex-col md:flex-row bg-[#0B0F19] text-gray-200 font-sans">
      
      {/* LEFT SIDEBAR matching Interface 6 mockup */}
      <div className="w-full md:w-64 bg-[#0D1117] border-r border-gray-800 p-5 flex flex-col justify-between">
        <div className="space-y-6">
          {/* Logo & Subtitle */}
          <div className="space-y-1">
            <div className="flex items-center space-x-2.5 text-white">
              <div className="w-8 h-8 rounded-lg bg-blue-600/20 border border-blue-500/30 flex items-center justify-center text-blue-400">
                <Code2 className="w-5 h-5" />
              </div>
              <span className="text-lg font-bold tracking-tight">CodeForge</span>
            </div>
            <p className="text-[11px] text-gray-400 pl-0.5">Manage problems and test cases</p>
          </div>

          {/* Navigation Items */}
          <nav className="space-y-1.5 text-xs font-semibold">
            <button
              onClick={() => setActiveTab('dashboard')}
              className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl transition-colors text-left ${
                activeTab === 'dashboard'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-gray-400 hover:text-white hover:bg-gray-800/60'
              }`}
            >
              <LayoutDashboard className="w-4 h-4" />
              <span>Dashboard</span>
            </button>

            <button
              onClick={() => setActiveTab('manage')}
              className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl transition-colors text-left ${
                activeTab === 'manage'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-gray-400 hover:text-white hover:bg-gray-800/60'
              }`}
            >
              <FolderCode className="w-4 h-4" />
              <span>Manage Problems</span>
            </button>

            <button
              onClick={() => { setActiveTab('add'); resetProblemForm(); }}
              className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl transition-colors text-left ${
                activeTab === 'add'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-gray-400 hover:text-white hover:bg-gray-800/60'
              }`}
            >
              <PlusCircle className="w-4 h-4" />
              <span>Add Problem</span>
            </button>

            <button
              onClick={() => setActiveTab('users')}
              className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl transition-colors text-left ${
                activeTab === 'users'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-gray-400 hover:text-white hover:bg-gray-800/60'
              }`}
            >
              <Users className="w-4 h-4" />
              <span>Manage Users</span>
            </button>

            <button
              onClick={() => setActiveTab('logs')}
              className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl transition-colors text-left ${
                activeTab === 'logs'
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-gray-400 hover:text-white hover:bg-gray-800/60'
              }`}
            >
              <Terminal className="w-4 h-4" />
              <span>System Logs</span>
            </button>
          </nav>
        </div>

        {/* Back to Site */}
        <div className="pt-6 border-t border-gray-800">
          <Link
            to="/problems"
            className="flex items-center gap-2 text-xs text-gray-400 hover:text-white transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Site</span>
          </Link>
        </div>
      </div>

      {/* MAIN CONTENT AREA */}
      <div className="flex-1 p-6 sm:p-8 overflow-y-auto">
        {message.text && (
          <div
            className={`mb-6 p-4 rounded-xl flex items-center gap-3 text-xs font-medium border ${
              message.type === 'success'
                ? 'bg-emerald-950/30 text-emerald-300 border-emerald-500/40'
                : 'bg-rose-950/30 text-rose-300 border-rose-500/40'
            }`}
          >
            {message.type === 'success' ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-400 flex-shrink-0" />
            ) : (
              <AlertCircle className="w-4 h-4 text-rose-400 flex-shrink-0" />
            )}
            <span>{message.text}</span>
          </div>
        )}

        {/* VIEW 1: ADD PROBLEM / EDIT PROBLEM (Matches Interface 6 Mockup) */}
        {activeTab === 'add' && (
          <div className="max-w-4xl bg-[#0F172A]/90 border border-gray-800 rounded-2xl p-6 sm:p-8 shadow-2xl space-y-6">
            
            {/* Top Bar: Title + Save Problem Button */}
            <div className="flex items-center justify-between pb-4 border-b border-gray-800">
              <h2 className="text-xl font-bold text-white tracking-tight">
                {editingProblemId ? 'Edit Problem' : 'Add New Problem'}
              </h2>
              <button
                type="button"
                onClick={handleSaveProblem}
                disabled={saving}
                className="px-5 py-2 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold shadow-sm transition-colors disabled:opacity-50"
              >
                {saving ? 'Saving...' : 'Save Problem'}
              </button>
            </div>

            {/* Two Tabs: [Problem Details] and [Test Cases] */}
            <div className="flex border-b border-gray-800 gap-6 text-sm font-semibold">
              <button
                type="button"
                onClick={() => setProblemTab('details')}
                className={`pb-3 border-b-2 transition-colors ${
                  problemTab === 'details'
                    ? 'border-blue-500 text-blue-400'
                    : 'border-transparent text-gray-400 hover:text-gray-200'
                }`}
              >
                Problem Details
              </button>
              <button
                type="button"
                onClick={() => setProblemTab('testcases')}
                className={`pb-3 border-b-2 transition-colors ${
                  problemTab === 'testcases'
                    ? 'border-blue-500 text-blue-400'
                    : 'border-transparent text-gray-400 hover:text-gray-200'
                }`}
              >
                Test Cases ({testCases.length})
              </button>
            </div>

            {/* TAB 1: Problem Details */}
            {problemTab === 'details' && (
              <div className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {/* Title */}
                  <div className="md:col-span-2">
                    <label className="block text-xs font-medium text-gray-300 mb-1">Title</label>
                    <input
                      type="text"
                      required
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      placeholder="Enter problem title"
                      className="w-full px-3.5 py-2.5 bg-gray-900 border border-gray-800 rounded-xl text-xs text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
                    />
                  </div>

                  {/* Difficulty */}
                  <div>
                    <label className="block text-xs font-medium text-gray-300 mb-1">Difficulty</label>
                    <select
                      value={difficulty}
                      onChange={(e) => setDifficulty(e.target.value)}
                      className="w-full px-3.5 py-2.5 bg-gray-900 border border-gray-800 rounded-xl text-xs text-white focus:outline-none focus:border-blue-500 cursor-pointer"
                    >
                      <option value="EASY">Easy</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="HARD">Hard</option>
                    </select>
                  </div>
                </div>

                {/* Topics (comma separated) */}
                <div>
                  <label className="block text-xs font-medium text-gray-300 mb-1">Topics (comma separated)</label>
                  <input
                    type="text"
                    value={topicsStr}
                    onChange={(e) => setTopicsStr(e.target.value)}
                    placeholder="e.g. Array, HashMap"
                    className="w-full px-3.5 py-2.5 bg-gray-900 border border-gray-800 rounded-xl text-xs text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
                  />
                </div>

                {/* Description with formatting toolbar matching Interface 6 */}
                <div>
                  <div className="flex items-center justify-between mb-1">
                    <label className="block text-xs font-medium text-gray-300">Description</label>
                    <div className="flex items-center gap-1 bg-gray-900 px-2 py-1 rounded-lg border border-gray-800">
                      <button
                        type="button"
                        onClick={() => insertMarkdown('**', '**')}
                        title="Bold"
                        className="p-1 text-gray-400 hover:text-white rounded"
                      >
                        <Bold className="w-3.5 h-3.5" />
                      </button>
                      <button
                        type="button"
                        onClick={() => insertMarkdown('*', '*')}
                        title="Italic"
                        className="p-1 text-gray-400 hover:text-white rounded"
                      >
                        <Italic className="w-3.5 h-3.5" />
                      </button>
                      <button
                        type="button"
                        onClick={() => insertMarkdown('\n- ')}
                        title="Bullet List"
                        className="p-1 text-gray-400 hover:text-white rounded"
                      >
                        <List className="w-3.5 h-3.5" />
                      </button>
                      <button
                        type="button"
                        onClick={() => insertMarkdown('`', '`')}
                        title="Inline Code"
                        className="p-1 text-gray-400 hover:text-white rounded"
                      >
                        <Code className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                  <textarea
                    rows={6}
                    required
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="Write the problem description..."
                    className="w-full p-3.5 bg-gray-900 border border-gray-800 rounded-xl text-xs text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 leading-relaxed font-sans"
                  />
                </div>

                {/* Constraints */}
                <div>
                  <label className="block text-xs font-medium text-gray-300 mb-1">Constraints (optional)</label>
                  <textarea
                    rows={2}
                    value={constraints}
                    onChange={(e) => setConstraints(e.target.value)}
                    placeholder="e.g. 1 <= nums.length <= 10^5&#10;-10^9 <= nums[i] <= 10^9"
                    className="w-full p-3 bg-gray-900 border border-gray-800 rounded-xl text-xs font-mono text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
                  />
                </div>
              </div>
            )}

            {/* TAB 2: Test Cases */}
            {problemTab === 'testcases' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between text-xs text-gray-400 pb-1">
                  <span>Configure sample and hidden test cases for automated sandbox grading</span>
                  <button
                    type="button"
                    onClick={addTestCaseRow}
                    className="px-3 py-1 bg-blue-600/20 text-blue-400 hover:bg-blue-600 hover:text-white border border-blue-500/30 rounded-lg font-semibold transition-colors"
                  >
                    + Add Test Case
                  </button>
                </div>

                <div className="space-y-3">
                  {testCases.map((tc, idx) => (
                    <div key={idx} className="p-4 bg-gray-900 border border-gray-800 rounded-xl space-y-3">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold font-mono text-gray-300">Case #{idx + 1}</span>
                        <div className="flex items-center gap-3">
                          <label className="flex items-center gap-1.5 text-xs text-gray-400 cursor-pointer">
                            <input
                              type="checkbox"
                              checked={tc.isHidden}
                              onChange={(e) => updateTestCase(idx, 'isHidden', e.target.checked)}
                              className="rounded border-gray-700 text-blue-600 focus:ring-0"
                            />
                            <span>{tc.isHidden ? 'Hidden Test Case' : 'Public Sample'}</span>
                          </label>
                          {testCases.length > 1 && (
                            <button
                              type="button"
                              onClick={() => removeTestCaseRow(idx)}
                              className="text-gray-500 hover:text-rose-400"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          )}
                        </div>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        <div>
                          <label className="block text-[11px] font-medium text-gray-400 mb-1">Standard Input (stdin)</label>
                          <textarea
                            rows={2}
                            value={tc.input}
                            onChange={(e) => updateTestCase(idx, 'input', e.target.value)}
                            placeholder="Input values"
                            className="w-full p-2.5 bg-[#0B0F19] border border-gray-800 rounded-lg text-xs font-mono text-white placeholder-gray-600 focus:outline-none focus:border-blue-500"
                          />
                        </div>
                        <div>
                          <label className="block text-[11px] font-medium text-gray-400 mb-1">Expected Output (stdout)</label>
                          <textarea
                            rows={2}
                            value={tc.expectedOutput}
                            onChange={(e) => updateTestCase(idx, 'expectedOutput', e.target.value)}
                            placeholder="Expected return"
                            className="w-full p-2.5 bg-[#0B0F19] border border-gray-800 rounded-lg text-xs font-mono text-emerald-400 placeholder-gray-600 focus:outline-none focus:border-blue-500"
                          />
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

          </div>
        )}

        {/* VIEW 2: MANAGE PROBLEMS */}
        {activeTab === 'manage' && (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-bold text-white tracking-tight">Problem Bank</h2>
                <p className="text-xs text-gray-400">View, edit and manage problems in the platform catalogue</p>
              </div>
              <button
                onClick={() => { setActiveTab('add'); resetProblemForm(); }}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-xl text-xs font-semibold shadow-sm transition-colors"
              >
                + Add New Problem
              </button>
            </div>

            <div className="rounded-xl overflow-hidden bg-[#0F172A]/80 border border-gray-800 shadow-xl">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm text-gray-300">
                  <thead className="bg-gray-900/90 text-xs text-gray-400 font-semibold border-b border-gray-800">
                    <tr>
                      <th className="py-3 px-4 w-16 text-center">#</th>
                      <th className="py-3 px-4">Title</th>
                      <th className="py-3 px-4 w-28">Difficulty</th>
                      <th className="py-3 px-4">Topics</th>
                      <th className="py-3 px-4 w-32 text-center">Acceptance</th>
                      <th className="py-3 px-4 w-28 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-800/60 text-xs">
                    {loading ? (
                      <tr>
                        <td colSpan="6" className="py-12 text-center text-gray-500 text-sm">
                          Loading problems...
                        </td>
                      </tr>
                    ) : problems.length === 0 ? (
                      <tr>
                        <td colSpan="6" className="py-12 text-center text-gray-400 text-sm">
                          No problems found. Click "Add New Problem" to create your first problem.
                        </td>
                      </tr>
                    ) : (
                      problems.map((prob) => (
                        <tr key={prob.id} className="hover:bg-gray-800/40 transition-colors">
                          <td className="py-3 px-4 text-center font-mono text-gray-400">{prob.id}</td>
                          <td className="py-3 px-4 font-medium text-white">
                            <Link to={`/problems/${prob.id}`} className="hover:underline hover:text-blue-400">
                              {prob.title}
                            </Link>
                          </td>
                          <td className="py-3 px-4">
                            <DifficultyBadge difficulty={prob.difficulty} />
                          </td>
                          <td className="py-3 px-4">
                            <div className="flex flex-wrap gap-1">
                              {prob.topics?.map(t => (
                                <span key={t} className="px-2 py-0.5 rounded bg-gray-800 text-gray-300 text-[11px] border border-gray-700/50">
                                  {t}
                                </span>
                              ))}
                            </div>
                          </td>
                          <td className="py-3 px-4 text-center font-mono">
                            {prob.acceptanceRate ? `${prob.acceptanceRate}%` : '—'}
                          </td>
                          <td className="py-3 px-4 text-right">
                            <div className="flex items-center justify-end gap-2">
                              <button
                                onClick={() => handleEditProblem(prob)}
                                className="p-1.5 text-gray-400 hover:text-blue-400 hover:bg-gray-800 rounded-lg transition-colors"
                                title="Edit Problem"
                              >
                                <Edit2 className="w-4 h-4" />
                              </button>
                              <button
                                onClick={() => handleDeleteProblem(prob.id)}
                                className="p-1.5 text-gray-400 hover:text-rose-400 hover:bg-gray-800 rounded-lg transition-colors"
                                title="Delete Problem"
                              >
                                <Trash2 className="w-4 h-4" />
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* VIEW 3: DASHBOARD OVERVIEW */}
        {activeTab === 'dashboard' && (
          <div className="space-y-6">
            <h2 className="text-xl font-bold text-white tracking-tight">Platform Metrics & Governance</h2>
            
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="p-5 rounded-xl bg-[#0F172A]/80 border border-gray-800">
                <span className="text-xs text-gray-400 uppercase font-semibold block">Total Users</span>
                <span className="text-3xl font-extrabold text-white font-mono block mt-1">
                  {stats?.totalUsers ?? '...'}
                </span>
                <span className="text-[11px] text-gray-500 mt-1 block">
                  {stats?.totalOrganizers || 0} Organizers &bull; {stats?.totalParticipants || 0} Coders
                </span>
              </div>

              <div className="p-5 rounded-xl bg-[#0F172A]/80 border border-gray-800">
                <span className="text-xs text-gray-400 uppercase font-semibold block">Total Problems</span>
                <span className="text-3xl font-extrabold text-white font-mono block mt-1">
                  {stats?.totalProblems ?? '...'}
                </span>
                <span className="text-[11px] text-gray-500 mt-1 block">Active in Bank</span>
              </div>

              <div className="p-5 rounded-xl bg-[#0F172A]/80 border border-gray-800">
                <span className="text-xs text-gray-400 uppercase font-semibold block">Submissions</span>
                <span className="text-3xl font-extrabold text-white font-mono block mt-1">
                  {stats?.totalSubmissions ?? '...'}
                </span>
                <span className="text-[11px] text-gray-500 mt-1 block">
                  {stats?.acceptedSubmissions || 0} Accepted
                </span>
              </div>

              <div className="p-5 rounded-xl bg-[#0F172A]/80 border border-gray-800">
                <span className="text-xs text-gray-400 uppercase font-semibold block">Contests</span>
                <span className="text-3xl font-extrabold text-white font-mono block mt-1">
                  {stats?.totalContests ?? '...'}
                </span>
                <span className="text-[11px] text-gray-500 mt-1 block">
                  {stats?.activeContests || 0} Active Live
                </span>
              </div>
            </div>
          </div>
        )}

        {/* VIEW 4: MANAGE USERS */}
        {activeTab === 'users' && (
          <div className="space-y-6">
            <h2 className="text-xl font-bold text-white tracking-tight">User Management</h2>
            <div className="rounded-xl overflow-hidden bg-[#0F172A]/80 border border-gray-800 shadow-xl">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm text-gray-300">
                  <thead className="bg-gray-900/90 text-xs text-gray-400 font-semibold border-b border-gray-800">
                    <tr>
                      <th className="py-3 px-4 w-16 text-center">#</th>
                      <th className="py-3 px-4">User</th>
                      <th className="py-3 px-4">Email</th>
                      <th className="py-3 px-4">Role</th>
                      <th className="py-3 px-4 w-32 text-center">Status</th>
                      <th className="py-3 px-4 w-36 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-800/60 text-xs">
                    {loading ? (
                      <tr>
                        <td colSpan="6" className="py-12 text-center text-gray-500 text-sm">
                          Loading users...
                        </td>
                      </tr>
                    ) : usersList.length === 0 ? (
                      <tr>
                        <td colSpan="6" className="py-12 text-center text-gray-400 text-sm">
                          No users found.
                        </td>
                      </tr>
                    ) : (
                      usersList.map((u) => (
                        <tr key={u.id} className="hover:bg-gray-800/40 transition-colors">
                          <td className="py-3 px-4 text-center font-mono text-gray-500">{u.id}</td>
                          <td className="py-3 px-4 font-semibold text-white">{u.username}</td>
                          <td className="py-3 px-4 text-gray-400">{u.email}</td>
                          <td className="py-3 px-4">
                            <select
                              value={u.role}
                              onChange={(e) => handleUpdateUserRole(u.id, e.target.value)}
                              className="bg-gray-900 border border-gray-800 rounded px-2 py-1 text-xs text-white focus:outline-none focus:border-blue-500"
                            >
                              <option value="ROLE_ADMIN">ROLE_ADMIN</option>
                              <option value="ROLE_ORGANIZER">ROLE_ORGANIZER</option>
                              <option value="ROLE_PARTICIPANT">ROLE_PARTICIPANT</option>
                            </select>
                          </td>
                          <td className="py-3 px-4 text-center">
                            <span className={`px-2 py-0.5 rounded text-[11px] font-semibold ${
                              u.active ? 'bg-emerald-500/15 text-emerald-400' : 'bg-rose-500/15 text-rose-400'
                            }`}>
                              {u.active ? 'Active' : 'Deactivated'}
                            </span>
                          </td>
                          <td className="py-3 px-4 text-right">
                            <button
                              onClick={() => handleToggleUserStatus(u.id)}
                              className="px-2.5 py-1 rounded bg-gray-800 hover:bg-gray-700 text-gray-300 text-xs font-medium transition-colors"
                            >
                              {u.active ? 'Deactivate' : 'Activate'}
                            </button>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* VIEW 5: SYSTEM LOGS */}
        {activeTab === 'logs' && (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-bold text-white tracking-tight">System Telemetry & Logs</h2>
                <p className="text-xs text-gray-400">Live operational status and execution logs</p>
              </div>
              <button
                onClick={fetchSystemLogs}
                className="px-3.5 py-1.5 rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-200 text-xs font-semibold"
              >
                Refresh
              </button>
            </div>

            {/* Telemetry Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div className="p-4 rounded-xl bg-gray-900/80 border border-gray-800 space-y-1">
                <span className="text-xs text-gray-400 uppercase font-semibold flex items-center gap-1.5">
                  <Server className="w-3.5 h-3.5 text-blue-400" />
                  Database
                </span>
                <span className="text-sm font-bold text-emerald-400 font-mono block">
                  {systemLogs?.databaseStatus || 'ONLINE (MySQL 8.0)'}
                </span>
              </div>

              <div className="p-4 rounded-xl bg-gray-900/80 border border-gray-800 space-y-1">
                <span className="text-xs text-gray-400 uppercase font-semibold flex items-center gap-1.5">
                  <Cpu className="w-3.5 h-3.5 text-purple-400" />
                  Execution Sandbox
                </span>
                <span className="text-sm font-bold text-blue-400 font-mono block">
                  {systemLogs?.executionSandbox || 'ACTIVE (Docker / Sandbox)'}
                </span>
              </div>

              <div className="p-4 rounded-xl bg-gray-900/80 border border-gray-800 space-y-1">
                <span className="text-xs text-gray-400 uppercase font-semibold flex items-center gap-1.5">
                  <Activity className="w-3.5 h-3.5 text-amber-400" />
                  JVM Memory
                </span>
                <span className="text-sm font-bold text-white font-mono block">
                  {systemLogs?.usedMemoryMb ? `${systemLogs.usedMemoryMb} MB / ${systemLogs.totalMemoryMb} MB` : '180 MB / 512 MB'}
                </span>
              </div>
            </div>

            {/* Log Stream */}
            <div className="rounded-xl overflow-hidden bg-[#070A13] border border-gray-800 shadow-xl p-4 font-mono text-xs space-y-2">
              <div className="text-gray-500 text-[11px] pb-2 border-b border-gray-800/80">
                [SYSTEM LOG AUDIT STREAM]
              </div>
              {systemLogs?.logs && systemLogs.logs.length > 0 ? (
                systemLogs.logs.map((item, idx) => (
                  <div key={idx} className="flex items-start gap-2 leading-relaxed">
                    <span className="text-gray-500">[{new Date(item.timestamp).toLocaleTimeString()}]</span>
                    <span className={item.level === 'INFO' ? 'text-emerald-400 font-bold' : 'text-amber-400 font-bold'}>
                      [{item.level}]
                    </span>
                    <span className="text-gray-300">{item.message}</span>
                  </div>
                ))
              ) : (
                <div className="text-gray-500 py-4">
                  No execution warning events logged in the last interval. Sandbox running smoothly.
                </div>
              )}
            </div>
          </div>
        )}

      </div>

    </div>
  );
};
