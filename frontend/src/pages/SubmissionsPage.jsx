import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { StatusBadge } from '../components/Badges';
import { X, Copy, Check } from 'lucide-react';

export const SubmissionsPage = () => {
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedCode, setSelectedCode] = useState(null);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    fetchSubmissions();
  }, []);

  const fetchSubmissions = async () => {
    setLoading(true);
    try {
      const res = await api.get('/submissions/my');
      setSubmissions(res.data.data || []);
    } catch (err) {
      console.error('Failed to load submissions', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = (text) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 font-sans">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl sm:text-3xl font-bold text-slate-900 tracking-tight">Submission History</h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Track your past submissions and improve over time
        </p>
      </div>

      {/* Table */}
      <div className="rounded-xl overflow-hidden bg-white border border-slate-200 shadow-xs">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-700">
            <thead className="bg-slate-50 text-xs text-slate-600 font-bold border-b border-slate-200">
              <tr>
                <th className="py-3 px-4 w-16 text-center">#</th>
                <th className="py-3 px-4">Problem</th>
                <th className="py-3 px-4 w-28">Language</th>
                <th className="py-3 px-4 w-44">Status</th>
                <th className="py-3 px-4 w-28 text-center">Runtime</th>
                <th className="py-3 px-4 w-28 text-center">Memory</th>
                <th className="py-3 px-4 w-36">Date</th>
                <th className="py-3 px-4 w-20 text-center">Code</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-xs">
              {loading ? (
                <tr>
                  <td colSpan="8" className="py-12 text-center text-slate-500 text-sm">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <div className="w-5 h-5 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                      <span>Loading your submissions...</span>
                    </div>
                  </td>
                </tr>
              ) : submissions.length === 0 ? (
                <tr>
                  <td colSpan="8" className="py-12 text-center text-slate-500 text-sm font-medium">
                    No submissions recorded yet. Solve a problem to start tracking your history!
                  </td>
                </tr>
              ) : (
                submissions.map((sub, index) => (
                  <tr key={sub.id} className="hover:bg-slate-50/80 transition-colors">
                    {/* # */}
                    <td className="py-3 px-4 text-center font-mono text-slate-500 font-medium">
                      {sub.id || index + 1}
                    </td>

                    {/* Problem */}
                    <td className="py-3 px-4 font-semibold text-slate-900 hover:text-blue-600 transition-colors">
                      <Link to={`/problems/${sub.problemId}`} className="hover:underline">
                        {sub.problemTitle}
                      </Link>
                    </td>

                    {/* Language */}
                    <td className="py-3 px-4 text-slate-700 font-mono font-medium">
                      {sub.language}
                    </td>

                    {/* Status */}
                    <td className="py-3 px-4">
                      <StatusBadge status={sub.status} />
                    </td>

                    {/* Runtime */}
                    <td className="py-3 px-4 text-center font-mono text-slate-700 font-medium">
                      {sub.runtime ? `${sub.runtime} ms` : '—'}
                    </td>

                    {/* Memory */}
                    <td className="py-3 px-4 text-center font-mono text-slate-700 font-medium">
                      {sub.memory ? `${Math.round(sub.memory / 1024 * 10) / 10} MB` : '—'}
                    </td>

                    {/* Date */}
                    <td className="py-3 px-4 text-slate-500 text-[11px] font-medium">
                      {new Date(sub.createdAt).toLocaleDateString('en-US', {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric',
                      })}
                    </td>

                    {/* Code button </> */}
                    <td className="py-3 px-4 text-center">
                      <button
                        onClick={() => setSelectedCode(sub)}
                        title="View Submitted Code"
                        className="px-2.5 py-1 rounded bg-slate-100 hover:bg-blue-50 text-slate-700 hover:text-blue-600 font-mono text-xs font-bold border border-slate-300 transition-colors shadow-2xs"
                      >
                        &lt;/&gt;
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Code Viewer Modal */}
      {selectedCode && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs">
          <div className="w-full max-w-3xl bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-2xl flex flex-col max-h-[85vh]">
            <div className="px-6 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
              <div className="flex items-center gap-3">
                <span className="text-sm font-bold text-slate-900">{selectedCode.problemTitle}</span>
                <span className="text-xs font-mono text-slate-500 font-semibold">({selectedCode.language})</span>
                <StatusBadge status={selectedCode.status} />
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handleCopy(selectedCode.code)}
                  className="px-3 py-1.5 rounded-lg bg-white border border-slate-300 hover:bg-slate-100 text-slate-700 font-semibold text-xs flex items-center gap-1.5 transition-colors shadow-2xs"
                >
                  {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                  <span>{copied ? 'Copied!' : 'Copy Code'}</span>
                </button>
                <button
                  onClick={() => setSelectedCode(null)}
                  className="p-1.5 rounded-lg text-slate-400 hover:text-slate-800 hover:bg-slate-200 transition-colors"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>

            <div className="p-6 overflow-y-auto flex-1 bg-slate-50">
              <pre className="text-xs font-mono text-slate-900 bg-white p-4 rounded-xl border border-slate-200 leading-relaxed overflow-x-auto whitespace-pre font-medium shadow-2xs">
                {selectedCode.code}
              </pre>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
