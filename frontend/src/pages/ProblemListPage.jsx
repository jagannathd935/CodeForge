import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { DifficultyBadge } from '../components/Badges';
import { Search, ChevronDown, CheckCircle2 } from 'lucide-react';

export const ProblemListPage = () => {
  const [problems, setProblems] = useState([]);
  const [topics, setTopics] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedDifficulty, setSelectedDifficulty] = useState('ALL');
  const [selectedTopic, setSelectedTopic] = useState('ALL');

  useEffect(() => {
    fetchTopics();
  }, []);

  useEffect(() => {
    fetchProblems();
  }, [search, selectedDifficulty, selectedTopic]);

  const fetchTopics = async () => {
    try {
      const res = await api.get('/topics');
      setTopics(res.data.data || []);
    } catch (err) {
      console.error('Failed to load topics', err);
    }
  };

  const fetchProblems = async () => {
    setLoading(true);
    try {
      const params = {};
      if (search) params.search = search;
      if (selectedDifficulty !== 'ALL') params.difficulty = selectedDifficulty;
      if (selectedTopic !== 'ALL') params.topic = selectedTopic;

      const res = await api.get('/problems', { params });
      setProblems(res.data.data || []);
    } catch (err) {
      console.error('Failed to load problems', err);
    } finally {
      setLoading(false);
    }
  };

  const difficulties = [
    { label: 'All', value: 'ALL', activeClass: 'bg-blue-600 text-white font-bold shadow-xs' },
    { label: 'Easy', value: 'EASY', activeClass: 'bg-emerald-600 text-white font-bold shadow-xs' },
    { label: 'Medium', value: 'MEDIUM', activeClass: 'bg-amber-600 text-white font-bold shadow-xs' },
    { label: 'Hard', value: 'HARD', activeClass: 'bg-rose-600 text-white font-bold shadow-xs' }
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 font-sans">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl sm:text-3xl font-bold text-slate-900 tracking-tight">Problem List</h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Explore problems, search and filter by difficulty and topics
        </p>
      </div>

      {/* Filter and Search Bar */}
      <div className="p-4 rounded-xl bg-white border border-slate-200 mb-6 flex flex-col md:flex-row gap-4 items-center justify-between shadow-xs">
        {/* Search input */}
        <div className="relative w-full md:w-96">
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
            <Search className="w-4 h-4" />
          </div>
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search problems by title, topic or keyword..."
            className="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs sm:text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:border-blue-600 focus:bg-white transition-all font-medium"
          />
        </div>

        {/* Filters: Difficulty Pills + Topic Dropdown */}
        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto justify-start md:justify-end">
          {/* Difficulty pills */}
          <div className="flex items-center p-1 bg-slate-100 rounded-lg border border-slate-200 text-xs font-semibold">
            {difficulties.map((diff) => (
              <button
                key={diff.value}
                onClick={() => setSelectedDifficulty(diff.value)}
                className={`px-3 py-1.5 rounded-md transition-all ${
                  selectedDifficulty === diff.value
                    ? diff.activeClass
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {diff.label}
              </button>
            ))}
          </div>

          {/* Topics dropdown */}
          <div className="relative">
            <select
              value={selectedTopic}
              onChange={(e) => setSelectedTopic(e.target.value)}
              className="appearance-none px-4 py-2 pr-8 bg-slate-50 border border-slate-200 rounded-lg text-xs text-slate-700 hover:text-slate-900 focus:outline-none focus:border-blue-600 focus:bg-white cursor-pointer transition-all font-semibold"
            >
              <option value="ALL">Topics</option>
              {topics.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
            <div className="absolute inset-y-0 right-0 pr-2.5 flex items-center pointer-events-none text-slate-400">
              <ChevronDown className="w-3.5 h-3.5" />
            </div>
          </div>
        </div>
      </div>

      {/* Problems Table */}
      <div className="rounded-xl overflow-hidden bg-white border border-slate-200 shadow-xs">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-700">
            <thead className="bg-slate-50 text-xs text-slate-600 font-bold border-b border-slate-200">
              <tr>
                <th className="py-3 px-4 w-16 text-center">#</th>
                <th className="py-3 px-4">Title</th>
                <th className="py-3 px-4 w-32">Difficulty</th>
                <th className="py-3 px-4">Topics</th>
                <th className="py-3 px-4 w-32 text-center">Acceptance</th>
                <th className="py-3 px-4 w-28 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-xs">
              {loading ? (
                <tr>
                  <td colSpan="6" className="py-12 text-center text-slate-500 text-sm">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <div className="w-5 h-5 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                      <span>Loading problems...</span>
                    </div>
                  </td>
                </tr>
              ) : problems.length === 0 ? (
                <tr>
                  <td colSpan="6" className="py-12 text-center text-slate-500 text-sm font-medium">
                    No problems found matching your filters.
                  </td>
                </tr>
              ) : (
                problems.map((problem) => (
                  <tr
                    key={problem.id}
                    className="hover:bg-slate-50/80 transition-colors"
                  >
                    {/* # */}
                    <td className="py-3 px-4 text-center font-mono text-slate-500 font-medium">
                      {problem.isSolved ? (
                        <CheckCircle2 className="w-4 h-4 text-emerald-600 mx-auto" title="Solved" />
                      ) : (
                        problem.id
                      )}
                    </td>

                    {/* Title */}
                    <td className="py-3 px-4 font-semibold text-slate-900 hover:text-blue-600 transition-colors">
                      <Link to={`/problems/${problem.id}`} className="hover:underline">
                        {problem.title}
                      </Link>
                    </td>

                    {/* Difficulty */}
                    <td className="py-3 px-4">
                      <DifficultyBadge difficulty={problem.difficulty} />
                    </td>

                    {/* Topics */}
                    <td className="py-3 px-4">
                      <div className="flex flex-wrap gap-1.5">
                        {problem.topics?.map((topic) => (
                          <span
                            key={topic}
                            className="px-2 py-0.5 rounded text-[11px] font-semibold bg-slate-100 text-slate-700 border border-slate-200"
                          >
                            {topic}
                          </span>
                        ))}
                      </div>
                    </td>

                    {/* Acceptance */}
                    <td className="py-3 px-4 text-center font-mono text-slate-700 font-medium">
                      {problem.acceptanceRate ? `${problem.acceptanceRate}%` : '50.0%'}
                    </td>

                    {/* Actions */}
                    <td className="py-3 px-4 text-center">
                      <Link
                        to={`/problems/${problem.id}`}
                        className="inline-block px-4 py-1.5 rounded-lg text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white transition-colors shadow-xs"
                      >
                        Solve
                      </Link>
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
