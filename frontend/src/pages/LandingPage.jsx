import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Code2, Trophy, ArrowRight, Play, Cpu, Layers, Terminal } from 'lucide-react';
import api from '../api/axios';
import { Badge } from '../components/Badges';

export function LandingPage() {
  const [featuredProblems, setFeaturedProblems] = useState([]);
  const [activeContest, setActiveContest] = useState(null);

  useEffect(() => {
    api.get('/problems')
      .then((res) => {
        const data = res.data?.data || res.data || [];
        if (Array.isArray(data)) setFeaturedProblems(data.slice(0, 4));
      })
      .catch(() => {});

    api.get('/contests')
      .then((res) => {
        const data = res.data?.data || res.data || [];
        if (Array.isArray(data) && data.length > 0) setActiveContest(data[0]);
      })
      .catch(() => {});
  }, []);

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 font-sans">
      {/* Hero Section */}
      <section className="py-16 sm:py-24 border-b border-slate-200 bg-white">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center space-y-6">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-200">
            <Code2 className="w-3.5 h-3.5" />
            <span>Competitive Programming & Automated Judge</span>
          </div>

          <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight text-slate-900 leading-tight">
            Code. Test. Submit. <br />
            <span className="text-blue-600">Master Algorithmic Problem Solving.</span>
          </h1>

          <p className="text-base sm:text-lg text-slate-600 max-w-2xl mx-auto leading-relaxed">
            Practice curated coding problems in Java, Python, C++, C, and JavaScript with instant test evaluation, isolated sandbox execution, and live timed contests.
          </p>

          <div className="flex items-center justify-center gap-3 pt-2">
            <Link
              to="/problems"
              className="inline-flex items-center gap-2 px-6 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm shadow-xs transition-colors"
            >
              <span>Explore Problems</span>
              <ArrowRight className="w-4 h-4" />
            </Link>
            <Link
              to="/register"
              className="inline-flex items-center gap-2 px-6 py-2.5 rounded-lg border border-slate-300 hover:bg-slate-50 text-slate-800 font-semibold text-sm transition-colors"
            >
              Create Account
            </Link>
          </div>

          {/* Quick Metrics */}
          <div className="pt-8 flex items-center justify-center gap-8 text-xs text-slate-500 font-medium flex-wrap border-t border-slate-100 max-w-xl mx-auto">
            <div>
              <strong className="text-slate-800 font-bold">5 Languages</strong> (Java, C++, Python, C, JS)
            </div>
            <div>
              <strong className="text-slate-800 font-bold">Real-time Judge</strong> (Sub-second execution)
            </div>
            <div>
              <strong className="text-slate-800 font-bold">ICPC Contests</strong> (Live leaderboards)
            </div>
          </div>
        </div>
      </section>

      {/* Curated Problems Preview */}
      <section className="py-14 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-xl font-bold tracking-tight text-slate-900">
              Practice Problems
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Solve problems with Monaco Editor and test against hidden suites.
            </p>
          </div>
          <Link
            to="/problems"
            className="text-xs font-semibold text-blue-600 hover:text-blue-700 inline-flex items-center gap-1"
          >
            <span>View All</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
          {featuredProblems.length > 0 ? (
            featuredProblems.map((prob) => (
              <div
                key={prob.id}
                className="p-4 bg-white border border-slate-200 rounded-xl hover:border-blue-400 hover:shadow-xs transition-all flex items-center justify-between"
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-mono text-slate-400 font-semibold">#{prob.id}</span>
                    <Link
                      to={`/problems/${prob.id}`}
                      className="text-sm font-bold text-slate-900 hover:text-blue-600 transition-colors"
                    >
                      {prob.title}
                    </Link>
                  </div>
                  <div className="flex items-center gap-2 mt-2">
                    <Badge variant={prob.difficulty?.toLowerCase()}>{prob.difficulty}</Badge>
                    {prob.acceptanceRate !== undefined && (
                      <span className="text-xs text-slate-500 font-mono">
                        Acceptance: {prob.acceptanceRate}%
                      </span>
                    )}
                  </div>
                </div>

                <Link
                  to={`/problems/${prob.id}`}
                  className="px-3 py-1.5 rounded-lg text-xs font-bold text-blue-600 bg-blue-50 hover:bg-blue-100 transition-colors inline-flex items-center gap-1"
                >
                  <Play className="w-3 h-3 fill-current" />
                  <span>Solve</span>
                </Link>
              </div>
            ))
          ) : (
            <div className="col-span-2 text-center py-6 text-slate-400 text-xs">
              Loading problem catalogue...
            </div>
          )}
        </div>
      </section>

      {/* Featured Contest Banner if available */}
      {activeContest && (
        <section className="py-8 bg-blue-50/60 border-y border-blue-100">
          <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-lg bg-amber-100 border border-amber-300 text-amber-700 flex items-center justify-center shrink-0">
                  <Trophy className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-xs font-bold text-amber-800 uppercase tracking-wider">
                    Featured Contest • {activeContest.status}
                  </div>
                  <h3 className="text-base font-bold text-slate-900">{activeContest.title}</h3>
                </div>
              </div>
              <Link
                to={`/contests/${activeContest.id}`}
                className="px-5 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs transition-colors shrink-0 shadow-xs"
              >
                Enter Contest
              </Link>
            </div>
          </div>
        </section>
      )}

      {/* Platform Features Grid */}
      <section className="py-14 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-lg mx-auto mb-10 space-y-1.5">
          <h2 className="text-xl font-bold tracking-tight text-slate-900">Key Features</h2>
          <p className="text-xs text-slate-500">
            A lightweight, clean environment designed for focused programming practice.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          <div className="p-5 bg-white border border-slate-200 rounded-xl space-y-2.5 shadow-2xs">
            <div className="w-8 h-8 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center">
              <Terminal className="w-4 h-4" />
            </div>
            <h3 className="text-sm font-bold text-slate-900">Monaco Editor</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              VS Code editor engine with syntax highlighting, auto-formatting, sample tests, and custom input runner.
            </p>
          </div>

          <div className="p-5 bg-white border border-slate-200 rounded-xl space-y-2.5 shadow-2xs">
            <div className="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <Cpu className="w-4 h-4" />
            </div>
            <h3 className="text-sm font-bold text-slate-900">Automated Judge</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              Sandboxed execution engine with strict time limits, memory limits, and clear compiler diagnostics.
            </p>
          </div>

          <div className="p-5 bg-white border border-slate-200 rounded-xl space-y-2.5 shadow-2xs">
            <div className="w-8 h-8 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center">
              <Layers className="w-4 h-4" />
            </div>
            <h3 className="text-sm font-bold text-slate-900">Role-Based Access</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              Tailored workspaces for Participants, Contest Organizers, and Administrators with JWT authentication.
            </p>
          </div>
        </div>
      </section>

      {/* Clean Footer */}
      <footer className="py-8 border-t border-slate-200 text-center text-xs text-slate-500 bg-white">
        <p>© 2026 CodeForge Platform. Clean & light competitive programming.</p>
      </footer>
    </div>
  );
}

export default LandingPage;
