import React, { useState, useEffect, useRef } from 'react';
import { useParams, useSearchParams, Link } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import api from '../api/axios';
import { DifficultyBadge, StatusBadge } from '../components/Badges';
import { useAuth } from '../context/AuthContext';
import {
  ChevronLeft,
  Play,
  Upload,
  CheckCircle,
  XCircle,
  AlertTriangle,
  Clock,
  Maximize2,
  Minimize2,
  FileText,
  BookOpen,
  History,
  RotateCcw,
  ChevronDown,
  ChevronUp,
  Terminal
} from 'lucide-react';

export const ProblemDetailPage = () => {
  const { id, contestId: routeContestId } = useParams();
  const [searchParams] = useSearchParams();
  const contestId = routeContestId || searchParams.get('contestId');
  const { isAuthenticated } = useAuth();

  const [problem, setProblem] = useState(null);
  const [contest, setContest] = useState(null);
  const [contestTimeLeft, setContestTimeLeft] = useState('');
  const [language, setLanguage] = useState('JAVA');
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(true);
  const [fullscreen, setFullscreen] = useState(false);

  // Left Tabs: 'description' | 'editorial' | 'submissions'
  const [leftTab, setLeftTab] = useState('description');

  // Bottom Console Drawer: 'testcases' | 'result'
  const [bottomTab, setBottomTab] = useState('testcases');
  const [activeTestCaseIdx, setActiveTestCaseIdx] = useState(0);
  const [useCustomInput, setUseCustomInput] = useState(false);
  const [customInputText, setCustomInputText] = useState('');
  const [drawerCollapsed, setDrawerCollapsed] = useState(false);

  // Past submissions
  const [submissions, setSubmissions] = useState([]);

  // Running & Submission State
  const [running, setRunning] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [lastAction, setLastAction] = useState(null); // 'run' | 'submit'
  const [runResult, setRunResult] = useState(null);
  const [submitResult, setSubmitResult] = useState(null);

  // Ref to track active language safely across Monaco callbacks
  const currentLangRef = useRef('JAVA');
  useEffect(() => {
    currentLangRef.current = language;
  }, [language]);

  const getTemplate = (lang, starter) => {
    switch (lang) {
      case 'PYTHON':
        return `import sys

def main():
    # Read all input from standard input
    input_data = sys.stdin.read().strip()
    if not input_data:
        return
    
    # Write your Python logic here
    lines = input_data.split('\\n')
    

if __name__ == '__main__':
    main()
`;
      case 'CPP':
        return `#include <iostream>
#include <vector>
#include <string>
#include <algorithm>

using namespace std;

int main() {
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);

    // Write your C++ logic here

    return 0;
}
`;
      case 'C':
        return `#include <stdio.h>
#include <stdlib.h>

int main() {
    // Write your C logic here

    return 0;
}
`;
      case 'JAVASCRIPT':
        return `const fs = require('fs');

function main() {
    const input = fs.readFileSync(0, 'utf-8').trim();
    if (!input) return;

    // Write your JavaScript logic here

}

main();
`;
      default:
        return starter || `import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // Write your Java logic here

    }
}
`;
    }
  };

  useEffect(() => {
    fetchProblem();
    fetchSubmissions();
    if (contestId) {
      fetchContest();
    }
  }, [id, contestId]);

  useEffect(() => {
    if (!contest) return;
    const timer = setInterval(() => {
      const now = new Date();
      const diff = new Date(contest.endTime) - now;
      if (diff <= 0) {
        setContestTimeLeft('00:00:00 (Concluded)');
      } else {
        const h = String(Math.floor(diff / (1000 * 60 * 60))).padStart(2, '0');
        const m = String(Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))).padStart(2, '0');
        const s = String(Math.floor((diff % (1000 * 60)) / 1000)).padStart(2, '0');
        setContestTimeLeft(`${h}:${m}:${s}`);
      }
    }, 1000);
    return () => clearInterval(timer);
  }, [contest]);

  const fetchContest = async () => {
    try {
      const res = await api.get(`/contests/${contestId}`);
      setContest(res.data.data);
    } catch (err) {
      console.error('Failed to load contest context', err);
    }
  };

  const fetchProblem = async () => {
    setLoading(true);
    try {
      const res = await api.get(`/problems/${id}`);
      const data = res.data.data;
      setProblem(data);

      if (data.sampleTestCases && data.sampleTestCases.length > 0) {
        setCustomInputText(data.sampleTestCases[0].input || '');
      }

      // Check cached code for Java by default
      const cacheKey = `codeforge_code_${id}_JAVA`;
      const saved = localStorage.getItem(cacheKey);
      const initialCode = saved || getTemplate('JAVA', data.starterCode);
      setCode(initialCode);
      setLanguage('JAVA');
      currentLangRef.current = 'JAVA';
    } catch (err) {
      console.error('Failed to load problem', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchSubmissions = async () => {
    try {
      const res = await api.get(`/submissions/problem/${id}`);
      setSubmissions(res.data.data || []);
    } catch (err) {
      // ignore when unauthenticated
    }
  };

  // Language Change: cleanly save previous language and load new language code
  const handleLanguageChange = (newLang) => {
    if (problem) {
      // Persist current code under current language
      localStorage.setItem(`codeforge_code_${id}_${currentLangRef.current}`, code);
    }
    currentLangRef.current = newLang;
    setLanguage(newLang);

    // Retrieve saved code for target language, or fallback to clean template
    const cacheKey = `codeforge_code_${id}_${newLang}`;
    const saved = localStorage.getItem(cacheKey);
    const starter = newLang === 'JAVA' ? problem?.starterCode : null;
    const nextCode = saved !== null ? saved : getTemplate(newLang, starter);
    setCode(nextCode);
  };

  // Editor code modification
  const handleCodeChange = (newCode) => {
    const val = newCode ?? '';
    setCode(val);
    if (problem) {
      localStorage.setItem(`codeforge_code_${id}_${currentLangRef.current}`, val);
    }
  };

  // Reset current language template
  const handleResetCode = () => {
    if (window.confirm(`Reset ${language} code back to the original starter template?`)) {
      const starter = language === 'JAVA' ? problem?.starterCode : null;
      const template = getTemplate(language, starter);
      setCode(template);
      localStorage.removeItem(`codeforge_code_${id}_${language}`);
    }
  };

  // Run Code against selected test case or custom input
  const handleRun = async () => {
    if (!isAuthenticated) {
      setDrawerCollapsed(false);
      setBottomTab('result');
      setLastAction('run');
      setRunResult({
        status: 'AUTH_REQUIRED',
        errorMessage: 'Authentication required. Please log in to test and execute code.',
      });
      return;
    }

    setRunning(true);
    setDrawerCollapsed(false);
    setBottomTab('result');
    setLastAction('run');
    setRunResult(null);
    setSubmitResult(null);

    try {
      const inputToRun = useCustomInput
        ? customInputText
        : (problem.sampleTestCases?.[activeTestCaseIdx]?.input ?? '');

      const res = await api.post('/submissions/run', {
        problemId: problem.id,
        language,
        code,
        customInput: inputToRun,
        input: inputToRun,
      });
      setRunResult(res.data.data);
    } catch (err) {
      setRunResult({
        status: 'EXECUTION_ERROR',
        errorMessage: err.response?.data?.message || 'Failed to execute code in sandbox environment.',
      });
    } finally {
      setRunning(false);
    }
  };

  // Submit Code against full test suite
  const handleSubmit = async () => {
    if (!isAuthenticated) {
      setDrawerCollapsed(false);
      setBottomTab('result');
      setLastAction('submit');
      setSubmitResult({
        status: 'AUTH_REQUIRED',
        errorMessage: 'Authentication required. Please log in to submit your solution.',
      });
      return;
    }

    setSubmitting(true);
    setDrawerCollapsed(false);
    setBottomTab('result');
    setLastAction('submit');
    setSubmitResult(null);
    setRunResult(null);

    try {
      const payload = {
        problemId: problem.id,
        language,
        code,
      };
      if (contestId) {
        payload.contestId = Number(contestId);
      }

      const res = await api.post('/submissions', payload);
      setSubmitResult(res.data.data);
      fetchSubmissions();
    } catch (err) {
      setSubmitResult({
        status: 'SUBMISSION_ERROR',
        errorMessage: err.response?.data?.message || 'Failed to submit solution.',
      });
    } finally {
      setSubmitting(false);
    }
  };

  const getMonacoLang = (lang) => {
    switch (lang) {
      case 'PYTHON': return 'python';
      case 'CPP':
      case 'C': return 'cpp';
      case 'JAVASCRIPT': return 'javascript';
      default: return 'java';
    }
  };

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center bg-slate-50">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <p className="text-slate-500 text-sm font-sans">Loading Problem Workspace...</p>
        </div>
      </div>
    );
  }

  if (!problem) {
    return (
      <div className="min-h-[calc(100vh-4rem)] flex flex-col items-center justify-center bg-slate-50 text-center p-4">
        <h2 className="text-xl font-bold text-slate-900 mb-2">Problem Not Found</h2>
        <p className="text-slate-500 text-xs mb-6">The requested problem could not be found or has been removed.</p>
        <Link to="/problems" className="px-4 py-2 bg-blue-600 text-white rounded-lg text-xs font-semibold hover:bg-blue-500 transition-colors shadow-sm">
          Back to Problem List
        </Link>
      </div>
    );
  }

  const activeResult = lastAction === 'submit' ? submitResult : (runResult || submitResult);

  return (
    <div className={`flex flex-col ${fullscreen ? 'fixed inset-0 z-50' : 'h-[calc(100vh-4rem)]'} bg-slate-50 font-sans text-slate-800`}>
      {/* Contest Banner if active */}
      {contest && (
        <div className="bg-amber-50 border-b border-amber-200 px-4 py-2 flex items-center justify-between text-xs">
          <div className="flex items-center gap-2">
            <span className="font-bold text-amber-900">Contest Mode:</span>
            <span className="text-amber-800 font-medium">{contest.title}</span>
          </div>
          <div className="flex items-center gap-2 font-mono text-amber-700 font-bold">
            <Clock className="w-3.5 h-3.5" />
            <span>Time Left: {contestTimeLeft}</span>
          </div>
        </div>
      )}

      {/* Main Split-Pane Workspace */}
      <div className="flex-1 flex flex-col md:flex-row overflow-hidden">
        
        {/* LEFT PANE: Description, Editorial, Submissions */}
        <div className="w-full md:w-1/2 border-r border-slate-200 flex flex-col overflow-hidden bg-white">
          
          {/* Breadcrumb & Left Header */}
          <div className="p-3 px-6 border-b border-slate-200 flex items-center justify-between bg-slate-50">
            <Link
              to="/problems"
              className="text-xs text-slate-500 hover:text-slate-900 flex items-center gap-1 transition-colors font-medium"
            >
              <ChevronLeft className="w-4 h-4" />
              <span>Back to Problems</span>
            </Link>
          </div>

          {/* Problem Title & Difficulty Badge */}
          <div className="px-6 pt-4 pb-3 flex items-center justify-between gap-3 border-b border-slate-200">
            <div className="flex items-center gap-3">
              <h1 className="text-lg font-bold text-slate-900 tracking-tight">
                {problem.id}. {problem.title}
              </h1>
              <DifficultyBadge difficulty={problem.difficulty} />
            </div>
          </div>

          {/* Left Navigation Tabs: [Description] [Editorial] [Submissions] */}
          <div className="flex border-b border-slate-200 px-6 bg-slate-50">
            <button
              onClick={() => setLeftTab('description')}
              className={`py-2.5 px-3 text-xs font-bold border-b-2 flex items-center gap-1.5 transition-colors ${
                leftTab === 'description'
                  ? 'border-blue-600 text-blue-600 bg-white shadow-2xs'
                  : 'border-transparent text-slate-600 hover:text-slate-900'
              }`}
            >
              <FileText className="w-3.5 h-3.5" />
              <span>Description</span>
            </button>
            <button
              onClick={() => setLeftTab('editorial')}
              className={`py-2.5 px-3 text-xs font-bold border-b-2 flex items-center gap-1.5 transition-colors ${
                leftTab === 'editorial'
                  ? 'border-blue-600 text-blue-600 bg-white shadow-2xs'
                  : 'border-transparent text-slate-600 hover:text-slate-900'
              }`}
            >
              <BookOpen className="w-3.5 h-3.5" />
              <span>Editorial</span>
            </button>
            <button
              onClick={() => setLeftTab('submissions')}
              className={`py-2.5 px-3 text-xs font-bold border-b-2 flex items-center gap-1.5 transition-colors ${
                leftTab === 'submissions'
                  ? 'border-blue-600 text-blue-600 bg-white shadow-2xs'
                  : 'border-transparent text-slate-600 hover:text-slate-900'
              }`}
            >
              <History className="w-3.5 h-3.5" />
              <span>Submissions ({submissions.length})</span>
            </button>
          </div>

          {/* Left Content Body */}
          <div className="flex-1 overflow-y-auto p-6 space-y-6 text-xs sm:text-sm text-slate-800 leading-relaxed bg-white">
            {leftTab === 'description' && (
              <>
                <div className="whitespace-pre-line font-sans text-slate-800">
                  {problem.description}
                </div>

                {/* Sample Examples */}
                {problem.sampleTestCases && problem.sampleTestCases.length > 0 && (
                  <div className="space-y-4 pt-2">
                    {problem.sampleTestCases.map((tc, idx) => (
                      <div key={idx} className="p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-2 shadow-2xs">
                        <span className="text-xs font-bold text-blue-600 block font-mono">
                          Example {idx + 1}:
                        </span>
                        <div className="font-mono text-xs space-y-1">
                          <div>
                            <span className="text-slate-500 font-semibold">Input: </span>
                            <span className="text-slate-900 font-medium">{tc.input.trim()}</span>
                          </div>
                          <div>
                            <span className="text-slate-500 font-semibold">Output: </span>
                            <span className="text-emerald-700 font-bold">{tc.expectedOutput.trim()}</span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* Constraints */}
                {problem.constraints && (
                  <div className="pt-2">
                    <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                      Constraints:
                    </h3>
                    <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200 font-mono text-xs text-slate-800 whitespace-pre-line leading-relaxed shadow-2xs">
                      {problem.constraints}
                    </div>
                  </div>
                )}

                {/* Topics */}
                {problem.topics && problem.topics.length > 0 && (
                  <div className="pt-2">
                    <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                      Topics:
                    </h3>
                    <div className="flex flex-wrap gap-2">
                      {problem.topics.map((topic) => (
                        <span
                          key={topic}
                          className="px-2.5 py-1 rounded-md text-xs font-semibold bg-slate-100 text-slate-700 border border-slate-200"
                        >
                          {topic}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </>
            )}

            {leftTab === 'editorial' && (
              <div className="space-y-4 font-sans text-xs sm:text-sm">
                <h3 className="text-sm font-bold text-slate-900">Algorithmic Approach & Analysis</h3>
                <p className="text-slate-700 leading-relaxed">
                  For <strong>{problem.title}</strong>, an optimal approach utilizes an auxiliary data structure or two-pointer technique to reduce computational complexity.
                </p>
                <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-2 font-mono text-xs shadow-2xs">
                  <div className="text-blue-600 font-bold">Time Complexity: O(N)</div>
                  <div className="text-emerald-700 font-bold">Space Complexity: O(N) or O(1) auxiliary space</div>
                </div>
                <p className="text-slate-500 leading-relaxed">
                  Always inspect boundary constraints and ensure edge cases (e.g. empty inputs, single elements, negative values, maximum range integers) are tested.
                </p>
              </div>
            )}

            {leftTab === 'submissions' && (
              <div className="space-y-3">
                <h3 className="text-sm font-bold text-slate-900 mb-2">Your Past Submissions</h3>
                {submissions.length === 0 ? (
                  <p className="text-xs text-slate-500">No submissions recorded yet for this problem.</p>
                ) : (
                  <div className="divide-y divide-slate-200 border border-slate-200 rounded-xl overflow-hidden shadow-2xs">
                    {submissions.map((sub) => (
                      <div key={sub.id} className="p-3 bg-white hover:bg-slate-50 transition-colors flex items-center justify-between text-xs">
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <StatusBadge status={sub.status} />
                            <span className="text-slate-600 font-mono text-[11px] font-semibold">{sub.language}</span>
                          </div>
                          <p className="text-[11px] text-slate-400">
                            {new Date(sub.createdAt).toLocaleString()}
                          </p>
                        </div>
                        <div className="text-right font-mono text-[11px] text-slate-700 font-semibold">
                          <div>{sub.runtime ? `${sub.runtime} ms` : '—'}</div>
                          <div>{sub.memory ? `${Math.round(sub.memory / 1024 * 10) / 10} MB` : '—'}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* RIGHT PANE: Code Editor & Test Cases / Results */}
        <div className="w-full md:w-1/2 flex flex-col overflow-hidden bg-white">
          
          {/* Top Editor Toolbar */}
          <div className="px-4 py-2 bg-slate-50 border-b border-slate-200 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <select
                value={language}
                onChange={(e) => handleLanguageChange(e.target.value)}
                className="bg-white border border-slate-300 text-xs text-slate-800 rounded-lg px-3 py-1.5 focus:outline-none focus:border-blue-600 cursor-pointer font-semibold shadow-xs"
              >
                <option value="JAVA">Java (OpenJDK 17/25)</option>
                <option value="PYTHON">Python (3.10+)</option>
                <option value="CPP">C++ (GCC / C++17)</option>
                <option value="C">C (GCC)</option>
                <option value="JAVASCRIPT">JavaScript (Node.js)</option>
              </select>

              <button
                onClick={handleResetCode}
                title="Reset Code Template"
                className="p-1.5 rounded-lg text-slate-600 hover:text-slate-900 hover:bg-slate-200 transition-colors border border-slate-300 shadow-2xs"
              >
                <RotateCcw className="w-3.5 h-3.5" />
              </button>
            </div>

            <div className="flex items-center gap-2">
              {/* Run Button */}
              <button
                onClick={handleRun}
                disabled={running || submitting}
                className="px-3.5 py-1.5 rounded-lg text-xs font-bold bg-white hover:bg-slate-100 text-slate-800 border border-slate-300 flex items-center gap-1.5 transition-colors disabled:opacity-50 shadow-xs"
              >
                {running ? (
                  <div className="w-3.5 h-3.5 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                ) : (
                  <Play className="w-3.5 h-3.5 fill-current text-blue-600" />
                )}
                <span>Run</span>
              </button>

              {/* Submit Button */}
              <button
                onClick={handleSubmit}
                disabled={running || submitting}
                className="px-4 py-1.5 rounded-lg text-xs font-bold bg-blue-600 hover:bg-blue-500 text-white flex items-center gap-1.5 transition-colors shadow-xs disabled:opacity-50"
              >
                {submitting ? (
                  <div className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                ) : (
                  <Upload className="w-3.5 h-3.5" />
                )}
                <span>Submit</span>
              </button>

              <button
                onClick={() => setFullscreen(!fullscreen)}
                title={fullscreen ? "Exit Fullscreen" : "Fullscreen"}
                className="p-1.5 rounded-lg text-slate-500 hover:text-slate-900 hover:bg-slate-200 transition-colors"
              >
                {fullscreen ? <Minimize2 className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {/* Monaco Editor Container */}
          <div className="flex-1 min-h-0 relative overflow-hidden bg-white">
            <Editor
              height="100%"
              language={getMonacoLang(language)}
              theme="light"
              value={code}
              onChange={handleCodeChange}
              options={{
                fontSize: 13,
                fontFamily: 'Consolas, Monaco, "Courier New", monospace',
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                smoothScrolling: true,
                padding: { top: 12, bottom: 12 },
                lineNumbers: 'on',
                renderLineHighlight: 'all',
                automaticLayout: true,
                tabSize: 4,
                wordWrap: 'on',
              }}
            />
          </div>

          {/* BOTTOM DRAWER: Test Cases & Execution Result */}
          <div className={`${drawerCollapsed ? 'h-10' : 'h-64 sm:h-72'} border-t border-slate-200 flex flex-col bg-slate-50 transition-all duration-150`}>
            
            {/* Drawer Tabs: [Test Cases] [Result] + Collapse/Expand */}
            <div className="flex items-center justify-between px-4 border-b border-slate-200 bg-slate-100 select-none">
              <div className="flex">
                <button
                  onClick={() => {
                    setBottomTab('testcases');
                    setDrawerCollapsed(false);
                  }}
                  className={`py-2 px-4 text-xs font-bold border-b-2 transition-colors ${
                    bottomTab === 'testcases' && !drawerCollapsed
                      ? 'border-blue-600 text-blue-600 bg-white'
                      : 'border-transparent text-slate-600 hover:text-slate-900'
                  }`}
                >
                  Test Cases
                </button>
                <button
                  onClick={() => {
                    setBottomTab('result');
                    setDrawerCollapsed(false);
                  }}
                  className={`py-2 px-4 text-xs font-bold border-b-2 transition-colors flex items-center gap-1.5 ${
                    bottomTab === 'result' && !drawerCollapsed
                      ? 'border-blue-600 text-blue-600 bg-white'
                      : 'border-transparent text-slate-600 hover:text-slate-900'
                  }`}
                >
                  <span>Result</span>
                  {activeResult && (
                    <span className={`w-2 h-2 rounded-full ${
                      activeResult.status === 'ACCEPTED' ? 'bg-emerald-500' : 'bg-rose-500'
                    }`}></span>
                  )}
                </button>
              </div>

              <div className="flex items-center gap-3">
                <span className="text-[11px] text-slate-500 font-mono hidden sm:inline flex items-center gap-1">
                  <Terminal className="w-3 h-3 text-emerald-600" />
                  Sandbox Execution Engine
                </span>
                <button
                  onClick={() => setDrawerCollapsed(!drawerCollapsed)}
                  title={drawerCollapsed ? "Expand Console" : "Collapse Console"}
                  className="p-1 rounded text-slate-500 hover:text-slate-900 hover:bg-slate-200 transition-colors"
                >
                  {drawerCollapsed ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {/* Drawer Body (Hidden if collapsed) */}
            {!drawerCollapsed && (
              <div className="flex-1 p-4 overflow-y-auto font-mono text-xs bg-slate-50">
                
                {/* Tab: Test Cases */}
                {bottomTab === 'testcases' && (
                  <div className="space-y-3">
                    {/* Case Pill Switcher */}
                    <div className="flex flex-wrap items-center gap-2 pb-2 border-b border-slate-200">
                      {problem.sampleTestCases?.map((_, idx) => (
                        <button
                          key={idx}
                          onClick={() => {
                            setActiveTestCaseIdx(idx);
                            setUseCustomInput(false);
                          }}
                          className={`px-3 py-1 rounded-md text-xs font-bold transition-colors ${
                            !useCustomInput && activeTestCaseIdx === idx
                              ? 'bg-blue-600 text-white shadow-xs'
                              : 'bg-white border border-slate-300 text-slate-700 hover:bg-slate-100'
                          }`}
                        >
                          Case {idx + 1}
                        </button>
                      ))}

                      <button
                        onClick={() => setUseCustomInput(true)}
                        className={`px-3 py-1 rounded-md text-xs font-bold transition-colors ${
                          useCustomInput
                            ? 'bg-blue-600 text-white shadow-xs'
                            : 'bg-white border border-slate-300 text-slate-700 hover:bg-slate-100'
                        }`}
                      >
                        + Custom Input
                      </button>
                    </div>

                    {/* Content: Selected Sample Case OR Custom Input */}
                    {!useCustomInput ? (
                      problem.sampleTestCases && problem.sampleTestCases.length > 0 ? (
                        <div className="space-y-3 pt-1">
                          <div>
                            <span className="text-slate-600 block mb-1 font-sans text-xs font-semibold">Input (stdin):</span>
                            <pre className="bg-white p-2.5 rounded-lg text-slate-900 border border-slate-300 whitespace-pre-wrap font-medium">
                              {problem.sampleTestCases[activeTestCaseIdx]?.input || ''}
                            </pre>
                          </div>
                          <div>
                            <span className="text-slate-600 block mb-1 font-sans text-xs font-semibold">Expected Output:</span>
                            <pre className="bg-white p-2.5 rounded-lg text-emerald-700 border border-slate-300 whitespace-pre-wrap font-bold">
                              {problem.sampleTestCases[activeTestCaseIdx]?.expectedOutput || ''}
                            </pre>
                          </div>
                        </div>
                      ) : (
                        <p className="text-slate-500">No public sample test cases configured.</p>
                      )
                    ) : (
                      <div className="space-y-2 pt-1">
                        <span className="text-slate-600 block font-sans text-xs font-semibold">Custom Input (passed directly to program stdin):</span>
                        <textarea
                          rows={4}
                          value={customInputText}
                          onChange={(e) => setCustomInputText(e.target.value)}
                          placeholder="Type input data here to test your solution..."
                          className="w-full bg-white p-2.5 rounded-lg text-slate-900 border border-slate-300 focus:outline-none focus:border-blue-600 focus:ring-1 focus:ring-blue-600 font-mono text-xs"
                        />
                      </div>
                    )}
                  </div>
                )}

                {/* Tab: Result */}
                {bottomTab === 'result' && (
                  <div className="h-full flex flex-col justify-center">
                    {running && (
                      <div className="flex flex-col items-center justify-center gap-2 py-8 text-blue-600">
                        <div className="w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                        <span className="font-sans text-xs font-bold">Executing solution in isolated sandbox...</span>
                      </div>
                    )}

                    {submitting && (
                      <div className="flex flex-col items-center justify-center gap-2 py-8 text-purple-600">
                        <div className="w-6 h-6 border-2 border-purple-600 border-t-transparent rounded-full animate-spin"></div>
                        <span className="font-sans text-xs font-bold">Evaluating full test suite in sandbox...</span>
                      </div>
                    )}

                    {!running && !submitting && !activeResult && (
                      <div className="text-center py-8 text-slate-500 font-sans text-xs">
                        Click <strong>Run</strong> to test your code against sample or custom inputs, or <strong>Submit</strong> to evaluate all test cases.
                      </div>
                    )}

                    {!running && !submitting && activeResult && (
                      <div className="space-y-4">
                        {/* Accepted Status Banner */}
                        {activeResult.status === 'ACCEPTED' ? (
                          <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-300 space-y-3 shadow-xs">
                            <div className="flex items-center gap-3">
                              <div className="w-8 h-8 rounded-full bg-emerald-100 text-emerald-700 flex items-center justify-center flex-shrink-0">
                                <CheckCircle className="w-5 h-5" />
                              </div>
                              <div>
                                <div className="text-base font-bold text-emerald-800 font-sans">
                                  {lastAction === 'submit' ? 'Accepted — Full Test Suite Passed' : 'Accepted — Sample Passed'}
                                </div>
                                <div className="text-xs text-slate-700 font-sans">
                                  {lastAction === 'submit' ? 'Congratulations! All test cases passed successfully.' : 'Output matched expected output.'}
                                </div>
                              </div>
                            </div>

                            <div className="flex flex-wrap items-center gap-4 text-xs font-mono text-slate-700 pt-1 border-t border-emerald-200">
                              <span>Runtime: <strong className="text-slate-900">{activeResult.runtime || 12} ms</strong></span>
                              <span>Memory: <strong className="text-slate-900">{activeResult.memory ? `${Math.round(activeResult.memory / 1024 * 10) / 10} MB` : '38.6 MB'}</strong></span>
                              {lastAction === 'submit' && activeResult.totalTests && (
                                <span>Passed: <strong className="text-emerald-700 font-bold">{activeResult.testsPassed} / {activeResult.totalTests}</strong></span>
                              )}
                            </div>

                            {/* Show Run Details if available */}
                            {lastAction === 'run' && (
                              <div className="space-y-2 pt-2 text-xs">
                                {activeResult.input !== undefined && (
                                  <div>
                                    <span className="text-slate-600 block mb-0.5 font-sans font-semibold">Input:</span>
                                    <pre className="bg-white p-2 rounded text-slate-900 border border-emerald-300 whitespace-pre-wrap font-medium">
                                      {activeResult.input}
                                    </pre>
                                  </div>
                                )}
                                {activeResult.actualOutput !== undefined && (
                                  <div>
                                    <span className="text-slate-600 block mb-0.5 font-sans font-semibold">Your Output:</span>
                                    <pre className="bg-white p-2 rounded text-emerald-800 border border-emerald-300 whitespace-pre-wrap font-bold">
                                      {activeResult.actualOutput || '(empty output)'}
                                    </pre>
                                  </div>
                                )}
                              </div>
                            )}
                          </div>
                        ) : (
                          /* Error / Wrong Answer Banner */
                          <div className="p-4 rounded-xl bg-rose-50 border border-rose-300 space-y-3 shadow-xs">
                            <div className="flex items-center gap-3">
                              <div className="w-8 h-8 rounded-full bg-rose-100 text-rose-700 flex items-center justify-center flex-shrink-0">
                                {activeResult.status === 'COMPILATION_ERROR' ? (
                                  <AlertTriangle className="w-5 h-5" />
                                ) : (
                                  <XCircle className="w-5 h-5" />
                                )}
                              </div>
                              <div>
                                <div className="text-base font-bold text-rose-800 font-sans">
                                  {activeResult.status?.replace(/_/g, ' ')}
                                </div>
                                <div className="text-xs text-rose-700 font-sans">
                                  {activeResult.errorMessage || 'Execution did not complete successfully or outputs did not match.'}
                                </div>
                              </div>
                            </div>

                            {activeResult.runtime && (
                              <div className="flex flex-wrap items-center gap-4 text-xs font-mono text-slate-700 pt-1 border-t border-rose-200">
                                <span>Runtime: <strong className="text-slate-900">{activeResult.runtime} ms</strong></span>
                                {activeResult.totalTests && (
                                  <span>Passed: <strong className="text-rose-700 font-bold">{activeResult.testsPassed || 0} / {activeResult.totalTests}</strong></span>
                                )}
                              </div>
                            )}

                            {/* Run Error / Output Comparison */}
                            {lastAction === 'run' && (
                              <div className="space-y-2 pt-2 text-xs">
                                {activeResult.input && (
                                  <div>
                                    <span className="text-slate-600 block mb-0.5 font-sans font-semibold">Input:</span>
                                    <pre className="bg-white p-2 rounded text-slate-900 border border-slate-300 whitespace-pre-wrap font-medium">
                                      {activeResult.input}
                                    </pre>
                                  </div>
                                )}
                                {activeResult.actualOutput !== undefined && activeResult.actualOutput !== null && (
                                  <div>
                                    <span className="text-slate-600 block mb-0.5 font-sans font-semibold">Your Output:</span>
                                    <pre className="bg-white p-2 rounded text-rose-700 border border-rose-300 whitespace-pre-wrap font-bold">
                                      {activeResult.actualOutput || '(no output produced)'}
                                    </pre>
                                  </div>
                                )}
                                {activeResult.expectedOutput && (
                                  <div>
                                    <span className="text-slate-600 block mb-0.5 font-sans font-semibold">Expected Output:</span>
                                    <pre className="bg-white p-2 rounded text-emerald-700 border border-emerald-300 whitespace-pre-wrap font-bold">
                                      {activeResult.expectedOutput}
                                    </pre>
                                  </div>
                                )}
                                {activeResult.errorMessage && activeResult.status !== 'WRONG_ANSWER' && (
                                  <div>
                                    <span className="text-rose-700 block mb-0.5 font-sans font-bold">Compiler / Runtime Logs:</span>
                                    <pre className="bg-rose-100 p-2.5 rounded text-rose-900 border border-rose-300 whitespace-pre-wrap font-medium">
                                      {activeResult.errorMessage}
                                    </pre>
                                  </div>
                                )}
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                )}

              </div>
            )}
          </div>

        </div>

      </div>
    </div>
  );
};
